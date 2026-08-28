package com.quantfolio.backend.risk;

import com.quantfolio.backend.portfolio.Portfolio;
import com.quantfolio.backend.portfolio.PortfolioService;
import com.quantfolio.backend.user.CurrentUserService;
import com.quantfolio.backend.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/risk")
public class RiskController {

    private final RiskEngineService riskEngineService;
    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public RiskController(RiskEngineService riskEngineService, PortfolioService portfolioService,
                           CurrentUserService currentUserService) {
        this.riskEngineService = riskEngineService;
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public RiskMetrics getRiskMetrics(@PathVariable Long portfolioId) {
        User user = currentUserService.getCurrentUser();
        Portfolio portfolio = portfolioService.getOwned(portfolioId, user);
        return riskEngineService.computeRiskMetrics(portfolio);
    }
}
