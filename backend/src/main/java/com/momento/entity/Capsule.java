package com.momento.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "capsules")
public class Capsule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "capsule_id")
    private UUID capsuleId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id")
    private UserProfile creator;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "expiry_at", nullable = false)
    private OffsetDateTime expiryAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    private String status = "ACTIVE";

    public UUID getCapsuleId() { return capsuleId; }
    public UserProfile getCreator() { return creator; }
    public void setCreator(UserProfile creator) { this.creator = creator; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public OffsetDateTime getExpiryAt() { return expiryAt; }
    public void setExpiryAt(OffsetDateTime expiryAt) { this.expiryAt = expiryAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
