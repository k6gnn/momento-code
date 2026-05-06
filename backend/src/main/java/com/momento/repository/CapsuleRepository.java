package com.momento.repository;

import com.momento.entity.Capsule;
import com.momento.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapsuleRepository extends JpaRepository<Capsule, UUID> {
    @Query(value = """
      select c.capsule_id as id,
             ST_Y(c.location::geometry) as latitude,
             ST_X(c.location::geometry) as longitude,
             ST_DistanceSphere(c.location, ST_SetSRID(ST_MakePoint(:longitude,:latitude),4326)) as distanceMeters
      from capsules c
      where c.status = 'ACTIVE'
        and c.creator_id <> :userId
        and c.expiry_at > now()
        and ST_DWithin(c.location::geography, ST_SetSRID(ST_MakePoint(:longitude,:latitude),4326)::geography, :radiusMeters)
        and not exists (
          select 1 from discoveries d where d.capsule_id = c.capsule_id and d.discoverer_id = :userId
        )
      order by distanceMeters asc
      """, nativeQuery = true)
    List<NearbyProjection> findNearby(@Param("latitude") double latitude, @Param("longitude") double longitude, @Param("radiusMeters") int radiusMeters, @Param("userId") UUID userId);

    List<Capsule> findByCreatorOrderByCreatedAtDesc(UserProfile creator);
    long countByCreator(UserProfile creator);
    Optional<Capsule> findByCapsuleIdAndCreator(UUID capsuleId, UserProfile creator);
    List<Capsule> findByStatusAndExpiryAtBefore(String status, OffsetDateTime time);
    List<Capsule> findByStatusAndExpiryAtBetween(String status, OffsetDateTime from, OffsetDateTime to);

    interface NearbyProjection {
        UUID getId();
        Double getLatitude();
        Double getLongitude();
        Double getDistanceMeters();
    }
}
