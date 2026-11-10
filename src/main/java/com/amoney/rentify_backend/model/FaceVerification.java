package com.amoney.rentify_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_profile_id", nullable = false)
    private VerificationProfile verificationProfile;

    private Double faceMatchConfidence;

    private Double livenessConfidence;

    private Boolean isIdentical;

    private Boolean isLive;

    private Boolean spoofDetected;

    private String spoofType; // "PRINT_ATTACK", "VIDEO_REPLAY", "3D_MASK", "NONE"

    @Column(columnDefinition = "TEXT")
    private String rawApiResponse;

    @Builder.Default
    private LocalDateTime verifiedAt = LocalDateTime.now();
}
