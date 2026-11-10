package com.amoney.rentify_backend.service;

import com.amoney.rentify_backend.dto.NameMatchResult;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NameMatchingEngineService {

    private static final Set<String> TITLES_AND_PREFIXES = Set.of(
            "MR", "MRS", "MS", "DR", "DOCTOR", "HON", "HONOURABLE", "ENG", "ENGINEER",
            "PROF", "PROFESSOR", "ADV", "ADVOCATE", "REV", "REVEREND", "PASTOR", "ESQ",
            "CPA", "CHIEF", "ELDER", "SENATOR", "GOVERNOR"
    );

    private static final Set<String> SUFFIXES = Set.of(
            "JR", "SR", "II", "III", "IV", "LTD", "LIMITED", "INC", "CORP", "PLC", "HOLDINGS"
    );

    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[^A-Z0-9\\s]");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

    private final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    private final LevenshteinDistance levenshtein = new LevenshteinDistance();

    @Value("${rentify.verification.ownership-name-match-threshold:0.82}")
    private double threshold;

    public String normalizeName(String name) {
        if (name == null) return "";
        String upper = name.toUpperCase().trim();
        String cleaned = PUNCTUATION_PATTERN.matcher(upper).replaceAll(" ");
        String normalized = MULTI_SPACE_PATTERN.matcher(cleaned).replaceAll(" ").trim();

        String[] tokens = normalized.split("\\s+");
        List<String> filtered = Arrays.stream(tokens)
                .filter(t -> !TITLES_AND_PREFIXES.contains(t) && !SUFFIXES.contains(t))
                .filter(t -> t.length() > 0)
                .collect(Collectors.toList());

        return String.join(" ", filtered);
    }

    public NameMatchResult compareNames(String name1, String name2) {
        String norm1 = normalizeName(name1);
        String norm2 = normalizeName(name2);

        if (norm1.isEmpty() || norm2.isEmpty()) {
            return NameMatchResult.builder()
                    .name1(name1)
                    .name2(name2)
                    .normalizedName1(norm1)
                    .normalizedName2(norm2)
                    .tokenSimilarityScore(0.0)
                    .levenshteinSimilarityScore(0.0)
                    .jaroWinklerScore(0.0)
                    .combinedScore(0.0)
                    .isMatch(false)
                    .matchTier("NO_MATCH")
                    .build();
        }

        if (norm1.equalsIgnoreCase(norm2)) {
            return NameMatchResult.builder()
                    .name1(name1)
                    .name2(name2)
                    .normalizedName1(norm1)
                    .normalizedName2(norm2)
                    .tokenSimilarityScore(1.0)
                    .levenshteinSimilarityScore(1.0)
                    .jaroWinklerScore(1.0)
                    .combinedScore(1.0)
                    .isMatch(true)
                    .matchTier("EXACT")
                    .build();
        }

        // 1. Token similarity (handles permutations like "James Mwangi Kamau" vs "Kamau James Mwangi")
        double tokenScore = calculateTokenSimilarity(norm1, norm2);

        // 2. Normalized Levenshtein similarity
        int maxLen = Math.max(norm1.length(), norm2.length());
        int dist = levenshtein.apply(norm1, norm2);
        double levenshteinScore = maxLen == 0 ? 1.0 : Math.max(0.0, 1.0 - ((double) dist / maxLen));

        // 3. Jaro-Winkler similarity
        double jwScore = jaroWinkler.apply(norm1, norm2);

        // 4. Combined weighted score:
        // Token score has highest weight (0.45) for African/global name reordering
        // Jaro-Winkler weight (0.30) for prefix typing & phonetics
        // Levenshtein weight (0.25) for character level edits
        double combinedScore = (tokenScore * 0.45) + (jwScore * 0.30) + (levenshteinScore * 0.25);
        combinedScore = Math.min(1.0, Math.max(0.0, Math.round(combinedScore * 1000.0) / 1000.0));

        boolean isMatch = combinedScore >= threshold;
        String tier;
        if (combinedScore >= 0.95) tier = "EXACT_OR_EQUIVALENT";
        else if (combinedScore >= 0.85) tier = "HIGH_CONFIDENCE";
        else if (combinedScore >= 0.70) tier = "PARTIAL_REVIEW";
        else tier = "NO_MATCH";

        return NameMatchResult.builder()
                .name1(name1)
                .name2(name2)
                .normalizedName1(norm1)
                .normalizedName2(norm2)
                .tokenSimilarityScore(tokenScore)
                .levenshteinSimilarityScore(levenshteinScore)
                .jaroWinklerScore(jwScore)
                .combinedScore(combinedScore)
                .isMatch(isMatch)
                .matchTier(tier)
                .build();
    }

    private double calculateTokenSimilarity(String norm1, String norm2) {
        Set<String> tokens1 = new HashSet<>(Arrays.asList(norm1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(norm2.split("\\s+")));

        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;

        // Exact token intersection (Jaccard)
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        double jaccard = (double) intersection.size() / union.size();

        // Fuzzy pairwise token matching for slight typos in individual names (e.g. "John" vs "Jon")
        double fuzzyMatchedCount = 0.0;
        for (String t1 : tokens1) {
            double bestMatch = 0.0;
            for (String t2 : tokens2) {
                double sim = jaroWinkler.apply(t1, t2);
                if (sim > bestMatch) bestMatch = sim;
            }
            if (bestMatch >= 0.85) {
                fuzzyMatchedCount += bestMatch;
            }
        }

        double fuzzyTokenScore = fuzzyMatchedCount / Math.max(tokens1.size(), tokens2.size());
        return Math.max(jaccard, fuzzyTokenScore);
    }
}
