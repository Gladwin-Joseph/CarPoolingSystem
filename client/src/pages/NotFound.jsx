import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '70vh',
      padding: '40px',
      textAlign: 'center',
    }}>
      <div className="italic-serif" style={{ fontSize: '180px', lineHeight: 1, color: 'var(--paper-deep)' }}>404</div>
      <h1 className="serif" style={{ fontSize: '40px', marginTop: '24px', fontWeight: 400 }}>
        Wrong turn.
      </h1>
      <p style={{ color: 'var(--slate)', maxWidth: '400px', margin: '12px 0 28px' }}>
        We couldn't find the page you're looking for. Maybe a passenger got off too early.
      </p>
      <Link to="/" className="btn btn-primary">Back to home →</Link>
    </div>
  )
}
