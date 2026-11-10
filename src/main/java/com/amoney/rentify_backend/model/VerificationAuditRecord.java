package com.amoney.rentify_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_audit_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationAuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // "IDENTITY", "PROPERTY_OWNERSHIP", "LISTING"

    @Column(nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private User performedBy; // Can be null if system-generated

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Double confidenceScore;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
