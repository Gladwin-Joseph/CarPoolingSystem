import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { userApi } from '../api/userApi'

const AuthContext = createContext(null)

// Decode JWT payload (no signature verification — server already does that)
const decodeJwt = (token) => {
  try {
    const payload = token.split('.')[1]
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(decoded)
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // On boot, restore session from localStorage
  useEffect(() => {
    const token = localStorage.getItem('drift_token')
    if (!token) { setLoading(false); return }

    const claims = decodeJwt(token)
    if (!claims || claims.exp * 1000 < Date.now()) {
      localStorage.removeItem('drift_token')
      setLoading(false)
      return
    }

    // Hydrate user profile
    userApi.getById(claims.userId)
      .then(profile => setUser(profile))
      .catch(() => localStorage.removeItem('drift_token'))
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (email, password) => {
    const { token } = await userApi.login(email, password)
    localStorage.setItem('drift_token', token)
    const claims = decodeJwt(token)
    const profile = await userApi.getById(claims.userId)
    setUser(profile)
    return profile
  }, [])

  const register = useCallback(async (data) => {
    await userApi.register(data)
    return await login(data.email, data.password)
  }, [login])

  const logout = useCallback(() => {
    localStorage.removeItem('drift_token')
    setUser(null)
  }, [])

  const refreshUser = useCallback(async () => {
    if (!user) return
    const fresh = await userApi.getById(user.id)
    setUser(fresh)
  }, [user])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
