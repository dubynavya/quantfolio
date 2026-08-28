package com.quantfolio.backend.trade;

import com.quantfolio.backend.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade_request")
@Getter
@Setter
@NoArgsConstructor
public class TradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false)
    private String ticker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeSide side;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal estimatedPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal notionalValue;

    @Column(nullable = false)
    private int requiredLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trade_request_reason", joinColumns = @JoinColumn(name = "trade_request_id"))
    @Column(name = "reason", length = 500)
    private List<String> reasons = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant decidedAt;
}
