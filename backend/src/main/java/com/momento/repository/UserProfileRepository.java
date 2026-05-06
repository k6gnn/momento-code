package com.momento.repository;

import com.momento.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    /**
     * Atomic upsert — inserts a new user row or does nothing if the
     * user_id already exists. Prevents duplicate-key errors when multiple
     * requests arrive simultaneously on first login.
     */
    @Modifying
    @Query(value = """
        INSERT INTO users (user_id, email, username, auth_provider, created_at,
                           points_total, dropped_count, discovered_count)
        VALUES (:userId, :email, :username, 'FIREBASE', now(), 0, 0, 0)
        ON CONFLICT (user_id) DO NOTHING
        """, nativeQuery = true)
    void upsertUser(@Param("userId") UUID userId,
                    @Param("email") String email,
                    @Param("username") String username);
}