package com.amoney.rentify_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NameMatchResult {
    private String name1;
    private String name2;
    private String normalizedName1;
    private String normalizedName2;
    private Double tokenSimilarityScore;
    private Double levenshteinSimilarityScore;
    private Double jaroWinklerScore;
    private Double combinedScore; // 0.0 to 1.0 (or 0% to 100%)
    private Boolean isMatch;
    private String matchTier; // "EXACT", "HIGH_CONFIDENCE", "PARTIAL_REVIEW", "NO_MATCH"
}
