package com.quantfolio.backend.risk;

import com.quantfolio.backend.portfolio.Portfolio;
import com.quantfolio.backend.portfolio.PortfolioService;
import com.quantfolio.backend.user.CurrentUserService;
import com.quantfolio.backend.user.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.quantfolio.backend.risk.RiskAlertDtos.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/alerts")
public class RiskAlertController {

    private final RiskAlertService riskAlertService;
    private final RiskEngineService riskEngineService;
    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public RiskAlertController(RiskAlertService riskAlertService, RiskEngineService riskEngineService,
                                PortfolioService portfolioService, CurrentUserService currentUserService) {
        this.riskAlertService = riskAlertService;
        this.riskEngineService = riskEngineService;
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<RiskAlertResponse> list(@PathVariable Long portfolioId) {
        return riskAlertService.list(ownedPortfolio(portfolioId)).stream().map(RiskAlertResponse::from).toList();
    }

    /** Runs the same threshold scan the nightly job runs, on demand — handy for a live demo. */
    @PostMapping("/scan")
    public List<RiskAlertResponse> scanNow(@PathVariable Long portfolioId) {
        Portfolio portfolio = ownedPortfolio(portfolioId);
        RiskMetrics metrics = riskEngineService.computeRiskMetrics(portfolio);
        return riskAlertService.evaluateAndPersist(portfolio, metrics).stream().map(RiskAlertResponse::from).toList();
    }

    private Portfolio ownedPortfolio(Long portfolioId) {
        User user = currentUserService.getCurrentUser();
        return portfolioService.getOwned(portfolioId, user);
    }
}
