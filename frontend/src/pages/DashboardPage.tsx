import { useEffect, useState } from "react";
import {
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import * as api from "../api/endpoints";
import { MetricCard } from "../components/MetricCard";
import { usePortfolio } from "../context/PortfolioContext";
import type { RiskAlert, RiskMetrics } from "../types";

const PIE_COLORS = ["#5b8def", "#7c5cf0", "#f0a35c", "#5cc9f0", "#f05c8d", "#8dd35c"];

function formatCurrency(value: number) {
  return value.toLocaleString("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 0 });
}

function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`;
}

export function DashboardPage() {
  const { selected, portfolios, loading } = usePortfolio();
  const [metrics, setMetrics] = useState<RiskMetrics | null>(null);
  const [alerts, setAlerts] = useState<RiskAlert[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const load = async () => {
    if (!selected) return;
    setError(null);
    setBusy(true);
    try {
      const [metricsRes, alertsRes] = await Promise.all([
        api.getRiskMetrics(selected.id),
        api.listAlerts(selected.id),
      ]);
      setMetrics(metricsRes);
      setAlerts(alertsRes);
    } catch (err) {
      setMetrics(null);
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected]);

  const scanNow = async () => {
    if (!selected) return;
    const newAlerts = await api.scanAlertsNow(selected.id);
    if (newAlerts.length > 0) {
      setAlerts((prev) => [...newAlerts, ...prev]);
    }
  };

  if (loading) return <div className="page">Loading portfolios...</div>;

  if (portfolios.length === 0) {
    return (
      <div className="page">
        <div className="empty-state">
          <h2>Create your first portfolio</h2>
          <p>Use the "+ New" button in the top bar to get started, then add holdings.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>{selected?.name}</h1>
        <button className="btn btn-ghost btn-sm" onClick={scanNow}>Scan for risk alerts</button>
      </div>

      {error && <div className="form-error">{error}</div>}
      {busy && !metrics && <div className="muted">Computing risk metrics from live market data...</div>}

      {metrics && (
        <>
          <div className="metric-grid">
            <MetricCard label="Total value" value={formatCurrency(metrics.totalValue)} />
            <MetricCard
              label="Annualized return"
              value={formatPercent(metrics.annualizedReturn)}
              tone={metrics.annualizedReturn >= 0 ? "good" : "bad"}
            />
            <MetricCard label="Volatility (ann.)" value={formatPercent(metrics.annualizedVolatility)} />
            <MetricCard
              label="Sharpe ratio"
              value={metrics.sharpeRatio.toFixed(2)}
              tone={metrics.sharpeRatio >= 1 ? "good" : metrics.sharpeRatio < 0 ? "bad" : "neutral"}
            />
            <MetricCard label="Max drawdown" value={formatPercent(metrics.maxDrawdown)} tone="bad" />
            <MetricCard
              label="VaR (95%, 1-day)"
              value={formatCurrency(metrics.valueAtRisk95)}
              hint="historical simulation"
              tone="bad"
            />
            <MetricCard label="Beta vs S&P 500" value={metrics.beta.toFixed(2)} />
          </div>

          <div className="panel-grid">
            <div className="panel">
              <h3>Equity curve</h3>
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={metrics.equityCurve}>
                  <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" />
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} minTickGap={40} />
                  <YAxis tick={{ fontSize: 11 }} domain={["auto", "auto"]} />
                  <Tooltip formatter={(v) => formatCurrency(Number(v))} />
                  <Line type="monotone" dataKey="value" stroke="#5b8def" strokeWidth={2} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="panel">
              <h3>Allocation</h3>
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={metrics.holdingWeights}
                    dataKey="weight"
                    nameKey="ticker"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={2}
                  >
                    {metrics.holdingWeights.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v) => formatPercent(Number(v))} />
                </PieChart>
              </ResponsiveContainer>
              <ul className="legend-list">
                {metrics.holdingWeights.map((w, i) => (
                  <li key={w.ticker}>
                    <span className="legend-dot" style={{ background: PIE_COLORS[i % PIE_COLORS.length] }} />
                    {w.ticker} — {formatPercent(w.weight)}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </>
      )}

      <div className="panel">
        <h3>Risk alerts</h3>
        {alerts.length === 0 && <p className="muted">No alerts. Run a scan or check back after the nightly job.</p>}
        <ul className="alert-list">
          {alerts.map((a) => (
            <li key={a.id} className={`alert-item alert-${a.type.toLowerCase()}`}>
              <span className="alert-type">{a.type.replace(/_/g, " ")}</span>
              <span>{a.message}</span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
