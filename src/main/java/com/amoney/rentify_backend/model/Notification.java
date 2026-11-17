package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
        private Long id;

    @DBRef
        private User recipient;

        private String title;

        private String message;

        private String type; // "IDENTITY_APPROVED", "IDENTITY_REJECTED", "PROPERTY_APPROVED", "DOC_REQUESTED", "VIEWING_UPDATE"

        @Builder.Default
    private Boolean isRead = false;

    private String actionUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
