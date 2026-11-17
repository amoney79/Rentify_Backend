package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property implements MongoIdentifiable {

    @Id
        private Long id;

    @DBRef
        private User landlord;

        private String title;

        private String description;

    private String propertyType; // "Apartment", "Villa", "Bedsitter", "Mansion"

    private String county;

    private String sublocation;

    private String address;

    private BigDecimal price;

    private Integer bedrooms;

    private Integer bathrooms;

    private String imageUrl;

            @Builder.Default
    private PropertyVerificationStatus verificationStatus = PropertyVerificationStatus.UNVERIFIED;

        @Builder.Default
    private Boolean isPubliclyVisible = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    }
