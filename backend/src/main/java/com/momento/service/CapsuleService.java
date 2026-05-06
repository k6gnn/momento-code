package com.momento.service;

import com.momento.dto.CapsuleDtos.*;
import com.momento.entity.*;
import com.momento.repository.*;
import com.momento.security.AuthenticatedUser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CapsuleService {
    private final CapsuleRepository capsuleRepository;
    private final MediaObjectRepository mediaObjectRepository;
    private final DiscoveryRepository discoveryRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileService userProfileService;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final int proximityRadiusMeters;
    private final int dropPoints;
    private final int discoveryPoints;

    public CapsuleService(CapsuleRepository capsuleRepository,
                          MediaObjectRepository mediaObjectRepository,
                          DiscoveryRepository discoveryRepository,
                          UserProfileRepository userProfileRepository,
                          UserProfileService userProfileService,
                          S3Service s3Service,
                          NotificationService notificationService,
                          @Value("${app.security.proximity-radius-meters}") int proximityRadiusMeters,
                          @Value("${app.security.drop-points}") int dropPoints,
                          @Value("${app.security.discovery-points}") int discoveryPoints) {
        this.capsuleRepository = capsuleRepository;
        this.mediaObjectRepository = mediaObjectRepository;
        this.discoveryRepository = discoveryRepository;
        this.userProfileRepository = userProfileRepository;
        this.userProfileService = userProfileService;
        this.s3Service = s3Service;
        this.notificationService = notificationService;
        this.proximityRadiusMeters = proximityRadiusMeters;
        this.dropPoints = dropPoints;
        this.discoveryPoints = discoveryPoints;
    }

    public List<NearbyCapsuleResponse> getNearby(AuthenticatedUser authUser, double latitude, double longitude, int radiusMeters) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        return capsuleRepository.findNearby(latitude, longitude, radiusMeters, profile.getUserId()).stream()
                .map(p -> new NearbyCapsuleResponse(p.getId(), p.getLatitude(), p.getLongitude(), p.getDistanceMeters()))
                .toList();
    }

    @Transactional
    public void create(AuthenticatedUser authUser, Double latitude, Double longitude,
                       String textContent, MultipartFile photo) {
        UserProfile creator = userProfileService.getOrCreate(authUser);

        boolean hasText = textContent != null && !textContent.isBlank();
        boolean hasPhoto = photo != null && !photo.isEmpty();
        if (!hasText && !hasPhoto) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capsule needs text or a photo");
        }

        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);

        Capsule capsule = new Capsule();
        capsule.setCreator(creator);
        capsule.setLocation(point);
        capsule.setLatitude(latitude);
        capsule.setLongitude(longitude);
        capsule.setTextContent(hasText ? textContent.trim() : null);
        capsule.setContentType(hasPhoto ? (hasText ? "MIXED" : "PHOTO") : "TEXT");
        capsule.setExpiryAt(OffsetDateTime.now().plusDays(30));

        try {
            capsule = capsuleRepository.saveAndFlush(capsule);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uq_same_creator_same_spot_24h")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "You already dropped a capsule at this location today. Move a few metres away or come back tomorrow.");
            }
            throw e;
        }

        if (hasPhoto) {
            String originalName = photo.getOriginalFilename() != null
                    ? photo.getOriginalFilename() : "photo.jpg";
            String mimeType = photo.getContentType() != null
                    ? photo.getContentType() : "image/jpeg";
            String key = "capsules/" + capsule.getCapsuleId() + "/" + UUID.randomUUID() + "-" + originalName;

            try {
                s3Service.uploadBytes(key, photo.getBytes(), mimeType);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload photo");
            }

            MediaObject object = new MediaObject();
            object.setCapsule(capsule);
            object.setStorageKey(key);
            object.setMediaType("PHOTO");
            object.setSizeBytes(photo.getSize());
            mediaObjectRepository.save(object);
        }

        creator.setPointsTotal(creator.getPointsTotal() + dropPoints);
        creator.setDroppedCount(creator.getDroppedCount() + 1);
        userProfileRepository.save(creator);
    }

    @Transactional
    public UnlockResponse unlock(AuthenticatedUser authUser, UUID capsuleId, UnlockRequest request) {
        UserProfile user = userProfileService.getOrCreate(authUser);
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capsule not found"));

        if (capsule.getCreator().getUserId().equals(user.getUserId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot discover own capsule");
        if (discoveryRepository.findByCapsuleAndDiscoverer(capsule, user).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already discovered");

        boolean nearby = capsuleRepository
                .findNearby(request.latitude(), request.longitude(), proximityRadiusMeters, user.getUserId())
                .stream().anyMatch(p -> p.getId().equals(capsuleId));
        if (!nearby)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not within proximity radius");

        Discovery discovery = new Discovery();
        discovery.setCapsule(capsule);
        discovery.setDiscoverer(user);
        discovery.setPointsAwarded(discoveryPoints);
        discoveryRepository.save(discovery);

        user.setPointsTotal(user.getPointsTotal() + discoveryPoints);
        user.setDiscoveredCount(user.getDiscoveredCount() + 1);
        userProfileRepository.save(user);

        capsule.setExpiryAt(OffsetDateTime.now().plusDays(30));
        capsuleRepository.save(capsule);

        notificationService.notifyCapsuleDiscovered(
                capsule.getCreator().getUserId().toString(),
                capsule.getCapsuleId().toString());

        List<SignedMediaResponse> media = mediaObjectRepository.findByCapsule(capsule).stream()
                .map(m -> new SignedMediaResponse(
                        m.getMediaId(),
                        m.getMediaType(),
                        s3Service.generateSignedGetUrl(m.getStorageKey()).toString()))
                .toList();

        return new UnlockResponse(capsule.getCapsuleId(), capsule.getTextContent(), media, discoveryPoints, capsule.getExpiryAt());
    }

    public List<MyCapsuleResponse> myCapsules(AuthenticatedUser authUser) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        return capsuleRepository.findByCreatorOrderByCreatedAtDesc(profile).stream()
                .map(c -> new MyCapsuleResponse(c.getCapsuleId(), c.getStatus(), c.getExpiryAt()))
                .toList();
    }

    public List<DiscoveryResponse> myDiscoveries(AuthenticatedUser authUser) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        return discoveryRepository.findByDiscovererOrderByDiscoveredAtDesc(profile).stream()
                .map(d -> new DiscoveryResponse(d.getDiscoveryId(), d.getDiscoveredAt(), d.getPointsAwarded()))
                .toList();
    }

    @Transactional
    public void deleteOwnCapsule(AuthenticatedUser authUser, UUID capsuleId) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        Capsule capsule = capsuleRepository.findByCapsuleIdAndCreator(capsuleId, profile)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capsule not found"));
        mediaObjectRepository.findByCapsule(capsule).forEach(m -> s3Service.deleteObject(m.getStorageKey()));
        capsule.setStatus("DELETED");
        capsuleRepository.save(capsule);
    }
}