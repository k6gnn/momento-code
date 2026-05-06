package com.momento.repository;

import com.momento.entity.Capsule;
import com.momento.entity.Discovery;
import com.momento.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscoveryRepository extends JpaRepository<Discovery, UUID> {
    List<Discovery> findByDiscovererOrderByDiscoveredAtDesc(UserProfile discoverer);
    long countByDiscoverer(UserProfile discoverer);
    Optional<Discovery> findByCapsuleAndDiscoverer(Capsule capsule, UserProfile discoverer);
}
