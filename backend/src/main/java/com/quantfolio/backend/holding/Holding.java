package com.quantfolio.backend.holding;

import com.quantfolio.backend.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "holding")
@Getter
@Setter
@NoArgsConstructor
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    /** Stooq-style ticker, e.g. "aapl.us" */
    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal avgCostPrice;

    public Holding(Portfolio portfolio, String ticker, BigDecimal quantity, BigDecimal avgCostPrice) {
        this.portfolio = portfolio;
        this.ticker = ticker;
        this.quantity = quantity;
        this.avgCostPrice = avgCostPrice;
    }
}
