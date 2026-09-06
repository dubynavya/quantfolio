import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [slowStart, setSlowStart] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    setSlowStart(false);
    const slowStartTimer = setTimeout(() => setSlowStart(true), 6000);
    try {
      await register(email, password, fullName);
      navigate("/");
    } catch (err) {
      setError((err as Error).message);
    } finally {
      clearTimeout(slowStartTimer);
      setSubmitting(false);
      setSlowStart(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Create your account</h1>
        <p className="auth-subtitle">Start tracking portfolio risk in minutes</p>
        <label>Full name</label>
        <input required value={fullName} onChange={(e) => setFullName(e.target.value)} />
        <label>Email</label>
        <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        <label>Password</label>
        <input type="password" required minLength={8} value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <div className="form-error">{error}</div>}
        {slowStart && (
          <div className="muted" style={{ marginTop: 8 }}>
            The server's waking up from idle (free hosting tier) — this can take up to a minute
            on the first request. Hang tight.
          </div>
        )}
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? "Creating account..." : "Create account"}
        </button>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </form>
    </div>
  );
}
