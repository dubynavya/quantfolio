package com.quantfolio.backend.risk;

import java.time.Instant;

public class RiskAlertDtos {

    public record RiskAlertResponse(Long id, RiskAlertType type, String message, Instant createdAt) {
        public static RiskAlertResponse from(RiskAlert a) {
            return new RiskAlertResponse(a.getId(), a.getType(), a.getMessage(), a.getCreatedAt());
        }
    }
}
