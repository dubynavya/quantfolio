import { useState } from "react";
import { usePortfolio } from "../context/PortfolioContext";

export function PortfolioSwitcher() {
  const { portfolios, selected, select, create } = usePortfolio();
  const [creating, setCreating] = useState(false);
  const [name, setName] = useState("");

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    await create(name.trim());
    setName("");
    setCreating(false);
  };

  if (creating) {
    return (
      <form className="portfolio-create-form" onSubmit={handleCreate}>
        <input
          autoFocus
          placeholder="Portfolio name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button type="submit" className="btn btn-primary btn-sm">Add</button>
        <button type="button" className="btn btn-ghost btn-sm" onClick={() => setCreating(false)}>Cancel</button>
      </form>
    );
  }

  return (
    <div className="portfolio-switcher">
      <select
        value={selected?.id ?? ""}
        onChange={(e) => {
          const p = portfolios.find((p) => p.id === Number(e.target.value));
          if (p) select(p);
        }}
      >
        {portfolios.length === 0 && <option value="">No portfolios yet</option>}
        {portfolios.map((p) => (
          <option key={p.id} value={p.id}>{p.name}</option>
        ))}
      </select>
      <button className="btn btn-ghost btn-sm" onClick={() => setCreating(true)}>+ New</button>
    </div>
  );
}
