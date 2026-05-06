package com.momento.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "capsules")
public class Capsule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "capsule_id", columnDefinition = "uuid")
    private UUID capsuleId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "creator_id")
    private UserProfile creator;

    @Column(columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "text_content")
    private String textContent;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "expiry_at", nullable = false)
    private OffsetDateTime expiryAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(nullable = false)
    private String status = "ACTIVE";

    public UUID getCapsuleId() { return capsuleId; }
    public UserProfile getCreator() { return creator; }
    public void setCreator(UserProfile creator) { this.creator = creator; }
    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public OffsetDateTime getExpiryAt() { return expiryAt; }
    public void setExpiryAt(OffsetDateTime expiryAt) { this.expiryAt = expiryAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public LocalDate getCreatedDate() { return createdDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
