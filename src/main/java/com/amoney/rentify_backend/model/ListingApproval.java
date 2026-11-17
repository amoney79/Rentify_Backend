package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "listing_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingApproval implements MongoIdentifiable {

    @Id
        private Long id;

    @DBRef
        private Property property;

    @DBRef
        private User approvedBy;

            private PropertyVerificationStatus approvalStatus;

        private String remarks;

    @Builder.Default
    private LocalDateTime approvedAt = LocalDateTime.now();
}
