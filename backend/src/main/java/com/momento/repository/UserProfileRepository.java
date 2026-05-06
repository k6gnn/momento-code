package com.momento.repository;

import com.momento.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {}
