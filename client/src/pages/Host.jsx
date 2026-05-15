import { useState, useEffect } from 'react'
import { rideApi } from '../api/rideApi'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import RideCard from '../components/RideCard'
import './Host.css'

export default function Host() {
  const { user } = useAuth()
  const toast = useToast()

  const [tab, setTab] = useState('create')
  const [hosted, setHosted] = useState([])
  const [loading, setLoading] = useState(false)
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState({
    source: '',
    destination: '',
    departureDatetime: '',
    arrivalDatetime: '',
    price: '',
    availableSeats: 3,
  })

  const update = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const loadHosted = async () => {
    setLoading(true)
    try {
      const data = await rideApi.getByDriver(user.id)
      setHosted(data.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0)))
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { if (tab === 'manage') loadHosted() }, [tab])

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreating(true)
    try {
      await rideApi.create({
        driverId: user.id,
        source: form.source,
        destination: form.destination,
        departureDatetime: form.departureDatetime,
        arrivalDatetime: form.arrivalDatetime || null,
        price: parseFloat(form.price),
        availableSeats: parseInt(form.availableSeats),
      })
      toast.success('Ride published. Passengers can now find it.')
      setForm({ source: '', destination: '', departureDatetime: '', arrivalDatetime: '', price: '', availableSeats: 3 })
      setTab('manage')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setCreating(false)
    }
  }

  const handleCancel = async (rideId) => {
    if (!confirm('Cancel this ride? Active bookings will also be cancelled.')) return
    try {
      await rideApi.cancel(rideId)
      toast.success('Ride cancelled')
      loadHosted()
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <div className="host-page">
      <section className="host-hero">
        <div className="container">
          <div className="eyebrow">Driver workspace</div>
          <h1 className="host-title">
            Host a <span className="italic-serif">ride.</span>
          </h1>
          <p className="host-sub">Fill empty seats. Earn back fuel costs. Reduce traffic, one trip at a time.</p>

          <div className="host-tabs">
            <button onClick={() => setTab('create')} className={`host-tab ${tab === 'create' ? 'active' : ''}`}>
              <span className="mono">01</span> Create
            </button>
            <button onClick={() => setTab('manage')} className={`host-tab ${tab === 'manage' ? 'active' : ''}`}>
              <span className="mono">02</span> Manage
            </button>
          </div>
        </div>
      </section>

      <section className="host-body">
        <div className="container">
          {tab === 'create' && (
            <form onSubmit={handleCreate} className="host-form fade-up">
              <div className="form-row">
                <div className="input-group">
                  <label className="input-label">Source</label>
                  <input className="input" required value={form.source}
                    onChange={(e) => update('source', e.target.value)} placeholder="Limerick" />
                </div>
                <div className="input-group">
                  <label className="input-label">Destination</label>
                  <input className="input" required value={form.destination}
                    onChange={(e) => update('destination', e.target.value)} placeholder="Dublin" />
                </div>
              </div>

              <div className="form-row">
                <div className="input-group">
                  <label className="input-label">Departure date & time</label>
                  <input type="datetime-local" className="input" required value={form.departureDatetime}
                    onChange={(e) => update('departureDatetime', e.target.value)} />
                </div>
                <div className="input-group">
                  <label className="input-label">Arrival (optional)</label>
                  <input type="datetime-local" className="input" value={form.arrivalDatetime}
                    onChange={(e) => update('arrivalDatetime', e.target.value)} />
                </div>
              </div>

              <div className="form-row">
                <div className="input-group">
                  <label className="input-label">Price per seat (€)</label>
                  <input type="number" step="0.01" min="0.01" className="input" required value={form.price}
                    onChange={(e) => update('price', e.target.value)} placeholder="15.00" />
                </div>
                <div className="input-group">
                  <label className="input-label">Available seats</label>
                  <input type="number" min="1" max="8" className="input" required value={form.availableSeats}
                    onChange={(e) => update('availableSeats', e.target.value)} />
                </div>
              </div>

              <button type="submit" className="btn btn-primary host-submit" disabled={creating}>
                {creating ? <span className="loader light" /> : 'Publish ride →'}
              </button>
            </form>
          )}

          {tab === 'manage' && (
            <div className="host-manage">
              {loading ? (
                <div className="rides-loading"><div className="loader" /></div>
              ) : hosted.length === 0 ? (
                <div className="rides-empty">
                  <div className="empty-mark italic-serif">∅</div>
                  <h3 className="serif">No rides hosted yet.</h3>
                  <p>Switch to Create to publish your first one.</p>
                </div>
              ) : (
                <div className="rides-grid">
                  {hosted.map((ride) => (
                    <div key={ride.id} className="host-ride-row">
                      <RideCard ride={ride} />
                      {ride.status === 'SCHEDULED' && (
                        <button onClick={() => handleCancel(ride.id)} className="cancel-btn">
                          Cancel ride
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
