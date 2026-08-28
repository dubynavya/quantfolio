package com.quantfolio.backend.approval;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Rule-based, multi-level trade routing — a real-data successor to the static approval routing
 * built in github.com/dubynavya/req-approval. Instead of routing generic requests through fixed
 * rules, it routes trades through levels derived from live notional value and the resulting
 * portfolio concentration.
 *
 * Level 1: auto-approved, no human sign-off needed.
 * Level 2: requires the user to explicitly confirm (simulates manager review).
 * Level 3: requires explicit confirmation and carries a high-risk warning (simulates risk-committee review).
 */
public final class ApprovalEngine {

    private ApprovalEngine() {}

    public static ApprovalEvaluation evaluate(double notional, double concentrationAfterTrade, ApprovalThresholds t) {
        List<String> reasons = new ArrayList<>();

        boolean withinLevel1 = notional <= t.level1MaxNotional() && concentrationAfterTrade <= t.level1MaxConcentration();
        boolean withinLevel2 = notional <= t.level2MaxNotional() && concentrationAfterTrade <= t.level2MaxConcentration();

        if (withinLevel1) {
            reasons.add(String.format(Locale.US,
                    "Notional $%,.2f and resulting concentration %.1f%% are both within level-1 auto-approve limits",
                    notional, concentrationAfterTrade * 100));
            return new ApprovalEvaluation(1, true, reasons);
        }

        if (notional > t.level1MaxNotional()) {
            reasons.add(String.format(Locale.US,
                    "Notional $%,.2f exceeds the level-1 auto-approve limit of $%,.2f", notional, t.level1MaxNotional()));
        }
        if (concentrationAfterTrade > t.level1MaxConcentration()) {
            reasons.add(String.format(Locale.US,
                    "Resulting concentration %.1f%% exceeds the level-1 limit of %.1f%%",
                    concentrationAfterTrade * 100, t.level1MaxConcentration() * 100));
        }

        if (withinLevel2) {
            return new ApprovalEvaluation(2, false, reasons);
        }

        if (notional > t.level2MaxNotional()) {
            reasons.add(String.format(Locale.US,
                    "Notional $%,.2f exceeds the level-2 review limit of $%,.2f", notional, t.level2MaxNotional()));
        }
        if (concentrationAfterTrade > t.level2MaxConcentration()) {
            reasons.add(String.format(Locale.US,
                    "Resulting concentration %.1f%% exceeds the level-2 limit of %.1f%% — high concentration risk",
                    concentrationAfterTrade * 100, t.level2MaxConcentration() * 100));
        }

        return new ApprovalEvaluation(3, false, reasons);
    }
}
