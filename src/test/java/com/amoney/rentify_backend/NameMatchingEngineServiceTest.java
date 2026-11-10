package com.amoney.rentify_backend;

import com.amoney.rentify_backend.dto.NameMatchResult;
import com.amoney.rentify_backend.service.NameMatchingEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameMatchingEngineServiceTest {

    private NameMatchingEngineService nameMatchingService;

    @BeforeEach
    void setUp() {
        nameMatchingService = new NameMatchingEngineService();
    }

    @Test
    @DisplayName("Exact match should produce 1.0 confidence score")
    void testExactMatch() {
        NameMatchResult result = nameMatchingService.compareNames("James Mwangi", "James Mwangi");
        assertTrue(result.getIsMatch());
        assertEquals(1.0, result.getCombinedScore());
        assertEquals("EXACT", result.getMatchTier());
    }

    @Test
    @DisplayName("Inverted name order should pass with high confidence score")
    void testInvertedNameOrder() {
        NameMatchResult result = nameMatchingService.compareNames("James Mwangi Kamau", "Kamau Mwangi James");
        assertTrue(result.getIsMatch());
        assertTrue(result.getCombinedScore() >= 0.85);
    }

    @Test
    @DisplayName("Salutations and corporate suffixes should be cleanly normalized")
    void testSalutationAndSuffixRemoval() {
        String norm1 = nameMatchingService.normalizeName("Dr. David Omwamba, Esq.");
        String norm2 = nameMatchingService.normalizeName("David Omwamba Ltd");
        assertEquals("DAVID OMWAMBA", norm1);
        assertEquals("DAVID OMWAMBA", norm2);

        NameMatchResult result = nameMatchingService.compareNames("Dr. David Omwamba", "David Omwamba");
        assertTrue(result.getIsMatch());
        assertTrue(result.getCombinedScore() >= 0.95);
    }

    @Test
    @DisplayName("Completely different names should produce NO_MATCH tier")
    void testMismatchedNames() {
        NameMatchResult result = nameMatchingService.compareNames("Johnathan Kiprono", "Alice Wanjiku");
        assertFalse(result.getIsMatch());
        assertEquals("NO_MATCH", result.getMatchTier());
        assertTrue(result.getCombinedScore() < 0.50);
    }
}
