import { apiClient } from "./client";
import type {
  AuthResponse,
  ForecastResponse,
  Holding,
  MonteCarloResponse,
  Portfolio,
  RiskAlert,
  RiskMetrics,
  TradeRequestDto,
  TradeSide,
} from "../types";

export async function register(email: string, password: string, fullName: string) {
  const { data } = await apiClient.post<AuthResponse>("/api/auth/register", { email, password, fullName });
  return data;
}

export async function login(email: string, password: string) {
  const { data } = await apiClient.post<AuthResponse>("/api/auth/login", { email, password });
  return data;
}

export async function listPortfolios() {
  const { data } = await apiClient.get<Portfolio[]>("/api/portfolios");
  return data;
}

export async function createPortfolio(name: string) {
  const { data } = await apiClient.post<Portfolio>("/api/portfolios", { name });
  return data;
}

export async function listHoldings(portfolioId: number) {
  const { data } = await apiClient.get<Holding[]>(`/api/portfolios/${portfolioId}/holdings`);
  return data;
}

export async function addHolding(portfolioId: number, ticker: string, quantity: number, avgCostPrice: number) {
  const { data } = await apiClient.post<Holding>(`/api/portfolios/${portfolioId}/holdings`, {
    ticker,
    quantity,
    avgCostPrice,
  });
  return data;
}

export async function getRiskMetrics(portfolioId: number) {
  const { data } = await apiClient.get<RiskMetrics>(`/api/portfolios/${portfolioId}/risk`);
  return data;
}

export async function listAlerts(portfolioId: number) {
  const { data } = await apiClient.get<RiskAlert[]>(`/api/portfolios/${portfolioId}/alerts`);
  return data;
}

export async function scanAlertsNow(portfolioId: number) {
  const { data } = await apiClient.post<RiskAlert[]>(`/api/portfolios/${portfolioId}/alerts/scan`);
  return data;
}

export async function listTrades(portfolioId: number) {
  const { data } = await apiClient.get<TradeRequestDto[]>(`/api/portfolios/${portfolioId}/trades`);
  return data;
}

export async function submitTrade(portfolioId: number, ticker: string, side: TradeSide, quantity: number) {
  const { data } = await apiClient.post<TradeRequestDto>(`/api/portfolios/${portfolioId}/trades`, {
    ticker,
    side,
    quantity,
  });
  return data;
}

export async function confirmTrade(portfolioId: number, tradeId: number) {
  const { data } = await apiClient.post<TradeRequestDto>(`/api/portfolios/${portfolioId}/trades/${tradeId}/confirm`);
  return data;
}

export async function rejectTrade(portfolioId: number, tradeId: number) {
  const { data } = await apiClient.post<TradeRequestDto>(`/api/portfolios/${portfolioId}/trades/${tradeId}/reject`);
  return data;
}

export async function getForecast(ticker: string, days: number) {
  const { data } = await apiClient.get<ForecastResponse>(`/api/forecast/${ticker}`, { params: { days } });
  return data;
}

export async function getMonteCarlo(ticker: string, simulations: number, horizonDays: number) {
  const { data } = await apiClient.get<MonteCarloResponse>(`/api/forecast/${ticker}/monte-carlo`, {
    params: { simulations, horizonDays },
  });
  return data;
}
