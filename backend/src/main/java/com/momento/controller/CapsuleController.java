package com.momento.controller;

import com.momento.dto.CapsuleDtos.*;
import com.momento.entity.UserProfile;
import com.momento.security.AuthenticatedUser;
import com.momento.service.CapsuleService;
import com.momento.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public List<NearbyCapsuleResponse> nearby(@AuthenticationPrincipal AuthenticatedUser user,
                                               @RequestParam double latitude,
                                               @RequestParam double longitude,
                                               @RequestParam(defaultValue = "50") int radiusMeters) {
        return capsuleService.getNearby(user, latitude, longitude, radiusMeters);
    }

    /**
     * Accepts multipart/form-data with two parts:
     *   - "data": JSON matching CreateCapsuleRequest (no localUri)
     *   - "files": zero or more binary file parts, in the same order as the media[] array in "data"
     */
    @PostMapping(value = "/capsules", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@AuthenticationPrincipal AuthenticatedUser user,
                       @RequestPart("data") @Valid CreateCapsuleRequest request,
                       @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        capsuleService.create(user, request, files != null ? files : List.of());
    }

    @PostMapping("/capsules/{capsuleId}/unlock")
    public UnlockResponse unlock(@AuthenticationPrincipal AuthenticatedUser user,
                                  @PathVariable UUID capsuleId,
                                  @Valid @RequestBody UnlockRequest request) {
        return capsuleService.unlock(user, capsuleId, request);
    }

    @GetMapping("/capsules/{capsuleId}/media-urls")
    public List<SignedMediaResponse> refreshMediaUrls(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @PathVariable UUID capsuleId) {
        return capsuleService.getMediaUrls(user, capsuleId);
    }

    @GetMapping("/capsules/mine")
    public List<MyCapsuleResponse> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return capsuleService.myCapsules(user);
    }

    @DeleteMapping("/capsules/{capsuleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
        return new ProfileResponse(profile.getUsername(), profile.getPointsTotal(),
                userProfileService.droppedCount(profile), userProfileService.discoveredCount(profile));
    }
}
