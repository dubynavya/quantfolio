interface MetricCardProps {
  label: string;
  value: string;
  tone?: "neutral" | "good" | "bad";
  hint?: string;
}

export function MetricCard({ label, value, tone = "neutral", hint }: MetricCardProps) {
  return (
    <div className={`metric-card tone-${tone}`}>
      <div className="metric-label">{label}</div>
      <div className="metric-value">{value}</div>
      {hint && <div className="metric-hint">{hint}</div>}
    </div>
  );
}
