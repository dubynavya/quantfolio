from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware

import forecasting
import risk_simulation
from market_data import TickerNotFoundError, get_history

app = FastAPI(
    title="QuantFolio ML Service",
    description="Price forecasting and Monte Carlo risk simulation for the QuantFolio backend.",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET"],
    allow_headers=["*"],
)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/forecast/{ticker}")
def forecast(ticker: str, days: int = Query(default=10, ge=1, le=60)):
    try:
        history = get_history(ticker, range_="1y")
        return {"ticker": ticker.upper(), **forecasting.forecast_prices(history, days)}
    except TickerNotFoundError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc


@app.get("/risk/monte-carlo/{ticker}")
def monte_carlo(
    ticker: str,
    simulations: int = Query(default=1000, ge=100, le=20000),
    horizon_days: int = Query(default=20, ge=1, le=252),
):
    try:
        history = get_history(ticker, range_="1y")
        return {"ticker": ticker.upper(), **risk_simulation.simulate_var(history, simulations, horizon_days)}
    except TickerNotFoundError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc)) from exc
