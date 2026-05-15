import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import './Auth.css'

export default function Register() {
  const { register } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    name: '',
    username: '',
    email: '',
    password: '',
    phoneNumber: '',
    role: 'PASSENGER',
    // Driver-specific
    licenseNumber: '',
    licensePlate: '',
    carModel: '',
    capacity: 4,
    // Passenger-specific
    govIdNumber: '',
    preferredPaymentMethod: 'CARD',
  })

  const update = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const profile = await register(form)
      toast.success(`Welcome to our CarPooling System, ${profile.name?.split(' ')[0]}!`)
      navigate('/rides')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-art alt">
        <div className="auth-art-inner">
          <div className="eyebrow">New rider</div>
          <h1 className="auth-art-title">
            Join the <span className="italic-serif">CarPooling System</span>
          </h1>
          <div className="register-steps">
            <div className={`reg-step ${step >= 1 ? 'on' : ''}`}>
              <span className="reg-step-num mono">01</span>
              <span>Basics</span>
            </div>
            <div className="reg-step-line" />
            <div className={`reg-step ${step >= 2 ? 'on' : ''}`}>
              <span className="reg-step-num mono">02</span>
              <span>Role</span>
            </div>
            <div className="reg-step-line" />
            <div className={`reg-step ${step >= 3 ? 'on' : ''}`}>
              <span className="reg-step-num mono">03</span>
              <span>Details</span>
            </div>
          </div>
        </div>
      </div>

      <div className="auth-form-wrap">
        <div className="auth-form fade-up">
          <Link to="/" className="auth-back">&larr; Back home</Link>
          <h2 className="serif auth-title">
            {step === 1 && 'Tell us who you are.'}
            {step === 2 && 'How will you use Drift?'}
            {step === 3 && 'A few last details.'}
          </h2>
          <p className="auth-sub">
            Step {step} of 3
          </p>

          <form onSubmit={handleSubmit} className="auth-fields">
            {step === 1 && (
              <>
                <div className="input-group">
                  <label className="input-label">Full name</label>
                  <input className="input" required value={form.name}
                    onChange={(e) => update('name', e.target.value)} placeholder="Jane Doe" autoFocus />
                </div>
                <div className="input-group">
                  <label className="input-label">Username</label>
                  <input className="input" required value={form.username}
                    onChange={(e) => update('username', e.target.value)} placeholder="janedoe" />
                </div>
                <div className="input-group">
                  <label className="input-label">Email</label>
                  <input type="email" className="input" required value={form.email}
                    onChange={(e) => update('email', e.target.value)} placeholder="jane@example.com" />
                </div>
                <div className="input-group">
                  <label className="input-label">Password</label>
                  <input type="password" className="input" required minLength={6} value={form.password}
                    onChange={(e) => update('password', e.target.value)} placeholder="Min 6 characters" />
                </div>
                <div className="input-group">
                  <label className="input-label">Phone (optional)</label>
                  <input className="input" value={form.phoneNumber}
                    onChange={(e) => update('phoneNumber', e.target.value)} placeholder="+353 ..." />
                </div>
                <button type="button" className="btn btn-primary auth-submit"
                  onClick={() => setStep(2)}
                  disabled={!form.name || !form.username || !form.email || !form.password}>
                  Continue →
                </button>
              </>
            )}

            {step === 2 && (
              <>
                <div className="role-grid">
                  <button type="button"
                    className={`role-card ${form.role === 'PASSENGER' ? 'active' : ''}`}
                    onClick={() => update('role', 'PASSENGER')}>
                    <div className="role-num mono">01</div>
                    <div className="role-name serif">Passenger</div>
                    <p className="role-desc">I'm looking for rides. Search routes, book seats, rate drivers.</p>
                  </button>
                  <button type="button"
                    className={`role-card ${form.role === 'DRIVER' ? 'active' : ''}`}
                    onClick={() => update('role', 'DRIVER')}>
                    <div className="role-num mono">02</div>
                    <div className="role-name serif italic-serif">Driver</div>
                    <p className="role-desc">I have a car. Host rides, fill empty seats, earn back fuel costs.</p>
                  </button>
                </div>
                <div className="auth-row">
                  <button type="button" className="btn btn-ghost" onClick={() => setStep(1)}>← Back</button>
                  <button type="button" className="btn btn-primary" onClick={() => setStep(3)}>
                    Continue →
                  </button>
                </div>
              </>
            )}

            {step === 3 && form.role === 'DRIVER' && (
              <>
                <div className="input-group">
                  <label className="input-label">License Number</label>
                  <input className="input" required value={form.licenseNumber}
                    onChange={(e) => update('licenseNumber', e.target.value)} placeholder="DL-2024-001" />
                </div>
                <div className="input-group">
                  <label className="input-label">License Plate</label>
                  <input className="input" required value={form.licensePlate}
                    onChange={(e) => update('licensePlate', e.target.value)} placeholder="192-LK-1234" />
                </div>
                <div className="input-group">
                  <label className="input-label">Car Model</label>
                  <input className="input" required value={form.carModel}
                    onChange={(e) => update('carModel', e.target.value)} placeholder="Toyota Corolla" />
                </div>
                <div className="input-group">
                  <label className="input-label">Capacity (seats)</label>
                  <input type="number" min="1" max="8" className="input" value={form.capacity}
                    onChange={(e) => update('capacity', parseInt(e.target.value) || 4)} />
                </div>
                <div className="auth-row">
                  <button type="button" className="btn btn-ghost" onClick={() => setStep(2)}>← Back</button>
                  <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? <span className="loader light" /> : 'Create account →'}
                  </button>
                </div>
              </>
            )}

            {step === 3 && form.role === 'PASSENGER' && (
              <>
                <div className="input-group">
                  <label className="input-label">Government ID (optional)</label>
                  <input className="input" value={form.govIdNumber}
                    onChange={(e) => update('govIdNumber', e.target.value)} placeholder="ID number" />
                </div>
                <div className="input-group">
                  <label className="input-label">Preferred Payment Method</label>
                  <select className="input" value={form.preferredPaymentMethod}
                    onChange={(e) => update('preferredPaymentMethod', e.target.value)}>
                    <option value="CARD">Card</option>
                    <option value="WALLET">Wallet</option>
                    <option value="BANK_TRANSFER">Bank Transfer</option>
                    <option value="CASH">Cash</option>
                  </select>
                </div>
                <div className="auth-row">
                  <button type="button" className="btn btn-ghost" onClick={() => setStep(2)}>← Back</button>
                  <button type="submit" className="btn btn-primary" disabled={loading}>
                    {loading ? <span className="loader light" /> : 'Create account →'}
                  </button>
                </div>
              </>
            )}
          </form>

          <div className="auth-foot">
            Already have an account? <Link to="/login">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
