package com.momento.service;

import com.momento.entity.UserProfile;
import com.momento.repository.UserProfileRepository;
import com.momento.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Atomically upserts the user row (INSERT ... ON CONFLICT DO NOTHING),
     * then fetches and returns it. Safe under concurrent first-login requests.
     */
    @Transactional
    public UserProfile getOrCreate(AuthenticatedUser authUser) {
        String username = authUser.displayName() != null
                ? authUser.displayName()
                : authUser.email().split("@")[0];

        // Atomic upsert — no duplicate key error if two requests race.
        userProfileRepository.upsertUser(
                authUser.internalUserId(),
                authUser.email(),
                username);

        // Always fetch the canonical row after the upsert.
        return userProfileRepository.findById(authUser.internalUserId())
                .orElseThrow(() -> new IllegalStateException("User row missing after upsert"));
    }

    public long droppedCount(UserProfile profile) { return profile.getDroppedCount(); }
    public long discoveredCount(UserProfile profile) { return profile.getDiscoveredCount(); }
}