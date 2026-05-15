import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { rideApi } from '../api/rideApi'
import { useToast } from '../context/ToastContext'
import RideCard from '../components/RideCard'
import './Rides.css'

export default function Rides() {
  const navigate = useNavigate()
  const toast = useToast()

  const [rides, setRides] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchMode, setSearchMode] = useState(false)
  const [filters, setFilters] = useState({ source: '', destination: '', date: '' })

  const loadAll = async () => {
    setLoading(true)
    try {
      const data = await rideApi.getAllAvailable()
      setRides(data)
      setSearchMode(false)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadAll() }, [])

  const handleSearch = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      // Backend expects ISO LocalDateTime
      const date = filters.date ? `${filters.date}T00:00:00` : new Date().toISOString().split('.')[0]
      const data = await rideApi.search(filters.source, filters.destination, date)
      setRides(data)
      setSearchMode(true)
      toast.info(`Found ${data.length} ride${data.length !== 1 ? 's' : ''}`)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="rides-page">
      <section className="rides-hero">
        <div className="container">
          <div className="eyebrow fade-in">Routes available</div>
          <h1 className="rides-title">
            Find your <span className="italic-serif">next</span> ride.
          </h1>

          <form onSubmit={handleSearch} className="search-bar fade-up">
            <div className="search-field">
              <label className="input-label">From</label>
              <input
                className="input search-input"
                placeholder="City, town, or area"
                value={filters.source}
                onChange={(e) => setFilters({...filters, source: e.target.value})}
                required
              />
            </div>
            <div className="search-divider">→</div>
            <div className="search-field">
              <label className="input-label">To</label>
              <input
                className="input search-input"
                placeholder="Destination"
                value={filters.destination}
                onChange={(e) => setFilters({...filters, destination: e.target.value})}
                required
              />
            </div>
            <div className="search-divider">·</div>
            <div className="search-field">
              <label className="input-label">From Date</label>
              <input
                type="date"
                className="input search-input"
                value={filters.date}
                onChange={(e) => setFilters({...filters, date: e.target.value})}
              />
            </div>
            <button type="submit" className="btn btn-accent search-btn">Search</button>
          </form>

          {searchMode && (
            <button onClick={loadAll} className="reset-link mono">← Show all available rides</button>
          )}
        </div>
      </section>

      <section className="rides-list">
        <div className="container">
          {loading ? (
            <div className="rides-loading">
              <div className="loader" />
              <p className="mono">Fetching rides…</p>
            </div>
          ) : rides.length === 0 ? (
            <div className="rides-empty">
              <div className="empty-mark italic-serif">∅</div>
              <h3 className="serif">No rides matched.</h3>
              <p>Try a different route or date, or come back later.</p>
            </div>
          ) : (
            <div className="rides-grid">
              {rides.map((ride) => (
                <RideCard
                  key={ride.id}
                  ride={ride}
                  onSelect={() => navigate(`/rides/${ride.id}`)}
                />
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
