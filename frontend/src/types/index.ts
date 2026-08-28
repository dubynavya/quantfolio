export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
}

export interface Portfolio {
  id: number;
  name: string;
  baseCurrency: string;
  createdAt: string;
}

export interface Holding {
  id: number;
  ticker: string;
  quantity: number;
  avgCostPrice: number;
}

export interface HoldingWeight {
  ticker: string;
  marketValue: number;
  weight: number;
}

export interface DatedValue {
  date: string;
  value: number;
}

export interface RiskMetrics {
  totalValue: number;
  annualizedReturn: number;
  annualizedVolatility: number;
  sharpeRatio: number;
  maxDrawdown: number;
  valueAtRisk95: number;
  beta: number;
  holdingWeights: HoldingWeight[];
  equityCurve: DatedValue[];
}

export type TradeSide = "BUY" | "SELL";
export type TradeStatus = "AUTO_APPROVED" | "PENDING_CONFIRMATION" | "CONFIRMED" | "REJECTED";

export interface TradeRequestDto {
  id: number;
  ticker: string;
  side: TradeSide;
  quantity: number;
  estimatedPrice: number;
  notionalValue: number;
  requiredLevel: number;
  status: TradeStatus;
  reasons: string[];
  createdAt: string;
  decidedAt: string | null;
}

export type RiskAlertType = "HIGH_VOLATILITY" | "DEEP_DRAWDOWN" | "HIGH_CONCENTRATION";

export interface RiskAlert {
  id: number;
  type: RiskAlertType;
  message: string;
  createdAt: string;
}

export interface ForecastPoint {
  date: string;
  close?: number;
  predicted?: number;
  lower95?: number;
  upper95?: number;
}

export interface ForecastResponse {
  ticker: string;
  model: string;
  history: { date: string; close: number }[];
  forecast: { date: string; predicted: number; lower95: number; upper95: number }[];
}

export interface MonteCarloResponse {
  ticker: string;
  ticker_current_price: number;
  simulations: number;
  horizon_days: number;
  confidence: number;
  value_at_risk_pct: number;
  value_at_risk_dollar: number;
  conditional_value_at_risk_pct: number;
  conditional_value_at_risk_dollar: number;
  expected_price: number;
  percentiles: { p5: number; p25: number; p50: number; p75: number; p95: number };
  sample_paths: number[][];
}
