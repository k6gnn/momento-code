package com.momento.scheduler;

import com.momento.repository.CapsuleRepository;
import com.momento.repository.MediaObjectRepository;
import com.momento.service.NotificationService;
import com.momento.service.S3Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Component
public class ExpiryScheduler {
    private final CapsuleRepository capsuleRepository;
    private final MediaObjectRepository mediaObjectRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;

    public ExpiryScheduler(CapsuleRepository capsuleRepository, MediaObjectRepository mediaObjectRepository, S3Service s3Service, NotificationService notificationService) {
        this.capsuleRepository = capsuleRepository;
        this.mediaObjectRepository = mediaObjectRepository;
        this.s3Service = s3Service;
        this.notificationService = notificationService;
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void expireCapsules() {
        var expired = capsuleRepository.findByStatusAndExpiryAtBefore("ACTIVE", OffsetDateTime.now());
        for (var capsule : expired) {
            mediaObjectRepository.findByCapsule(capsule).forEach(m -> s3Service.deleteObject(m.getStorageKey()));
            capsule.setStatus("EXPIRED");
            capsuleRepository.save(capsule);
        }
    }

    /**
     * Notify creators of capsules expiring in the next 3 days.
     *
     * Bug fix: the original query found all ACTIVE capsules expiring before (now + 3 days),
     * which also included already-expired capsules that lost the race with the hourly job.
     * Now we bound between now and threshold so we only notify for genuinely upcoming expirations.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyUpcomingExpiry() {
        var now = OffsetDateTime.now();
        var threshold = now.plusDays(3);
        capsuleRepository.findByStatusAndExpiryAtBetween("ACTIVE", now, threshold).forEach(c ->
                notificationService.notifyExpiryWarning(c.getCreator().getUserId().toString(), c.getCapsuleId().toString()));
    }
}
