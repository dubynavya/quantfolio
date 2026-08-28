package com.quantfolio.backend.portfolio;

import com.quantfolio.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String baseCurrency = "USD";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Portfolio(User owner, String name) {
        this.owner = owner;
        this.name = name;
    }
}
