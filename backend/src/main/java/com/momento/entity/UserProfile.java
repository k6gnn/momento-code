package com.momento.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserProfile {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(name = "points_total", nullable = false)
    private Integer pointsTotal = 0;

    @Column(name = "dropped_count", nullable = false)
    private Integer droppedCount = 0;

    @Column(name = "discovered_count", nullable = false)
    private Integer discoveredCount = 0;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "auth_provider", nullable = false)
    private String authProvider = "FIREBASE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getPointsTotal() { return pointsTotal; }
    public void setPointsTotal(Integer pointsTotal) { this.pointsTotal = pointsTotal; }
    public Integer getDroppedCount() { return droppedCount; }
    public void setDroppedCount(Integer droppedCount) { this.droppedCount = droppedCount; }
    public Integer getDiscoveredCount() { return discoveredCount; }
    public void setDiscoveredCount(Integer discoveredCount) { this.discoveredCount = discoveredCount; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
