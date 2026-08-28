package com.quantfolio.backend.risk;

import com.quantfolio.backend.config.QuantfolioProperties;
import com.quantfolio.backend.portfolio.Portfolio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RiskAlertService {

    private static final Logger log = LoggerFactory.getLogger(RiskAlertService.class);

    private final RiskAlertRepository riskAlertRepository;
    private final QuantfolioProperties properties;

    public RiskAlertService(RiskAlertRepository riskAlertRepository, QuantfolioProperties properties) {
        this.riskAlertRepository = riskAlertRepository;
        this.properties = properties;
    }

    /** Evaluates a freshly-computed RiskMetrics snapshot against configured thresholds and persists any breaches. */
    public List<RiskAlert> evaluateAndPersist(Portfolio portfolio, RiskMetrics metrics) {
        List<RiskAlert> newAlerts = new ArrayList<>();
        QuantfolioProperties.Risk thresholds = properties.getRisk();

        if (metrics.annualizedVolatility() > thresholds.getVolatilityAlertThreshold()) {
            newAlerts.add(new RiskAlert(portfolio, RiskAlertType.HIGH_VOLATILITY, String.format(Locale.US,
                    "Annualized volatility %.1f%% exceeds the %.0f%% threshold",
                    metrics.annualizedVolatility() * 100, thresholds.getVolatilityAlertThreshold() * 100)));
        }

        if (metrics.maxDrawdown() > thresholds.getDrawdownAlertThreshold()) {
            newAlerts.add(new RiskAlert(portfolio, RiskAlertType.DEEP_DRAWDOWN, String.format(Locale.US,
                    "Max drawdown %.1f%% exceeds the %.0f%% threshold",
                    metrics.maxDrawdown() * 100, thresholds.getDrawdownAlertThreshold() * 100)));
        }

        metrics.holdingWeights().stream()
                .filter(w -> w.weight() > thresholds.getConcentrationAlertThreshold())
                .forEach(w -> newAlerts.add(new RiskAlert(portfolio, RiskAlertType.HIGH_CONCENTRATION, String.format(Locale.US,
                        "%s is %.1f%% of the portfolio, exceeding the %.0f%% concentration threshold",
                        w.ticker(), w.weight() * 100, thresholds.getConcentrationAlertThreshold() * 100))));

        if (!newAlerts.isEmpty()) {
            riskAlertRepository.saveAll(newAlerts);
            log.info("Portfolio {} raised {} new risk alert(s)", portfolio.getId(), newAlerts.size());
        }
        return newAlerts;
    }

    public List<RiskAlert> list(Portfolio portfolio) {
        return riskAlertRepository.findByPortfolioOrderByCreatedAtDesc(portfolio);
    }
}
