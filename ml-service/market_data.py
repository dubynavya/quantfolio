"""Fetches daily OHLCV history from Yahoo Finance's public (key-less) chart endpoint.

Mirrors backend/.../marketdata/MarketDataService.java so both services agree on where price
data comes from without either needing an API key that a recruiter cloning the repo would have
to go sign up for.
"""

import time
from functools import lru_cache

import pandas as pd
import requests

_HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        "(KHTML, like Gecko) Chrome/120.0 Safari/537.36"
    )
}

_CACHE_TTL_SECONDS = 6 * 60 * 60
_cache: dict[str, tuple[float, pd.DataFrame]] = {}


class TickerNotFoundError(Exception):
    pass


def get_history(ticker: str, range_: str = "1y") -> pd.DataFrame:
    """Returns a DataFrame indexed by date with an 'close' column, oldest first."""
    key = f"{ticker.upper()}:{range_}"
    cached = _cache.get(key)
    if cached and (time.time() - cached[0]) < _CACHE_TTL_SECONDS:
        return cached[1]

    df = _fetch(ticker.upper(), range_)
    _cache[key] = (time.time(), df)
    return df


def _fetch(ticker: str, range_: str) -> pd.DataFrame:
    url = f"https://query1.finance.yahoo.com/v8/finance/chart/{ticker}"
    try:
        resp = requests.get(url, params={"range": range_, "interval": "1d"}, headers=_HEADERS, timeout=10)
        resp.raise_for_status()
        payload = resp.json()
    except Exception as exc:  # noqa: BLE001 - surfaced to the caller as a clean 502/404
        raise TickerNotFoundError(f"Could not fetch market data for {ticker}: {exc}") from exc

    result = payload.get("chart", {}).get("result")
    if not result:
        raise TickerNotFoundError(f"Unknown ticker or no data available: {ticker}")

    result = result[0]
    timestamps = result.get("timestamp") or []
    closes = result["indicators"]["quote"][0].get("close") or []

    if not timestamps or not closes:
        raise TickerNotFoundError(f"Unknown ticker or no data available: {ticker}")

    dates = pd.to_datetime(timestamps, unit="s").normalize()
    df = pd.DataFrame({"close": closes}, index=dates)
    df = df.dropna()

    if df.empty:
        raise TickerNotFoundError(f"Unknown ticker or no data available: {ticker}")

    return df
