package com.quantfolio.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quantfolio")
public class QuantfolioProperties {

    private Jwt jwt = new Jwt();
    private MlService mlService = new MlService();
    private MarketData marketData = new MarketData();
    private Risk risk = new Risk();
    private Approval approval = new Approval();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public MlService getMlService() { return mlService; }
    public void setMlService(MlService mlService) { this.mlService = mlService; }
    public MarketData getMarketData() { return marketData; }
    public void setMarketData(MarketData marketData) { this.marketData = marketData; }
    public Risk getRisk() { return risk; }
    public void setRisk(Risk risk) { this.risk = risk; }
    public Approval getApproval() { return approval; }
    public void setApproval(Approval approval) { this.approval = approval; }

    public static class Jwt {
        private String secret;
        private long expirationMs;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }

    public static class MlService {
        private String baseUrl;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class MarketData {
        private int historyDays;
        public int getHistoryDays() { return historyDays; }
        public void setHistoryDays(int historyDays) { this.historyDays = historyDays; }
    }

    public static class Risk {
        private double riskFreeRate;
        private double varConfidence;
        private String benchmarkTicker;
        private double volatilityAlertThreshold;
        private double drawdownAlertThreshold;
        private double concentrationAlertThreshold;
        public double getRiskFreeRate() { return riskFreeRate; }
        public void setRiskFreeRate(double riskFreeRate) { this.riskFreeRate = riskFreeRate; }
        public double getVarConfidence() { return varConfidence; }
        public void setVarConfidence(double varConfidence) { this.varConfidence = varConfidence; }
        public String getBenchmarkTicker() { return benchmarkTicker; }
        public void setBenchmarkTicker(String benchmarkTicker) { this.benchmarkTicker = benchmarkTicker; }
        public double getVolatilityAlertThreshold() { return volatilityAlertThreshold; }
        public void setVolatilityAlertThreshold(double v) { this.volatilityAlertThreshold = v; }
        public double getDrawdownAlertThreshold() { return drawdownAlertThreshold; }
        public void setDrawdownAlertThreshold(double v) { this.drawdownAlertThreshold = v; }
        public double getConcentrationAlertThreshold() { return concentrationAlertThreshold; }
        public void setConcentrationAlertThreshold(double v) { this.concentrationAlertThreshold = v; }
    }

    public static class Approval {
        private double level1MaxNotional;
        private double level2MaxNotional;
        private double level1MaxConcentration;
        private double level2MaxConcentration;
        public double getLevel1MaxNotional() { return level1MaxNotional; }
        public void setLevel1MaxNotional(double v) { this.level1MaxNotional = v; }
        public double getLevel2MaxNotional() { return level2MaxNotional; }
        public void setLevel2MaxNotional(double v) { this.level2MaxNotional = v; }
        public double getLevel1MaxConcentration() { return level1MaxConcentration; }
        public void setLevel1MaxConcentration(double v) { this.level1MaxConcentration = v; }
        public double getLevel2MaxConcentration() { return level2MaxConcentration; }
        public void setLevel2MaxConcentration(double v) { this.level2MaxConcentration = v; }
    }
}
