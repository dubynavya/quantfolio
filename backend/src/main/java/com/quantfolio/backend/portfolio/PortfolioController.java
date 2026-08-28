package com.quantfolio.backend.portfolio;

import com.quantfolio.backend.user.CurrentUserService;
import com.quantfolio.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.quantfolio.backend.portfolio.PortfolioDtos.*;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public PortfolioController(PortfolioService portfolioService, CurrentUserService currentUserService) {
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<PortfolioResponse> create(@Valid @RequestBody CreatePortfolioRequest request) {
        User user = currentUserService.getCurrentUser();
        Portfolio portfolio = portfolioService.create(user, request.name());
        return ResponseEntity.ok(PortfolioResponse.from(portfolio));
    }

    @GetMapping
    public List<PortfolioResponse> list() {
        User user = currentUserService.getCurrentUser();
        return portfolioService.listForOwner(user).stream().map(PortfolioResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PortfolioResponse get(@PathVariable Long id) {
        User user = currentUserService.getCurrentUser();
        return PortfolioResponse.from(portfolioService.getOwned(id, user));
    }
}
