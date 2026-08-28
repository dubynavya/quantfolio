package com.quantfolio.backend.trade;

import com.quantfolio.backend.approval.ApprovalEngine;
import com.quantfolio.backend.approval.ApprovalEvaluation;
import com.quantfolio.backend.approval.ApprovalThresholds;
import com.quantfolio.backend.config.QuantfolioProperties;
import com.quantfolio.backend.holding.HoldingService;
import com.quantfolio.backend.marketdata.MarketDataService;
import com.quantfolio.backend.portfolio.Portfolio;
import com.quantfolio.backend.risk.RiskEngineService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class TradeService {

    private final TradeRequestRepository tradeRequestRepository;
    private final HoldingService holdingService;
    private final MarketDataService marketDataService;
    private final RiskEngineService riskEngineService;
    private final QuantfolioProperties properties;

    public TradeService(TradeRequestRepository tradeRequestRepository, HoldingService holdingService,
                         MarketDataService marketDataService, RiskEngineService riskEngineService,
                         QuantfolioProperties properties) {
        this.tradeRequestRepository = tradeRequestRepository;
        this.holdingService = holdingService;
        this.marketDataService = marketDataService;
        this.riskEngineService = riskEngineService;
        this.properties = properties;
    }

    @Transactional
    public TradeRequest submit(Portfolio portfolio, String rawTicker, TradeSide side, BigDecimal quantity) {
        String ticker = rawTicker.trim().toUpperCase();

        if (side == TradeSide.SELL) {
            BigDecimal held = holdingService.find(portfolio, ticker)
                    .map(h -> h.getQuantity())
                    .orElse(BigDecimal.ZERO);
            if (held.compareTo(quantity) < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Cannot sell " + quantity + " shares of " + ticker + " — only " + held + " held");
            }
        }

        double price = marketDataService.getLatestClose(ticker);
        BigDecimal estimatedPrice = BigDecimal.valueOf(price).setScale(6, RoundingMode.HALF_UP);
        BigDecimal notionalValue = quantity.multiply(estimatedPrice).setScale(2, RoundingMode.HALF_UP);

        double signedNotional = side == TradeSide.BUY ? notionalValue.doubleValue() : -notionalValue.doubleValue();
        double concentrationAfterTrade = riskEngineService.computeConcentrationAfterTrade(portfolio, ticker, signedNotional);

        ApprovalThresholds thresholds = new ApprovalThresholds(
                properties.getApproval().getLevel1MaxNotional(),
                properties.getApproval().getLevel2MaxNotional(),
                properties.getApproval().getLevel1MaxConcentration(),
                properties.getApproval().getLevel2MaxConcentration());

        ApprovalEvaluation evaluation = ApprovalEngine.evaluate(
                notionalValue.doubleValue(), Math.max(0, concentrationAfterTrade), thresholds);

        TradeRequest trade = new TradeRequest();
        trade.setPortfolio(portfolio);
        trade.setTicker(ticker);
        trade.setSide(side);
        trade.setQuantity(quantity);
        trade.setEstimatedPrice(estimatedPrice);
        trade.setNotionalValue(notionalValue);
        trade.setRequiredLevel(evaluation.requiredLevel());
        trade.setReasons(evaluation.reasons());

        if (evaluation.autoApproved()) {
            trade.setStatus(TradeStatus.AUTO_APPROVED);
            trade.setDecidedAt(Instant.now());
            applyToHoldings(trade);
        } else {
            trade.setStatus(TradeStatus.PENDING_CONFIRMATION);
        }

        return tradeRequestRepository.save(trade);
    }

    @Transactional
    public TradeRequest confirm(Portfolio portfolio, Long tradeId) {
        TradeRequest trade = getOwned(portfolio, tradeId);
        if (trade.getStatus() != TradeStatus.PENDING_CONFIRMATION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Trade is not awaiting confirmation (status: " + trade.getStatus() + ")");
        }

        applyToHoldings(trade);
        trade.setStatus(TradeStatus.CONFIRMED);
        trade.setDecidedAt(Instant.now());
        return tradeRequestRepository.save(trade);
    }

    @Transactional
    public TradeRequest reject(Portfolio portfolio, Long tradeId) {
        TradeRequest trade = getOwned(portfolio, tradeId);
        if (trade.getStatus() != TradeStatus.PENDING_CONFIRMATION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Trade is not awaiting confirmation (status: " + trade.getStatus() + ")");
        }

        trade.setStatus(TradeStatus.REJECTED);
        trade.setDecidedAt(Instant.now());
        return tradeRequestRepository.save(trade);
    }

    public List<TradeRequest> list(Portfolio portfolio) {
        return tradeRequestRepository.findByPortfolioOrderByCreatedAtDesc(portfolio);
    }

    private void applyToHoldings(TradeRequest trade) {
        if (trade.getSide() == TradeSide.BUY) {
            holdingService.addOrMerge(trade.getPortfolio(), trade.getTicker(), trade.getQuantity(), trade.getEstimatedPrice());
        } else {
            holdingService.reduce(trade.getPortfolio(), trade.getTicker(), trade.getQuantity());
        }
    }

    private TradeRequest getOwned(Portfolio portfolio, Long tradeId) {
        return tradeRequestRepository.findByIdAndPortfolio(tradeId, portfolio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade request not found"));
    }
}
