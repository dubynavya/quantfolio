"""Short-horizon price forecasting using Holt's linear trend (double exponential smoothing).

Holt's method is chosen over a heavier model (ARIMA, LSTM) deliberately: daily equity closes are
close to a random walk, so a simple, well-understood trend model is honest about how much signal
is actually there, while still being a real statistical forecast (not a straight-line extrapolation)
with a defensible confidence interval derived from in-sample residuals.
"""

import numpy as np
import pandas as pd
from statsmodels.tsa.holtwinters import Holt


def forecast_prices(history: pd.DataFrame, days: int) -> dict:
    close = history["close"]
    if len(close) < 20:
        raise ValueError("Need at least 20 days of history to fit a forecast")

    model = Holt(close, initialization_method="estimated").fit(optimized=True)
    forecast_values = model.forecast(days)

    residuals = model.fittedvalues - close
    residual_std = float(np.std(residuals))

    last_date = close.index[-1]
    forecast_dates = pd.bdate_range(start=last_date, periods=days + 1)[1:]

    forecast_points = []
    for i, (date, value) in enumerate(zip(forecast_dates, forecast_values), start=1):
        # Widen the interval with sqrt(horizon) the way a random-walk error bound would grow.
        margin = 1.96 * residual_std * np.sqrt(i)
        forecast_points.append({
            "date": date.strftime("%Y-%m-%d"),
            "predicted": round(float(value), 4),
            "lower95": round(float(value - margin), 4),
            "upper95": round(float(value + margin), 4),
        })

    history_points = [
        {"date": d.strftime("%Y-%m-%d"), "close": round(float(v), 4)}
        for d, v in close.tail(120).items()
    ]

    return {
        "model": "Holt linear trend (double exponential smoothing)",
        "history": history_points,
        "forecast": forecast_points,
    }
