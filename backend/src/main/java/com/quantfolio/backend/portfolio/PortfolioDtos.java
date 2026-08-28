package com.quantfolio.backend.portfolio;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public class PortfolioDtos {

    public record CreatePortfolioRequest(@NotBlank String name) {}

    public record PortfolioResponse(Long id, String name, String baseCurrency, Instant createdAt) {
        public static PortfolioResponse from(Portfolio p) {
            return new PortfolioResponse(p.getId(), p.getName(), p.getBaseCurrency(), p.getCreatedAt());
        }
    }
}
