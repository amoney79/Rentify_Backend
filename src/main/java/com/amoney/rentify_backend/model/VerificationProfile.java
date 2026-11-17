package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "verification_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationProfile {

    @Id
        private Long id;

    @DBRef
        private User user;

            @Builder.Default
    private IdentityStatus status = IdentityStatus.PENDING;

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

    @DBRef
        private User reviewer;

    private String reviewerNotes;

    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    private LocalDateTime expiresAt;
}
