import './StatCard.css'

export default function StatCard({ label, value, icon, color = 'blue', delta }) {
  return (
    <div className={`stat-card stat-card--${color}`}>
      <div className="stat-card__icon">{icon}</div>
      <div className="stat-card__body">
        <p className="stat-card__value">{value ?? '—'}</p>
        <p className="stat-card__label">{label}</p>
        {delta !== undefined && (
          <p className={`stat-card__delta ${delta >= 0 ? 'pos' : 'neg'}`}>
            {delta >= 0 ? '↑' : '↓'} {Math.abs(delta)}%
          </p>
        )}
      </div>
    </div>
  )
}
