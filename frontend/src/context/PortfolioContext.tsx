import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import * as api from "../api/endpoints";
import type { Portfolio } from "../types";
import { useAuth } from "./AuthContext";

interface PortfolioContextValue {
  portfolios: Portfolio[];
  selected: Portfolio | null;
  loading: boolean;
  select: (portfolio: Portfolio) => void;
  refresh: () => Promise<void>;
  create: (name: string) => Promise<void>;
}

const PortfolioContext = createContext<PortfolioContextValue | null>(null);

export function PortfolioProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [portfolios, setPortfolios] = useState<Portfolio[]>([]);
  const [selected, setSelected] = useState<Portfolio | null>(null);
  const [loading, setLoading] = useState(false);

  const refresh = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const list = await api.listPortfolios();
      setPortfolios(list);
      setSelected((current) => current ?? list[0] ?? null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user) {
      refresh();
    } else {
      setPortfolios([]);
      setSelected(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const create = async (name: string) => {
    const created = await api.createPortfolio(name);
    setPortfolios((prev) => [...prev, created]);
    setSelected(created);
  };

  return (
    <PortfolioContext.Provider value={{ portfolios, selected, loading, select: setSelected, refresh, create }}>
      {children}
    </PortfolioContext.Provider>
  );
}

export function usePortfolio() {
  const ctx = useContext(PortfolioContext);
  if (!ctx) throw new Error("usePortfolio must be used within PortfolioProvider");
  return ctx;
}
