import { useEffect, useState } from "react";
import * as api from "../api/endpoints";
import { usePortfolio } from "../context/PortfolioContext";
import type { TradeRequestDto, TradeSide } from "../types";

const STATUS_LABEL: Record<TradeRequestDto["status"], string> = {
  AUTO_APPROVED: "Auto-approved",
  PENDING_CONFIRMATION: "Needs your confirmation",
  CONFIRMED: "Confirmed",
  REJECTED: "Rejected",
};

export function TradePage() {
  const { selected } = usePortfolio();
  const [trades, setTrades] = useState<TradeRequestDto[]>([]);
  const [ticker, setTicker] = useState("");
  const [side, setSide] = useState<TradeSide>("BUY");
  const [quantity, setQuantity] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    if (!selected) return;
    setTrades(await api.listTrades(selected.id));
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
      await api.submitTrade(selected.id, ticker.trim().toUpperCase(), side, Number(quantity));
      setTicker("");
      setQuantity("");
      await load();
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setSubmitting(false);
    }
  };

  const decide = async (tradeId: number, action: "confirm" | "reject") => {
    if (!selected) return;
    if (action === "confirm") await api.confirmTrade(selected.id, tradeId);
    else await api.rejectTrade(selected.id, tradeId);
    await load();
  };

  if (!selected) return <div className="page">Create a portfolio first.</div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Trades — {selected.name}</h1>
      </div>

      <div className="panel">
        <h3>Submit a trade</h3>
        <p className="muted">
          Every trade is evaluated by the approval engine against live notional value and resulting portfolio
          concentration. Small, low-concentration trades auto-approve; larger or riskier ones need your explicit
          confirmation before they touch your holdings.
        </p>
        <form className="inline-form" onSubmit={handleSubmit}>
          <select value={side} onChange={(e) => setSide(e.target.value as TradeSide)}>
            <option value="BUY">Buy</option>
            <option value="SELL">Sell</option>
          </select>
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
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Evaluating..." : "Submit"}
          </button>
        </form>
        {error && <div className="form-error">{error}</div>}
      </div>

      <div className="panel">
        <h3>Trade history</h3>
        <ul className="trade-list">
          {trades.map((t) => (
            <li key={t.id} className={`trade-item status-${t.status.toLowerCase()}`}>
              <div className="trade-item-header">
                <span className="trade-side">{t.side}</span>
                <span>{t.quantity} {t.ticker} @ ${Number(t.estimatedPrice).toFixed(2)}</span>
                <span className="trade-notional">${Number(t.notionalValue).toLocaleString()}</span>
                <span className={`trade-status status-${t.status.toLowerCase()}`}>
                  L{t.requiredLevel} · {STATUS_LABEL[t.status]}
                </span>
              </div>
              {t.reasons.length > 0 && (
                <ul className="trade-reasons">
                  {t.reasons.map((r, i) => <li key={i}>{r}</li>)}
                </ul>
              )}
              {t.status === "PENDING_CONFIRMATION" && (
                <div className="trade-actions">
                  <button className="btn btn-primary btn-sm" onClick={() => decide(t.id, "confirm")}>Confirm</button>
                  <button className="btn btn-ghost btn-sm" onClick={() => decide(t.id, "reject")}>Reject</button>
                </div>
              )}
            </li>
          ))}
          {trades.length === 0 && <li className="muted">No trades yet.</li>}
        </ul>
      </div>
    </div>
  );
}
