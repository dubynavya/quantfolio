package com.quantfolio.backend.risk;

import com.quantfolio.backend.portfolio.Portfolio;
import com.quantfolio.backend.portfolio.PortfolioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically recomputes risk metrics for every portfolio and raises alerts on threshold
 * breaches — the automated-monitoring counterpart to the on-demand /risk endpoint.
 */
@Component
public class RiskAlertScanJob {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertScanJob.class);

    private final PortfolioRepository portfolioRepository;
    private final RiskEngineService riskEngineService;
    private final RiskAlertService riskAlertService;

    public RiskAlertScanJob(PortfolioRepository portfolioRepository, RiskEngineService riskEngineService,
                             RiskAlertService riskAlertService) {
        this.portfolioRepository = portfolioRepository;
        this.riskEngineService = riskEngineService;
        this.riskAlertService = riskAlertService;
    }

    @Scheduled(cron = "${quantfolio.alerts.scan-cron}")
    public void scanAllPortfolios() {
        for (Portfolio portfolio : portfolioRepository.findAll()) {
            try {
                RiskMetrics metrics = riskEngineService.computeRiskMetrics(portfolio);
                riskAlertService.evaluateAndPersist(portfolio, metrics);
            } catch (Exception ex) {
                log.warn("Skipping risk scan for portfolio {}: {}", portfolio.getId(), ex.getMessage());
            }
        }
    }
}
