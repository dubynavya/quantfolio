import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { PortfolioSwitcher } from "./PortfolioSwitcher";

const linkClass = ({ isActive }: { isActive: boolean }) => (isActive ? "nav-link active" : "nav-link");

export function Navbar() {
  const { user, logout } = useAuth();
  if (!user) return null;

  return (
    <header className="navbar">
      <div className="navbar-brand">QuantFolio</div>
      <nav className="navbar-links">
        <NavLink to="/" className={linkClass} end>Dashboard</NavLink>
        <NavLink to="/holdings" className={linkClass}>Holdings</NavLink>
        <NavLink to="/trades" className={linkClass}>Trades</NavLink>
        <NavLink to="/forecast" className={linkClass}>Forecast</NavLink>
      </nav>
      <div className="navbar-right">
        <PortfolioSwitcher />
        <span className="navbar-user">{user.fullName}</span>
        <button className="btn btn-ghost" onClick={logout}>Log out</button>
      </div>
    </header>
  );
}
