package com.amoney.rentify_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceAnalysisResult {
    private Double faceMatchScore;
    private Double livenessScore;
    private Boolean isLive;
    private Boolean spoofDetected;
    private String spoofType; // "PRINTED_PHOTO", "SCREEN_REPLAY", "DEEPFAKE", "NONE"
    private String details;
    private String rawApiResponse;
}
