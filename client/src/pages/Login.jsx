import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import './Auth.css'

export default function Login() {
  const { login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)

  const from = location.state?.from?.pathname || '/rides'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const profile = await login(email, password)
      toast.success(`Welcome back, ${profile.name?.split(' ')[0]}`)
      navigate(from, { replace: true })
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-art">
        <div className="auth-art-inner">
          <div className="eyebrow">Welcome back</div>
          <h1 className="auth-art-title">
            Sign in to <span className="italic-serif">CarPooling System</span>
          </h1>
          <div className="auth-art-quote serif">
            "The journey itself is my home."
            <span className="auth-art-attr">— Bashō</span>
          </div>
        </div>
      </div>

      <div className="auth-form-wrap">
        <div className="auth-form fade-up">
          <Link to="/" className="auth-back">&larr; Back home</Link>
          <h2 className="serif auth-title">Welcome back.</h2>
          <p className="auth-sub">Enter your credentials to continue.</p>

          <form onSubmit={handleSubmit} className="auth-fields">
            <div className="input-group">
              <label className="input-label">Email</label>
              <input
                type="email"
                required
                className="input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                autoFocus
              />
            </div>
            <div className="input-group">
              <label className="input-label">Password</label>
              <input
                type="password"
                required
                className="input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Your password"
              />
            </div>
            <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
              {loading ? <span className="loader light" /> : 'Sign in →'}
            </button>
          </form>

          <div className="auth-foot">
            New here? <Link to="/register">Create an account</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
