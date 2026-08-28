package com.quantfolio.backend.approval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalEngineTest {

    private final ApprovalThresholds thresholds = new ApprovalThresholds(10_000, 50_000, 0.20, 0.40);

    @Test
    void smallLowConcentrationTrade_isAutoApprovedAtLevel1() {
        ApprovalEvaluation eval = ApprovalEngine.evaluate(5_000, 0.10, thresholds);

        assertEquals(1, eval.requiredLevel());
        assertTrue(eval.autoApproved());
        assertFalse(eval.reasons().isEmpty());
    }

    @Test
    void mediumNotional_requiresLevel2Confirmation() {
        ApprovalEvaluation eval = ApprovalEngine.evaluate(30_000, 0.10, thresholds);

        assertEquals(2, eval.requiredLevel());
        assertFalse(eval.autoApproved());
    }

    @Test
    void highConcentrationAloneEscalatesEvenWithSmallNotional() {
        ApprovalEvaluation eval = ApprovalEngine.evaluate(1_000, 0.45, thresholds);

        assertEquals(3, eval.requiredLevel());
        assertFalse(eval.autoApproved());
    }

    @Test
    void largeNotional_requiresLevel3Confirmation() {
        ApprovalEvaluation eval = ApprovalEngine.evaluate(75_000, 0.10, thresholds);

        assertEquals(3, eval.requiredLevel());
        assertFalse(eval.autoApproved());
    }

    @Test
    void boundaryNotional_exactlyAtLevel1Limit_isAutoApproved() {
        ApprovalEvaluation eval = ApprovalEngine.evaluate(10_000, 0.20, thresholds);

        assertEquals(1, eval.requiredLevel());
        assertTrue(eval.autoApproved());
    }

    @Test
    void reasonsExplainWhyLevelWasEscalated() {
        ApprovalEvaluation eval = ApprovalEngine.evaluate(75_000, 0.50, thresholds);

        assertTrue(eval.reasons().stream().anyMatch(r -> r.contains("75,000")));
        assertTrue(eval.reasons().stream().anyMatch(r -> r.contains("50.0%")));
    }
}
