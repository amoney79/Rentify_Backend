package com.amoney.rentify_backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.time.LocalDateTime;

@Document(collection = "face_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceVerification implements MongoIdentifiable {

    @Id
        private Long id;

    @DBRef
        private VerificationProfile verificationProfile;

    private Double faceMatchConfidence;

    private Double livenessConfidence;

    private Boolean isIdentical;

    private Boolean isLive;

    private Boolean spoofDetected;

    private String spoofType; // "PRINT_ATTACK", "VIDEO_REPLAY", "3D_MASK", "NONE"

        private String rawApiResponse;

    @Builder.Default
    private LocalDateTime verifiedAt = LocalDateTime.now();
}
