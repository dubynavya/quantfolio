package com.quantfolio.backend.risk;

import com.quantfolio.backend.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "risk_alert")
@Getter
@Setter
@NoArgsConstructor
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskAlertType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public RiskAlert(Portfolio portfolio, RiskAlertType type, String message) {
        this.portfolio = portfolio;
        this.type = type;
        this.message = message;
    }
}
