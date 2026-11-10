package com.amoney.rentify_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IdentityStatus status = IdentityStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private DocumentType idDocumentType;

    private String idNumber;

    private String extractedFullName;

    private LocalDate extractedDob;

    private LocalDate extractedExpiryDate;

    private String extractedDocumentNumber;

    // Azure Blob storage identifiers
    private String idFrontBlobName;

    private String idBackBlobName;

    private String selfieBlobName;

    // AI Confidence Scores (0.0 to 1.0)
    private Double faceMatchScore;

    private Double livenessScore;

    private Double documentExtractionScore;

    private Double overallConfidence;

    private Boolean isLive;

    private Boolean spoofDetected;

    private String antiSpoofDetails;

    // Review & Workflow Fields
    private String rejectionReason;

    private String additionalDocsRequested;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    private String reviewerNotes;

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    private LocalDateTime expiresAt;
}
