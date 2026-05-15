import { useState, useEffect } from 'react'
import { rideApi } from '../api/rideApi'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import './Bookings.css'

const formatDate = (iso) => {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('en-IE', {
    weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
  })
}

export default function Bookings() {
  const { user } = useAuth()
  const toast = useToast()
  const [bookings, setBookings] = useState([])
  const [rides, setRides] = useState({})
  const [loading, setLoading] = useState(true)
  const [ratingFor, setRatingFor] = useState(null)
  const [ratingValue, setRatingValue] = useState(5)
  const [ratingComment, setRatingComment] = useState('')

  const load = async () => {
    setLoading(true)
    try {
      const data = await rideApi.getBookingHistory(user.id)
      const sorted = data.sort((a, b) => new Date(b.bookingDatetime) - new Date(a.bookingDatetime))
      setBookings(sorted)

      // Fetch ride details for each unique ride
      const rideIds = [...new Set(sorted.map(b => b.rideId))]
      const rideMap = {}
      await Promise.all(rideIds.map(async (rid) => {
        try { rideMap[rid] = await rideApi.getById(rid) } catch {}
      }))
      setRides(rideMap)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const handleCancel = async (bookingId) => {
    if (!confirm('Cancel this booking? Your seat will be released.')) return
    try {
      await rideApi.cancelBooking(bookingId)
      toast.success('Booking cancelled')
      load()
    } catch (err) {
      toast.error(err.message)
    }
  }

  const submitRating = async () => {
    if (!ratingFor) return
    try {
      await rideApi.rate({
        rideId: ratingFor.rideId,
        ratedByUserId: user.id,
        ratedUserId: rides[ratingFor.rideId]?.driverId,
        rating: ratingValue,
        userType: 'DRIVER',
        comment: ratingComment,
      })
      toast.success('Rating submitted. Thanks!')
      setRatingFor(null)
      setRatingComment('')
      setRatingValue(5)
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <div className="bookings-page">
      <section className="bookings-hero">
        <div className="container">
          <div className="eyebrow">Your trips</div>
          <h1 className="bookings-title">
            Travel <span className="italic-serif">log.</span>
          </h1>
        </div>
      </section>

      <section className="bookings-body">
        <div className="container">
          {loading ? (
            <div className="rides-loading"><div className="loader" /></div>
          ) : bookings.length === 0 ? (
            <div className="rides-empty">
              <div className="empty-mark italic-serif">∅</div>
              <h3 className="serif">No trips yet.</h3>
              <p>Find a ride and your travel log will start filling up.</p>
            </div>
          ) : (
            <div className="bookings-list">
              {bookings.map((b) => {
                const ride = rides[b.rideId] || {}
                return (
                  <article key={b.id} className="booking-row card fade-up">
                    <div className="booking-left">
                      <div className="booking-status">
                        <span className={`tag ${b.status === 'CONFIRMED' ? 'olive' : b.status === 'CANCELLED' ? 'accent' : 'slate'}`}>
                          {b.status}
                        </span>
                        <span className="mono booking-id">REF · {String(b.id).padStart(6, '0')}</span>
                      </div>
                      <div className="booking-route serif">
                        {ride.source || '—'} <span className="b-arrow italic-serif">to</span> {ride.destination || '—'}
                      </div>
                      <div className="booking-meta">
                        <span><span className="eyebrow">Departs</span> {formatDate(ride.departureDatetime)}</span>
                        <span><span className="eyebrow">Booked on</span> {formatDate(b.bookingDatetime)}</span>
                        <span><span className="eyebrow">Seats</span> <span className="mono">{b.bookedSeats}</span></span>
                      </div>
                    </div>
                    <div className="booking-right">
                      <div className="booking-amount">
                        <span className="eyebrow">Total</span>
                        <span className="amount-num serif">€{Number(b.amount).toFixed(2)}</span>
                      </div>
                      <div className="booking-actions">
                        {b.status === 'CONFIRMED' && (
                          <button onClick={() => handleCancel(b.id)} className="btn btn-ghost btn-sm">
                            Cancel
                          </button>
                        )}
                        {b.status === 'CONFIRMED' && (
                          <button onClick={() => setRatingFor(b)} className="btn btn-primary btn-sm">
                            Rate driver
                          </button>
                        )}
                      </div>
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </div>
      </section>

      {/* Rating Modal */}
      {ratingFor && (
        <div className="modal-overlay" onClick={() => setRatingFor(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="eyebrow">Rate your ride</div>
            <h3 className="serif modal-title">How was the trip?</h3>

            <div className="star-row">
              {[1,2,3,4,5].map(n => (
                <button key={n} onClick={() => setRatingValue(n)} className={`star ${n <= ratingValue ? 'on' : ''}`}>
                  ★
                </button>
              ))}
            </div>

            <div className="input-group" style={{marginTop: 24}}>
              <label className="input-label">Comment (optional)</label>
              <textarea className="input" rows="3" value={ratingComment}
                onChange={(e) => setRatingComment(e.target.value)} placeholder="How was the journey?" />
            </div>

            <div className="modal-actions">
              <button onClick={() => setRatingFor(null)} className="btn btn-ghost">Cancel</button>
              <button onClick={submitRating} className="btn btn-primary">Submit rating →</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
