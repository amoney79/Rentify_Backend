package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "ownership_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnershipVerification {

    @Id
        private Long id;

    @DBRef
        private Property property;

    @DBRef
        private User applicantUser;

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

        private String specialScenarioNotes;

    private Boolean escalatedToManualReview;

            @Builder.Default
    private PropertyVerificationStatus status = PropertyVerificationStatus.PENDING_REVIEW;

    private String rejectionReason;

    private String additionalDocsRequested;

    @DBRef
        private User reviewer;

    private String reviewerNotes;

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;
}
