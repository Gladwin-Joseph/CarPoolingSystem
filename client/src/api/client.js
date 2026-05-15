import axios from 'axios'

// All requests go through API Gateway on port 8080
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000
})

// Attach JWT token from localStorage to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('drift_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Friendly error messages
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const msg = err.response?.data?.message
              || err.response?.data?.error
              || err.message
              || 'Something went wrong'
    return Promise.reject(new Error(msg))
  }
)

export default api
