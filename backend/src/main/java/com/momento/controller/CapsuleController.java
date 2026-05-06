package com.momento.controller;

import com.momento.dto.CapsuleDtos.*;
import com.momento.security.AuthenticatedUser;
import com.momento.service.CapsuleService;
import com.momento.service.UserProfileService;
import com.momento.entity.UserProfile;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CapsuleController {
    private final CapsuleService capsuleService;
    private final UserProfileService userProfileService;

    public CapsuleController(CapsuleService capsuleService, UserProfileService userProfileService) {
        this.capsuleService = capsuleService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/capsules/nearby")
    public List<NearbyCapsuleResponse> nearby(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "50") int radiusMeters) {
        return capsuleService.getNearby(user, latitude, longitude, radiusMeters);
    }

    /**
     * Create a capsule with optional photo.
     * Accepts multipart/form-data so the real image bytes are transferred.
     * Fields:
     *   latitude    – required
     *   longitude   – required
     *   textContent – optional text message
     *   photo       – optional image file
     */
    @PostMapping(value = "/capsules", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam @NotNull Double latitude,
            @RequestParam @NotNull Double longitude,
            @RequestParam(required = false) String textContent,
            @RequestPart(required = false) MultipartFile photo) {
        capsuleService.create(user, latitude, longitude, textContent, photo);
    }

    @PostMapping("/capsules/{capsuleId}/unlock")
    public UnlockResponse unlock(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID capsuleId,
            @RequestBody UnlockRequest request) {
        return capsuleService.unlock(user, capsuleId, request);
    }

    @GetMapping("/capsules/mine")
    public List<MyCapsuleResponse> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return capsuleService.myCapsules(user);
    }

    @DeleteMapping("/capsules/{capsuleId}")
    public void delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable UUID capsuleId) {
        capsuleService.deleteOwnCapsule(user, capsuleId);
    }

    @GetMapping("/discoveries/me")
    public List<DiscoveryResponse> discoveries(@AuthenticationPrincipal AuthenticatedUser user) {
        return capsuleService.myDiscoveries(user);
    }

    @GetMapping("/users/me")
    public ProfileResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        UserProfile profile = userProfileService.getOrCreate(user);
        return new ProfileResponse(
                profile.getUsername(),
                profile.getPointsTotal(),
                userProfileService.droppedCount(profile),
                userProfileService.discoveredCount(profile));
    }
}