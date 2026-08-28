package com.quantfolio.backend.holding;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class HoldingDtos {

    public record AddHoldingRequest(
            @NotBlank String ticker,
            @DecimalMin(value = "0.000001") BigDecimal quantity,
            @DecimalMin(value = "0.000001") BigDecimal avgCostPrice
    ) {}

    public record HoldingResponse(Long id, String ticker, BigDecimal quantity, BigDecimal avgCostPrice) {
        public static HoldingResponse from(Holding h) {
            return new HoldingResponse(h.getId(), h.getTicker(), h.getQuantity(), h.getAvgCostPrice());
        }
    }
}
