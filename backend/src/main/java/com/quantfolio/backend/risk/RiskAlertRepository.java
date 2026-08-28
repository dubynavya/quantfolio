package com.quantfolio.backend.risk;

import com.quantfolio.backend.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {
    List<RiskAlert> findByPortfolioOrderByCreatedAtDesc(Portfolio portfolio);
}
