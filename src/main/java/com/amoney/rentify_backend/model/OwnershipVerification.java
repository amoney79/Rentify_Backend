package com.amoney.rentify_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ownership_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private User applicantUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OwnershipType ownershipType = OwnershipType.INDIVIDUAL;

    // Extracted Fields from Azure AI Document Intelligence
    private String extractedOwnerName;

    private String extractedTitleNumber;

    private String extractedPlotNumber;

    private String extractedParcelNumber;

    private LocalDate extractedRegistrationDate;

    private String extractedCounty;

    private String extractedAddress;

    // Advanced Name Matching Results (Levenshtein, Token similarity, Jaccard, Fuzzy)
    private Double nameMatchScore;

    private Double ownershipConfidenceScore;

    // Corporate / Agent Special Scenario Blob References
    private String corporateRecordBlobName; // CR12 record or Certificate of Incorporation

    private String authorizationLetterBlobName; // Agency agreement / Power of Attorney

    private String directorIdBlobName;

    @Column(columnDefinition = "TEXT")
    private String specialScenarioNotes;

    private Boolean escalatedToManualReview;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PropertyVerificationStatus status = PropertyVerificationStatus.PENDING_REVIEW;

    private String rejectionReason;

    private String additionalDocsRequested;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    private String reviewerNotes;

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;
}
