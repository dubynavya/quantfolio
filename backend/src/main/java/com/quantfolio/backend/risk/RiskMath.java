package com.quantfolio.backend.risk;

import java.util.Arrays;

/**
 * Pure financial math functions, kept free of Spring/JPA so they're trivial to unit test.
 * Trading-day convention: 252 trading days/year is used throughout for annualization.
 */
public final class RiskMath {

    public static final int TRADING_DAYS_PER_YEAR = 252;

    private RiskMath() {}

    /** Simple (non-log) daily returns from a price series. */
    public static double[] dailyReturns(double[] prices) {
        if (prices.length < 2) return new double[0];
        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            returns[i - 1] = (prices[i] - prices[i - 1]) / prices[i - 1];
        }
        return returns;
    }

    public static double mean(double[] values) {
        if (values.length == 0) return 0.0;
        double sum = 0.0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    public static double stdDev(double[] values) {
        if (values.length < 2) return 0.0;
        double m = mean(values);
        double sumSq = 0.0;
        for (double v : values) sumSq += (v - m) * (v - m);
        return Math.sqrt(sumSq / (values.length - 1));
    }

    public static double annualizedReturn(double[] dailyReturns) {
        return mean(dailyReturns) * TRADING_DAYS_PER_YEAR;
    }

    public static double annualizedVolatility(double[] dailyReturns) {
        return stdDev(dailyReturns) * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    public static double sharpeRatio(double annualizedReturn, double annualizedVolatility, double riskFreeRate) {
        if (annualizedVolatility == 0.0) return 0.0;
        return (annualizedReturn - riskFreeRate) / annualizedVolatility;
    }

    /** Largest peak-to-trough decline across the series, as a positive fraction (e.g. 0.23 = -23%). */
    public static double maxDrawdown(double[] prices) {
        if (prices.length == 0) return 0.0;
        double peak = prices[0];
        double worst = 0.0;
        for (double price : prices) {
            peak = Math.max(peak, price);
            double drawdown = (peak - price) / peak;
            worst = Math.max(worst, drawdown);
        }
        return worst;
    }

    /**
     * Historical (empirical) Value-at-Risk: the currency loss such that, based on the historical
     * return distribution, losses worse than this occurred only (1 - confidence) of days.
     * Returns a positive number representing potential loss.
     */
    public static double historicalVaR(double[] dailyReturns, double confidence, double currentValue) {
        if (dailyReturns.length == 0) return 0.0;
        double[] sorted = dailyReturns.clone();
        Arrays.sort(sorted);
        double tail = 1.0 - confidence;
        int index = (int) Math.floor(tail * sorted.length);
        index = Math.max(0, Math.min(sorted.length - 1, index));
        double worstReturn = sorted[index];
        return Math.max(0.0, -worstReturn * currentValue);
    }

    /** Portfolio beta relative to a benchmark: cov(asset, benchmark) / var(benchmark). */
    public static double beta(double[] assetReturns, double[] benchmarkReturns) {
        int n = Math.min(assetReturns.length, benchmarkReturns.length);
        if (n < 2) return 0.0;

        double meanAsset = mean(Arrays.copyOf(assetReturns, n));
        double meanBench = mean(Arrays.copyOf(benchmarkReturns, n));

        double covariance = 0.0;
        double varianceBench = 0.0;
        for (int i = 0; i < n; i++) {
            double da = assetReturns[i] - meanAsset;
            double db = benchmarkReturns[i] - meanBench;
            covariance += da * db;
            varianceBench += db * db;
        }
        if (varianceBench == 0.0) return 0.0;
        return covariance / varianceBench;
    }
}
