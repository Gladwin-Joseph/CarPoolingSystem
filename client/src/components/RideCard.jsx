import './RideCard.css'

const formatDate = (iso) => {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleString('en-IE', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export default function RideCard({ ride, onSelect, action = 'Book' }) {
  return (
    <article className="ride-card fade-up" onClick={() => onSelect?.(ride)}>
      <div className="ride-card-route">
        <div className="route-point">
          <span className="route-dot" />
          <div>
            <div className="eyebrow">From</div>
            <div className="route-name serif">{ride.source}</div>
          </div>
        </div>
        <div className="route-line">
          <svg width="100%" height="2" viewBox="0 0 100 2" preserveAspectRatio="none">
            <line x1="0" y1="1" x2="100" y2="1" stroke="currentColor" strokeWidth="1" strokeDasharray="3 3"/>
          </svg>
        </div>
        <div className="route-point end">
          <span className="route-dot accent" />
          <div>
            <div className="eyebrow">To</div>
            <div className="route-name serif">{ride.destination}</div>
          </div>
        </div>
      </div>

      <div className="ride-card-meta">
        <div className="meta-block">
          <div className="eyebrow">Departs</div>
          <div className="meta-value">{formatDate(ride.departureDatetime)}</div>
        </div>
        <div className="meta-block">
          <div className="eyebrow">Seats</div>
          <div className="meta-value mono">{ride.availableSeats}</div>
        </div>
        <div className="meta-block">
          <div className="eyebrow">Price</div>
          <div className="meta-value">€{Number(ride.price).toFixed(2)}</div>
        </div>
        <div className="meta-block status-block">
          <span className={`tag ${ride.status === 'SCHEDULED' ? 'olive' : ride.status === 'CANCELLED' ? 'accent' : 'slate'}`}>
            {ride.status}
          </span>
        </div>
      </div>

      {onSelect && (
        <div className="ride-card-action">
          <span className="action-label">{action}</span>
          <span className="action-arrow">&rarr;</span>
        </div>
      )}
    </article>
  )
}
