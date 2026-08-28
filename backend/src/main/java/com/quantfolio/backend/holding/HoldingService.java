package com.quantfolio.backend.holding;

import com.quantfolio.backend.portfolio.Portfolio;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    public List<Holding> list(Portfolio portfolio) {
        return holdingRepository.findByPortfolio(portfolio);
    }

    public Optional<Holding> find(Portfolio portfolio, String ticker) {
        return holdingRepository.findByPortfolioAndTicker(portfolio, ticker.trim().toUpperCase());
    }

    public void reduce(Portfolio portfolio, String ticker, BigDecimal quantity) {
        String normalizedTicker = ticker.trim().toUpperCase();
        Holding holding = holdingRepository.findByPortfolioAndTicker(portfolio, normalizedTicker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No existing position in " + normalizedTicker + " to sell"));

        if (holding.getQuantity().compareTo(quantity) < 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Cannot sell " + quantity + " shares of " + normalizedTicker
                            + " — only " + holding.getQuantity() + " held");
        }

        BigDecimal remaining = holding.getQuantity().subtract(quantity);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            holdingRepository.delete(holding);
        } else {
            holding.setQuantity(remaining);
            holdingRepository.save(holding);
        }
    }

    public Holding addOrMerge(Portfolio portfolio, String ticker, BigDecimal quantity, BigDecimal price) {
        String normalizedTicker = ticker.trim().toUpperCase();
        return holdingRepository.findByPortfolioAndTicker(portfolio, normalizedTicker)
                .map(existing -> mergeIntoExisting(existing, quantity, price))
                .orElseGet(() -> holdingRepository.save(new Holding(portfolio, normalizedTicker, quantity, price)));
    }

    private Holding mergeIntoExisting(Holding existing, BigDecimal quantity, BigDecimal price) {
        BigDecimal newQuantity = existing.getQuantity().add(quantity);
        // Weighted-average cost basis across the old and newly-added lots.
        BigDecimal existingCostBasis = existing.getQuantity().multiply(existing.getAvgCostPrice());
        BigDecimal addedCostBasis = quantity.multiply(price);
        BigDecimal newAvgCost = existingCostBasis.add(addedCostBasis)
                .divide(newQuantity, 6, RoundingMode.HALF_UP);

        existing.setQuantity(newQuantity);
        existing.setAvgCostPrice(newAvgCost);
        return holdingRepository.save(existing);
    }
}
