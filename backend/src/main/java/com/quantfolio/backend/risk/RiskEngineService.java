package com.quantfolio.backend.risk;

import com.quantfolio.backend.config.QuantfolioProperties;
import com.quantfolio.backend.holding.Holding;
import com.quantfolio.backend.holding.HoldingRepository;
import com.quantfolio.backend.marketdata.MarketDataService;
import com.quantfolio.backend.marketdata.PriceBar;
import com.quantfolio.backend.portfolio.Portfolio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
public class RiskEngineService {

    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;
    private final QuantfolioProperties properties;

    public RiskEngineService(HoldingRepository holdingRepository, MarketDataService marketDataService,
                              QuantfolioProperties properties) {
        this.holdingRepository = holdingRepository;
        this.marketDataService = marketDataService;
        this.properties = properties;
    }

    public RiskMetrics computeRiskMetrics(Portfolio portfolio) {
        List<Holding> holdings = holdingRepository.findByPortfolio(portfolio);
        if (holdings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Portfolio has no holdings yet — add at least one to compute risk metrics");
        }

        // Map ticker -> date -> close, and find the dates common to every holding + the benchmark.
        Map<String, Map<LocalDate, Double>> closesByTicker = new HashMap<>();
        Set<LocalDate> commonDates = null;

        for (Holding holding : holdings) {
            List<PriceBar> bars = marketDataService.getHistory(holding.getTicker());
            Map<LocalDate, Double> closes = new HashMap<>();
            for (PriceBar bar : bars) closes.put(bar.date(), bar.close());
            closesByTicker.put(holding.getTicker(), closes);

            commonDates = (commonDates == null) ? new HashSet<>(closes.keySet())
                    : intersect(commonDates, closes.keySet());
        }

        String benchmarkTicker = properties.getRisk().getBenchmarkTicker();
        List<PriceBar> benchmarkBars = marketDataService.getHistory(benchmarkTicker);
        Map<LocalDate, Double> benchmarkCloses = new HashMap<>();
        for (PriceBar bar : benchmarkBars) benchmarkCloses.put(bar.date(), bar.close());
        commonDates = intersect(commonDates, benchmarkCloses.keySet());

        List<LocalDate> sortedDates = new ArrayList<>(commonDates);
        Collections.sort(sortedDates);

        if (sortedDates.size() < 10) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Not enough overlapping trading history across holdings to compute risk metrics yet");
        }

        double[] portfolioValues = new double[sortedDates.size()];
        double[] benchmarkValues = new double[sortedDates.size()];

        for (int i = 0; i < sortedDates.size(); i++) {
            LocalDate date = sortedDates.get(i);
            double value = 0.0;
            for (Holding holding : holdings) {
                double close = closesByTicker.get(holding.getTicker()).get(date);
                value += close * holding.getQuantity().doubleValue();
            }
            portfolioValues[i] = value;
            benchmarkValues[i] = benchmarkCloses.get(date);
        }

        double[] portfolioReturns = RiskMath.dailyReturns(portfolioValues);
        double[] benchmarkReturns = RiskMath.dailyReturns(benchmarkValues);

        double totalValue = portfolioValues[portfolioValues.length - 1];
        double annualizedReturn = RiskMath.annualizedReturn(portfolioReturns);
        double annualizedVolatility = RiskMath.annualizedVolatility(portfolioReturns);
        double sharpe = RiskMath.sharpeRatio(annualizedReturn, annualizedVolatility, properties.getRisk().getRiskFreeRate());
        double maxDrawdown = RiskMath.maxDrawdown(portfolioValues);
        double var95 = RiskMath.historicalVaR(portfolioReturns, properties.getRisk().getVarConfidence(), totalValue);
        double beta = RiskMath.beta(portfolioReturns, benchmarkReturns);

        List<RiskMetrics.HoldingWeight> weights = new ArrayList<>();
        LocalDate latestDate = sortedDates.get(sortedDates.size() - 1);
        for (Holding holding : holdings) {
            double close = closesByTicker.get(holding.getTicker()).get(latestDate);
            double marketValue = close * holding.getQuantity().doubleValue();
            weights.add(new RiskMetrics.HoldingWeight(holding.getTicker(), marketValue, marketValue / totalValue));
        }

        List<RiskMetrics.DatedValue> equityCurve = new ArrayList<>();
        for (int i = 0; i < sortedDates.size(); i++) {
            equityCurve.add(new RiskMetrics.DatedValue(sortedDates.get(i).toString(), portfolioValues[i]));
        }

        return new RiskMetrics(totalValue, annualizedReturn, annualizedVolatility, sharpe,
                maxDrawdown, var95, beta, weights, equityCurve);
    }

    /** Concentration (max single-holding weight) — used by the approval engine independent of the full metrics call. */
    public double computeConcentrationAfterTrade(Portfolio portfolio, String ticker, double additionalNotional) {
        List<Holding> holdings = holdingRepository.findByPortfolio(portfolio);
        double existingValue = 0.0;
        double tickerValue = additionalNotional;

        for (Holding holding : holdings) {
            double close = marketDataService.getLatestClose(holding.getTicker());
            double value = close * holding.getQuantity().doubleValue();
            existingValue += value;
            if (holding.getTicker().equalsIgnoreCase(ticker)) {
                tickerValue += value;
            }
        }

        double totalAfterTrade = existingValue + additionalNotional;
        if (totalAfterTrade <= 0) return 0.0;
        return tickerValue / totalAfterTrade;
    }

    private static Set<LocalDate> intersect(Set<LocalDate> a, Set<LocalDate> b) {
        Set<LocalDate> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }
}
