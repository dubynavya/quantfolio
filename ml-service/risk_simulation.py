"""Monte Carlo Value-at-Risk via Geometric Brownian Motion.

Drift (mu) and volatility (sigma) are estimated from historical daily log returns, then used to
simulate many possible future price paths. This is a standard quant-risk technique and a useful
complement to the backend's *historical* VaR (risk/RiskMath.java): historical VaR only sees
scenarios that actually happened, Monte Carlo VaR also covers unseen-but-plausible paths implied
by the fitted return distribution.
"""

import numpy as np
import pandas as pd


def simulate_var(history: pd.DataFrame, simulations: int, horizon_days: int, confidence: float = 0.95) -> dict:
    close = history["close"]
    if len(close) < 30:
        raise ValueError("Need at least 30 days of history to estimate drift/volatility")

    log_returns = np.diff(np.log(close.values))
    mu = float(np.mean(log_returns))
    sigma = float(np.std(log_returns))
    s0 = float(close.iloc[-1])

    rng = np.random.default_rng(seed=None)
    z = rng.standard_normal((simulations, horizon_days))
    daily_log_returns = (mu - 0.5 * sigma ** 2) + sigma * z
    cumulative_log_returns = np.cumsum(daily_log_returns, axis=1)
    paths = s0 * np.exp(cumulative_log_returns)

    final_prices = paths[:, -1]
    pnl = final_prices - s0
    pnl_pct = pnl / s0

    tail = 1.0 - confidence
    var_pct = float(-np.quantile(pnl_pct, tail))
    var_dollar = max(0.0, var_pct * s0)

    worst_tail = pnl_pct[pnl_pct <= np.quantile(pnl_pct, tail)]
    cvar_pct = float(-np.mean(worst_tail)) if len(worst_tail) > 0 else var_pct
    cvar_dollar = max(0.0, cvar_pct * s0)

    percentiles = {
        "p5": round(float(np.quantile(final_prices, 0.05)), 4),
        "p25": round(float(np.quantile(final_prices, 0.25)), 4),
        "p50": round(float(np.quantile(final_prices, 0.50)), 4),
        "p75": round(float(np.quantile(final_prices, 0.75)), 4),
        "p95": round(float(np.quantile(final_prices, 0.95)), 4),
    }

    sample_size = min(25, simulations)
    sample_paths = paths[:sample_size].round(4).tolist()

    return {
        "ticker_current_price": round(s0, 4),
        "estimated_daily_drift": mu,
        "estimated_daily_volatility": sigma,
        "simulations": simulations,
        "horizon_days": horizon_days,
        "confidence": confidence,
        "value_at_risk_pct": round(var_pct * 100, 3),
        "value_at_risk_dollar": round(var_dollar, 4),
        "conditional_value_at_risk_pct": round(cvar_pct * 100, 3),
        "conditional_value_at_risk_dollar": round(cvar_dollar, 4),
        "expected_price": round(float(np.mean(final_prices)), 4),
        "percentiles": percentiles,
        "sample_paths": sample_paths,
    }
