package com.quantfolio.backend.trade;

import com.quantfolio.backend.portfolio.Portfolio;
import com.quantfolio.backend.portfolio.PortfolioService;
import com.quantfolio.backend.user.CurrentUserService;
import com.quantfolio.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.quantfolio.backend.trade.TradeDtos.*;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/trades")
public class TradeController {

    private final TradeService tradeService;
    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    public TradeController(TradeService tradeService, PortfolioService portfolioService,
                            CurrentUserService currentUserService) {
        this.tradeService = tradeService;
        this.portfolioService = portfolioService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<TradeResponse> list(@PathVariable Long portfolioId) {
        return tradeService.list(ownedPortfolio(portfolioId)).stream().map(TradeResponse::from).toList();
    }

    @PostMapping
    public TradeResponse submit(@PathVariable Long portfolioId, @Valid @RequestBody SubmitTradeRequest request) {
        Portfolio portfolio = ownedPortfolio(portfolioId);
        TradeRequest trade = tradeService.submit(portfolio, request.ticker(), request.side(), request.quantity());
        return TradeResponse.from(trade);
    }

    @PostMapping("/{tradeId}/confirm")
    public TradeResponse confirm(@PathVariable Long portfolioId, @PathVariable Long tradeId) {
        Portfolio portfolio = ownedPortfolio(portfolioId);
        return TradeResponse.from(tradeService.confirm(portfolio, tradeId));
    }

    @PostMapping("/{tradeId}/reject")
    public TradeResponse reject(@PathVariable Long portfolioId, @PathVariable Long tradeId) {
        Portfolio portfolio = ownedPortfolio(portfolioId);
        return TradeResponse.from(tradeService.reject(portfolio, tradeId));
    }

    private Portfolio ownedPortfolio(Long portfolioId) {
        User user = currentUserService.getCurrentUser();
        return portfolioService.getOwned(portfolioId, user);
    }
}
