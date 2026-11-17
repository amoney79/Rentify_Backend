package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "verification_audit_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationAuditRecord implements MongoIdentifiable {

    @Id
        private Long id;

        private String entityType; // "IDENTITY", "PROPERTY_OWNERSHIP", "LISTING"

        private Long entityId;

    @DBRef
        private User performedBy; // Can be null if system-generated

            private AuditAction action;

        private String notes;

    private Double confidenceScore;

        private String metadataJson;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
