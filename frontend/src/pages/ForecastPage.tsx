import { useState } from "react";
import {
  Area,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import * as api from "../api/endpoints";
import type { ForecastResponse, MonteCarloResponse } from "../types";

interface ChartPoint {
  date: string;
  close?: number;
  predicted?: number;
  band?: [number, number];
}

export function ForecastPage() {
  const [ticker, setTicker] = useState("AAPL");
  const [days, setDays] = useState(10);
  const [forecast, setForecast] = useState<ForecastResponse | null>(null);
  const [monteCarlo, setMonteCarlo] = useState<MonteCarloResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const run = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const [f, mc] = await Promise.all([
        api.getForecast(ticker.trim().toUpperCase(), days),
        api.getMonteCarlo(ticker.trim().toUpperCase(), 2000, days),
      ]);
      setForecast(f);
      setMonteCarlo(mc);
    } catch (err) {
      setError((err as Error).message);
      setForecast(null);
      setMonteCarlo(null);
    } finally {
      setLoading(false);
    }
  };

  const chartData: ChartPoint[] = forecast
    ? [
        ...forecast.history.map((h) => ({ date: h.date, close: h.close })),
        ...forecast.forecast.map((f) => ({
          date: f.date,
          predicted: f.predicted,
          band: [f.lower95, f.upper95] as [number, number],
        })),
      ]
    : [];

  return (
    <div className="page">
      <div className="page-header">
        <h1>Forecast &amp; Monte Carlo Risk</h1>
      </div>

      <div className="panel">
        <form className="inline-form" onSubmit={run}>
          <input placeholder="Ticker (e.g. AAPL)" value={ticker} onChange={(e) => setTicker(e.target.value)} required />
          <input
            type="number"
            min={1}
            max={60}
            value={days}
            onChange={(e) => setDays(Number(e.target.value))}
          />
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "Running..." : "Run forecast"}
          </button>
        </form>
        {error && <div className="form-error">{error}</div>}
      </div>

      {forecast && (
        <div className="panel">
          <h3>{forecast.ticker} — {forecast.model}</h3>
          <ResponsiveContainer width="100%" height={320}>
            <ComposedChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} minTickGap={40} />
              <YAxis tick={{ fontSize: 11 }} domain={["auto", "auto"]} />
              <Tooltip />
              <Legend />
              <Area dataKey="band" stroke="none" fill="#5b8def" fillOpacity={0.15} name="95% interval" />
              <Line type="monotone" dataKey="close" stroke="#5b8def" strokeWidth={2} dot={false} name="Historical close" />
              <Line
                type="monotone"
                dataKey="predicted"
                stroke="#f0a35c"
                strokeWidth={2}
                strokeDasharray="5 4"
                dot={false}
                name="Forecast"
              />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      )}

      {monteCarlo && (
        <div className="panel">
          <h3>Monte Carlo VaR — {monteCarlo.simulations.toLocaleString()} simulated paths, {monteCarlo.horizon_days}-day horizon</h3>
          <div className="metric-grid">
            <div className="metric-card tone-neutral">
              <div className="metric-label">Current price</div>
              <div className="metric-value">${monteCarlo.ticker_current_price.toFixed(2)}</div>
            </div>
            <div className="metric-card tone-neutral">
              <div className="metric-label">Expected price</div>
              <div className="metric-value">${monteCarlo.expected_price.toFixed(2)}</div>
            </div>
            <div className="metric-card tone-bad">
              <div className="metric-label">VaR (95%)</div>
              <div className="metric-value">{monteCarlo.value_at_risk_pct.toFixed(1)}%</div>
              <div className="metric-hint">${monteCarlo.value_at_risk_dollar.toFixed(2)} / share</div>
            </div>
            <div className="metric-card tone-bad">
              <div className="metric-label">CVaR (95%)</div>
              <div className="metric-value">{monteCarlo.conditional_value_at_risk_pct.toFixed(1)}%</div>
              <div className="metric-hint">${monteCarlo.conditional_value_at_risk_dollar.toFixed(2)} / share</div>
            </div>
          </div>
          <p className="muted">
            5th/50th/95th percentile of simulated price after {monteCarlo.horizon_days} days: $
            {monteCarlo.percentiles.p5.toFixed(2)} / ${monteCarlo.percentiles.p50.toFixed(2)} / $
            {monteCarlo.percentiles.p95.toFixed(2)}
          </p>
        </div>
      )}
    </div>
  );
}
