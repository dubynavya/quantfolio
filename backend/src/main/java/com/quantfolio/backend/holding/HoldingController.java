package com.quantfolio.backend.holding;

import com.quantfolio.backend.portfolio.Portfolio;
import com.quantfolio.backend.portfolio.PortfolioService;
import com.quantfolio.backend.user.CurrentUserService;
import com.quantfolio.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.quantfolio.backend.holding.HoldingDtos.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/holdings")
public class HoldingController {

    private final HoldingService holdingService;
    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public HoldingController(HoldingService holdingService, PortfolioService portfolioService,
                              CurrentUserService currentUserService) {
        this.holdingService = holdingService;
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<HoldingResponse> list(@PathVariable Long portfolioId) {
        Portfolio portfolio = ownedPortfolio(portfolioId);
        return holdingService.list(portfolio).stream().map(HoldingResponse::from).toList();
    }

    @PostMapping
    public HoldingResponse add(@PathVariable Long portfolioId, @Valid @RequestBody AddHoldingRequest request) {
        Portfolio portfolio = ownedPortfolio(portfolioId);
        Holding holding = holdingService.addOrMerge(portfolio, request.ticker(), request.quantity(), request.avgCostPrice());
        return HoldingResponse.from(holding);
    }

    private Portfolio ownedPortfolio(Long portfolioId) {
        User user = currentUserService.getCurrentUser();
        return portfolioService.getOwned(portfolioId, user);
    }
}
