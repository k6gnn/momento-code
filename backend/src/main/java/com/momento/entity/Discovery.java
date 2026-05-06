package com.momento.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "discoveries", uniqueConstraints = @UniqueConstraint(columnNames = {"capsule_id", "discoverer_id"}))
public class Discovery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "discovery_id")
    private UUID discoveryId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "capsule_id")
    private Capsule capsule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "discoverer_id")
    private UserProfile discoverer;

    @Column(name = "discovered_at", nullable = false)
    private OffsetDateTime discoveredAt = OffsetDateTime.now();

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    public UUID getDiscoveryId() { return discoveryId; }
    public Capsule getCapsule() { return capsule; }
    public void setCapsule(Capsule capsule) { this.capsule = capsule; }
    public UserProfile getDiscoverer() { return discoverer; }
    public void setDiscoverer(UserProfile discoverer) { this.discoverer = discoverer; }
    public OffsetDateTime getDiscoveredAt() { return discoveredAt; }
    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
}
