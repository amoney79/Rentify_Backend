package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "property_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDocument implements MongoIdentifiable {

    @Id
        private Long id;

    @DBRef
        private Property property;

            private DocumentType documentType;

        private String blobName;

    private String originalFileName;

    private Long fileSize;

    private String mimeType;

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
