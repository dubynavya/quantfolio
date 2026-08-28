package com.quantfolio.backend.portfolio;

import com.quantfolio.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public Portfolio create(User owner, String name) {
        return portfolioRepository.save(new Portfolio(owner, name));
    }

    public List<Portfolio> listForOwner(User owner) {
        return portfolioRepository.findByOwner(owner);
    }

    public Portfolio getOwned(Long id, User owner) {
        return portfolioRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found"));
    }
}
