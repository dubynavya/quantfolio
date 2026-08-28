package com.quantfolio.backend.trade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class TradeDtos {

    public record SubmitTradeRequest(
            @NotBlank String ticker,
            @NotNull TradeSide side,
            @DecimalMin(value = "0.000001") BigDecimal quantity
    ) {}

    public record TradeResponse(
            Long id, String ticker, TradeSide side, BigDecimal quantity, BigDecimal estimatedPrice,
            BigDecimal notionalValue, int requiredLevel, TradeStatus status, List<String> reasons,
            Instant createdAt, Instant decidedAt
    ) {
        public static TradeResponse from(TradeRequest t) {
            return new TradeResponse(t.getId(), t.getTicker(), t.getSide(), t.getQuantity(), t.getEstimatedPrice(),
                    t.getNotionalValue(), t.getRequiredLevel(), t.getStatus(), t.getReasons(),
                    t.getCreatedAt(), t.getDecidedAt());
        }
    }
}
