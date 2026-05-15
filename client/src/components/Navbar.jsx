import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Navbar.css'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="nav">
      <div className="container nav-inner">
        <Link to="/" className="logo">
          <span className="logo-mark italic-serif">d</span>
          <span className="logo-name serif">CarPooling System</span>
        </Link>

        {user ? (
          <>
            <nav className="nav-links">
              <NavLink to="/rides" className={({isActive}) => isActive ? 'active' : ''}>Find a ride</NavLink>
              {user.role === 'DRIVER' && (
                <NavLink to="/host" className={({isActive}) => isActive ? 'active' : ''}>Host</NavLink>
              )}
              <NavLink to="/bookings" className={({isActive}) => isActive ? 'active' : ''}>Trips</NavLink>
              <NavLink to="/profile" className={({isActive}) => isActive ? 'active' : ''}>Profile</NavLink>
            </nav>
            <div className="nav-right">
              <span className="user-chip">
                <span className="dot" />
                <span className="user-name">{user.name?.split(' ')[0]}</span>
                <span className="user-role">{user.role}</span>
              </span>
              <button onClick={handleLogout} className="btn btn-ghost btn-sm">Sign out</button>
            </div>
          </>
        ) : (
          <div className="nav-right">
            <Link to="/login" className="btn btn-ghost btn-sm">Log in</Link>
            <Link to="/register" className="btn btn-primary btn-sm">Get started</Link>
          </div>
        )}
      </div>
    </header>
  )
}
