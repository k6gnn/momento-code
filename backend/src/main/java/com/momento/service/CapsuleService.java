package com.momento.service;

import com.momento.dto.CapsuleDtos.*;
import com.momento.entity.*;
import com.momento.repository.*;
import com.momento.security.AuthenticatedUser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
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

    public CapsuleService(CapsuleRepository capsuleRepository, MediaObjectRepository mediaObjectRepository, DiscoveryRepository discoveryRepository, UserProfileRepository userProfileRepository, UserProfileService userProfileService, S3Service s3Service, NotificationService notificationService, @Value("${app.security.proximity-radius-meters}") int proximityRadiusMeters, @Value("${app.security.drop-points}") int dropPoints, @Value("${app.security.discovery-points}") int discoveryPoints) {
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
    public void create(AuthenticatedUser authUser, CreateCapsuleRequest request) {
        UserProfile creator = userProfileService.getOrCreate(authUser);
        if ((request.textContent() == null || request.textContent().isBlank()) && (request.media() == null || request.media().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capsule needs text or media");
        }
        Point point = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Capsule capsule = new Capsule();
        capsule.setCreator(creator);
        capsule.setLocation(point);
        capsule.setTextContent(request.textContent());
        capsule.setContentType(request.media() != null && !request.media().isEmpty() ? "MIXED" : "TEXT");
        capsule.setExpiryAt(OffsetDateTime.now().plusDays(30));
        capsule = capsuleRepository.save(capsule);

        if (request.media() != null) {
            for (CreateMediaRequest media : request.media()) {
                String key;
                if (media.localUri() != null && !media.localUri().isBlank() && !media.localUri().startsWith("file://") && !media.localUri().startsWith("content://")) {
                    // If localUri is not a local file path, assume it's already a storage key from the mobile app
                    key = media.localUri();
                } else {
                    key = "capsules/" + capsule.getCapsuleId() + "/" + UUID.randomUUID() + "-" + media.fileName();
                    s3Service.uploadBytes(key, ("prototype-placeholder-" + media.fileName()).getBytes(StandardCharsets.UTF_8), media.mimeType());
                }
                
                MediaObject object = new MediaObject();
                object.setCapsule(capsule);
                object.setStorageKey(key);
                object.setMediaType(media.mediaType());
                object.setSizeBytes(0L);
                mediaObjectRepository.save(object);
            }
        }
        creator.setPointsTotal(creator.getPointsTotal() + dropPoints);
        userProfileRepository.save(creator);
    }

    @Transactional
    public UnlockResponse unlock(AuthenticatedUser authUser, UUID capsuleId, UnlockRequest request) {
        UserProfile user = userProfileService.getOrCreate(authUser);
        Capsule capsule = capsuleRepository.findById(capsuleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capsule not found"));
        if (capsule.getCreator().getUserId().equals(user.getUserId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot discover own capsule");
        if (discoveryRepository.findByCapsuleAndDiscoverer(capsule, user).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Already discovered");

        var nearby = capsuleRepository.findNearby(request.latitude(), request.longitude(), proximityRadiusMeters, user.getUserId())
                .stream().anyMatch(p -> p.getId().equals(capsuleId));
        if (!nearby) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not within proximity radius");

        Discovery discovery = new Discovery();
        discovery.setCapsule(capsule);
        discovery.setDiscoverer(user);
        discovery.setPointsAwarded(discoveryPoints);
        discoveryRepository.save(discovery);

        user.setPointsTotal(user.getPointsTotal() + discoveryPoints);
        userProfileRepository.save(user);

        capsule.setExpiryAt(OffsetDateTime.now().plusDays(30));
        capsuleRepository.save(capsule);

        notificationService.notifyCapsuleDiscovered(capsule.getCreator().getUserId().toString(), capsule.getCapsuleId().toString());

        List<SignedMediaResponse> media = mediaObjectRepository.findByCapsule(capsule).stream()
                .map(m -> new SignedMediaResponse(m.getMediaId(), m.getMediaType(), s3Service.generateSignedGetUrl(m.getStorageKey()).toString()))
                .toList();
        return new UnlockResponse(capsule.getCapsuleId(), capsule.getTextContent(), media, discoveryPoints, capsule.getExpiryAt());
    }

    public List<MyCapsuleResponse> myCapsules(AuthenticatedUser authUser) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        return capsuleRepository.findByCreatorOrderByCreatedAtDesc(profile).stream().map(c -> new MyCapsuleResponse(c.getCapsuleId(), c.getStatus(), c.getExpiryAt())).toList();
    }

    public List<DiscoveryResponse> myDiscoveries(AuthenticatedUser authUser) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        return discoveryRepository.findByDiscovererOrderByDiscoveredAtDesc(profile).stream().map(d -> new DiscoveryResponse(d.getDiscoveryId(), d.getDiscoveredAt(), d.getPointsAwarded())).toList();
    }

    @Transactional
    public void deleteOwnCapsule(AuthenticatedUser authUser, UUID capsuleId) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        Capsule capsule = capsuleRepository.findByCapsuleIdAndCreator(capsuleId, profile).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capsule not found"));
        mediaObjectRepository.findByCapsule(capsule).forEach(m -> s3Service.deleteObject(m.getStorageKey()));
        capsule.setStatus("DELETED");
        capsuleRepository.save(capsule);
    }
}
