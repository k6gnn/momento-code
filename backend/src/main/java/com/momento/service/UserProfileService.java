package com.momento.service;

import com.momento.entity.UserProfile;
import com.momento.repository.CapsuleRepository;
import com.momento.repository.DiscoveryRepository;
import com.momento.repository.UserProfileRepository;
import com.momento.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final CapsuleRepository capsuleRepository;
    private final DiscoveryRepository discoveryRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, CapsuleRepository capsuleRepository, DiscoveryRepository discoveryRepository) {
        this.userProfileRepository = userProfileRepository;
        this.capsuleRepository = capsuleRepository;
        this.discoveryRepository = discoveryRepository;
    }

    @Transactional
    public UserProfile getOrCreate(AuthenticatedUser authUser) {
        return userProfileRepository.findById(authUser.internalUserId()).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUserId(authUser.internalUserId());
            profile.setEmail(authUser.email());
            profile.setUsername(authUser.displayName() != null ? authUser.displayName() : authUser.email().split("@")[0]);
            return userProfileRepository.save(profile);
        });
    }

    public long droppedCount(UserProfile profile) { return capsuleRepository.countByCreator(profile); }
    public long discoveredCount(UserProfile profile) { return discoveryRepository.countByDiscoverer(profile); }
}
