package com.momento.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CapsuleDtos {
    public record CreateMediaRequest(@NotBlank String mediaType, @NotBlank String fileName, @NotBlank String mimeType) {}
    public record CreateCapsuleRequest(
            @NotNull Double latitude,
            @NotNull Double longitude,
            @Size(max = 2000) String textContent,
            List<CreateMediaRequest> media) {}
    public record NearbyCapsuleResponse(UUID id, Double latitude, Double longitude, Double distanceMeters) {}
    public record UnlockRequest(@NotNull Double latitude, @NotNull Double longitude) {}
    public record SignedMediaResponse(UUID mediaId, String mediaType, String signedUrl) {}
    public record UnlockResponse(UUID capsuleId, String textContent, List<SignedMediaResponse> media, Integer pointsAwarded, OffsetDateTime expiryAt) {}
    public record ProfileResponse(String username, Integer pointsTotal, Long droppedCount, Long discoveredCount) {}
    public record MyCapsuleResponse(UUID id, String status, OffsetDateTime expiryAt) {}
    public record DiscoveryResponse(UUID discoveryId, OffsetDateTime discoveredAt, Integer pointsAwarded) {}
}
