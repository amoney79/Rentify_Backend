package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "viewing_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewingRequest implements MongoIdentifiable {

    @Id
        private Long id;

    @DBRef
        private Property property;

    @DBRef
        private User tenant;

    @DBRef
        private User landlord;

        private LocalDate viewingDate;

        private String viewingTime; // e.g. "10:00 AM"

    private String notes;

            @Builder.Default
    private ViewingStatus status = ViewingStatus.REQUESTED;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    }
