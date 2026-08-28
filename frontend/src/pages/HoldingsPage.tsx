import { useEffect, useState } from "react";
import * as api from "../api/endpoints";
import { usePortfolio } from "../context/PortfolioContext";
import type { Holding } from "../types";

export function HoldingsPage() {
  const { selected } = usePortfolio();
  const [holdings, setHoldings] = useState<Holding[]>([]);
  const [ticker, setTicker] = useState("");
  const [quantity, setQuantity] = useState("");
  const [avgCostPrice, setAvgCostPrice] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    if (!selected) return;
    setHoldings(await api.listHoldings(selected.id));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selected]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected) return;
    setError(null);
    setSubmitting(true);
    try {
      await api.addHolding(selected.id, ticker.trim().toUpperCase(), Number(quantity), Number(avgCostPrice));
      setTicker("");
      setQuantity("");
      setAvgCostPrice("");
      await load();
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!selected) return <div className="page">Create a portfolio first.</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Holdings — {selected.name}</h1>
      </div>

      <div className="panel">
        <h3>Record an existing position</h3>
        <p className="muted">
          Use this to seed a starting position directly. New buys/sells that require risk sign-off go through the
          Trades page instead.
        </p>
        <form className="inline-form" onSubmit={handleSubmit}>
          <input placeholder="Ticker (e.g. AAPL)" value={ticker} onChange={(e) => setTicker(e.target.value)} required />
          <input
            placeholder="Quantity"
            type="number"
            step="any"
            min="0"
            value={quantity}
            onChange={(e) => setQuantity(e.target.value)}
            required
          />
          <input
            placeholder="Avg cost price"
            type="number"
            step="any"
            min="0"
            value={avgCostPrice}
            onChange={(e) => setAvgCostPrice(e.target.value)}
            required
          />
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Adding..." : "Add holding"}
          </button>
        </form>
        {error && <div className="form-error">{error}</div>}
      </div>

      <div className="panel">
        <h3>Current holdings</h3>
        <table className="data-table">
          <thead>
            <tr>
              <th>Ticker</th>
              <th>Quantity</th>
              <th>Avg cost</th>
            </tr>
          </thead>
          <tbody>
            {holdings.map((h) => (
              <tr key={h.id}>
                <td>{h.ticker}</td>
                <td>{h.quantity}</td>
                <td>${Number(h.avgCostPrice).toFixed(2)}</td>
              </tr>
            ))}
            {holdings.length === 0 && (
              <tr>
                <td colSpan={3} className="muted">No holdings yet.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
