package com.quantfolio.backend.portfolio;

import com.quantfolio.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByOwner(User owner);
    Optional<Portfolio> findByIdAndOwner(Long id, User owner);
}
