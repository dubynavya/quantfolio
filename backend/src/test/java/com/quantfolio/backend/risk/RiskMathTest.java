package com.quantfolio.backend.risk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskMathTest {

    private static final double EPSILON = 1e-9;

    @Test
    void dailyReturns_computesSimplePercentChange() {
        double[] prices = {100.0, 110.0, 99.0};
        double[] returns = RiskMath.dailyReturns(prices);

        assertEquals(2, returns.length);
        assertEquals(0.10, returns[0], EPSILON);
        assertEquals(-0.10, returns[1], EPSILON);
    }

    @Test
    void dailyReturns_tooFewPrices_returnsEmpty() {
        assertEquals(0, RiskMath.dailyReturns(new double[]{100.0}).length);
        assertEquals(0, RiskMath.dailyReturns(new double[]{}).length);
    }

    @Test
    void maxDrawdown_findsWorstPeakToTroughDecline() {
        // Peaks at 120, troughs at 90 -> 25% drawdown; later dips to 95 from a lower peak of 110 (~13.6%)
        double[] prices = {100, 120, 90, 110, 95};
        assertEquals(0.25, RiskMath.maxDrawdown(prices), EPSILON);
    }

    @Test
    void maxDrawdown_monotonicallyRising_isZero() {
        double[] prices = {100, 105, 110, 120};
        assertEquals(0.0, RiskMath.maxDrawdown(prices), EPSILON);
    }

    @Test
    void sharpeRatio_zeroVolatility_returnsZeroRatherThanDividingByZero() {
        assertEquals(0.0, RiskMath.sharpeRatio(0.10, 0.0, 0.04), EPSILON);
    }

    @Test
    void sharpeRatio_standardCase() {
        // (0.15 - 0.04) / 0.20 = 0.55
        assertEquals(0.55, RiskMath.sharpeRatio(0.15, 0.20, 0.04), EPSILON);
    }

    @Test
    void historicalVaR_picksLossAtTailPercentile() {
        // 20 returns, worst is -0.05; at 95% confidence with 20 samples the 5th-percentile index is 1
        double[] returns = new double[20];
        for (int i = 0; i < 20; i++) returns[i] = -0.05 + i * 0.01; // -0.05 .. 0.14
        double var = RiskMath.historicalVaR(returns, 0.95, 10_000.0);

        // sorted[1] = -0.04 -> VaR = 400
        assertEquals(400.0, var, 1e-6);
    }

    @Test
    void historicalVaR_neverNegative() {
        double[] allPositiveReturns = {0.01, 0.02, 0.03, 0.015, 0.025};
        assertTrue(RiskMath.historicalVaR(allPositiveReturns, 0.95, 10_000.0) >= 0.0);
    }

    @Test
    void beta_ofBenchmarkAgainstItself_isOne() {
        double[] benchmark = {0.01, -0.02, 0.03, 0.005, -0.01};
        assertEquals(1.0, RiskMath.beta(benchmark, benchmark), 1e-9);
    }

    @Test
    void beta_ofDoubleLeveragedSeries_isTwo() {
        double[] benchmark = {0.01, -0.02, 0.03, 0.005, -0.01};
        double[] leveraged = new double[benchmark.length];
        for (int i = 0; i < benchmark.length; i++) leveraged[i] = benchmark[i] * 2;

        assertEquals(2.0, RiskMath.beta(leveraged, benchmark), 1e-9);
    }

    @Test
    void beta_flatBenchmark_returnsZeroRatherThanDividingByZero() {
        double[] flatBenchmark = {0.0, 0.0, 0.0, 0.0};
        double[] asset = {0.01, -0.01, 0.02, -0.02};
        assertEquals(0.0, RiskMath.beta(asset, flatBenchmark), EPSILON);
    }
}
