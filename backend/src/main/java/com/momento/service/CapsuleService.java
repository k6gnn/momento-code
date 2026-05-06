package com.momento.service;

import com.momento.dto.CapsuleDtos.*;
import com.momento.entity.*;
import com.momento.exception.S3StorageException;
import com.momento.repository.*;
import com.momento.security.AuthenticatedUser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger log = LoggerFactory.getLogger(CapsuleService.class);

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
    public void create(AuthenticatedUser authUser, CreateCapsuleRequest request, List<MultipartFile> files) {
        UserProfile creator = userProfileService.getOrCreate(authUser);
        boolean hasText = request.textContent() != null && !request.textContent().isBlank();
        boolean hasMedia = request.media() != null && !request.media().isEmpty();
        if (!hasText && !hasMedia) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Capsule needs text or media");
        }
        if (hasMedia && files.size() != request.media().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of files must match number of media entries");
        }

        Point point = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Capsule capsule = new Capsule();
        capsule.setCreator(creator);
        capsule.setLocation(point);
        capsule.setTextContent(request.textContent());
        capsule.setContentType(hasMedia ? "MIXED" : "TEXT");
        capsule.setExpiryAt(OffsetDateTime.now().plusDays(30));
        capsule = capsuleRepository.save(capsule);

        if (hasMedia) {
            for (int i = 0; i < request.media().size(); i++) {
                CreateMediaRequest meta = request.media().get(i);
                MultipartFile file = files.get(i);
                String key = "capsules/" + capsule.getCapsuleId() + "/" + UUID.randomUUID() + "-" + meta.fileName();

                try {
                    s3Service.uploadBytes(key, file.getBytes(), meta.mimeType());
                } catch (S3StorageException e) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Media upload failed, please try again");
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read uploaded file");
                }

                MediaObject obj = new MediaObject();
                obj.setCapsule(capsule);
                obj.setStorageKey(key);
                obj.setMediaType(meta.mediaType());
                obj.setSizeBytes(file.getSize());
                mediaObjectRepository.save(obj);
            }
        }

        creator.setPointsTotal(creator.getPointsTotal() + dropPoints);
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

        boolean nearby = capsuleRepository.findNearby(request.latitude(), request.longitude(), proximityRadiusMeters, user.getUserId())
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

        // Build signed URLs after all DB writes — S3 failure here must not roll back the discovery
        List<SignedMediaResponse> media = buildSignedMediaList(capsule);
        return new UnlockResponse(capsule.getCapsuleId(), capsule.getTextContent(), media, discoveryPoints, capsule.getExpiryAt());
    }

    public List<SignedMediaResponse> getMediaUrls(AuthenticatedUser authUser, UUID capsuleId) {
        UserProfile profile = userProfileService.getOrCreate(authUser);
        Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Capsule not found"));
        boolean isCreator = capsule.getCreator().getUserId().equals(profile.getUserId());
        boolean isDiscoverer = discoveryRepository.findByCapsuleAndDiscoverer(capsule, profile).isPresent();
        if (!isCreator && !isDiscoverer)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        return buildSignedMediaList(capsule);
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
        mediaObjectRepository.findByCapsule(capsule).forEach(m -> {
            try {
                s3Service.deleteObject(m.getStorageKey());
            } catch (S3StorageException e) {
                log.warn("Could not delete S3 object {} for capsule {}, continuing with deletion", m.getStorageKey(), capsuleId);
            }
        });
        capsule.setStatus("DELETED");
        capsuleRepository.save(capsule);
    }

    private List<SignedMediaResponse> buildSignedMediaList(Capsule capsule) {
        return mediaObjectRepository.findByCapsule(capsule).stream()
                .map(m -> {
                    String url = null;
                    try {
                        url = s3Service.generateSignedGetUrl(m.getStorageKey()).toString();
                    } catch (S3StorageException e) {
                        log.warn("Could not generate signed URL for media {}", m.getMediaId());
                    }
                    return new SignedMediaResponse(m.getMediaId(), m.getMediaType(), url);
                })
                .toList();
    }
}
