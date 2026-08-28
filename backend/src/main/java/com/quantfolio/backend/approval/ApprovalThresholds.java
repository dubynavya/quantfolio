package com.quantfolio.backend.approval;

public record ApprovalThresholds(
        double level1MaxNotional,
        double level2MaxNotional,
        double level1MaxConcentration,
        double level2MaxConcentration
) {}
