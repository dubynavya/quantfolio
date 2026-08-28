package com.quantfolio.backend.trade;

import com.quantfolio.backend.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {
    List<TradeRequest> findByPortfolioOrderByCreatedAtDesc(Portfolio portfolio);
    Optional<TradeRequest> findByIdAndPortfolio(Long id, Portfolio portfolio);
}
