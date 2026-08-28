package com.quantfolio.backend.risk;

import java.util.List;

public record RiskMetrics(
        double totalValue,
        double annualizedReturn,
        double annualizedVolatility,
        double sharpeRatio,
        double maxDrawdown,
        double valueAtRisk95,
        double beta,
        List<HoldingWeight> holdingWeights,
        List<DatedValue> equityCurve
) {
    public record HoldingWeight(String ticker, double marketValue, double weight) {}
    public record DatedValue(String date, double value) {}
}
