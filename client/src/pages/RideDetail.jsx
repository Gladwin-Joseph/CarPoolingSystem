import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { rideApi } from '../api/rideApi'
import { paymentApi } from '../api/paymentApi'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import StripeProvider from '../components/StripeProvider'
import StripePaymentForm from '../components/StripePaymentForm'
import './RideDetail.css'

const formatDate = (iso) => {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('en-IE', {
    weekday: 'long', day: 'numeric', month: 'long', hour: '2-digit', minute: '2-digit'
  })
}

export default function RideDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const toast = useToast()
  const { user } = useAuth()

  const [ride, setRide] = useState(null)
  const [seats, setSeats] = useState(1)
  const [loading, setLoading] = useState(true)
  const [booking, setBooking] = useState(false)
  const [showPayment, setShowPayment] = useState(false)
  const [bookingResult, setBookingResult] = useState(null)
  const [paymentMethod, setPaymentMethod] = useState('CARD')
  const [paymentInfo, setPaymentInfo] = useState(null)
  const [processing, setProcessing] = useState(false)

  useEffect(() => {
    rideApi.getById(id)
      .then(setRide)
      .catch((err) => { toast.error(err.message); navigate('/rides') })
      .finally(() => setLoading(false))
  }, [id])

  const handleBook = async () => {
    if (user.role !== 'PASSENGER') {
      toast.error('Only passengers can book rides')
      return
    }
    setBooking(true)
    try {
      const result = await rideApi.book({
        userId: user.id,
        rideId: ride.id,
        bookedSeats: seats,
      })
      setBookingResult(result)
      setShowPayment(true)
      toast.success('Booking confirmed! Complete payment below.')
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBooking(false)
    }
  }

  // Step 1: Initiate payment (creates Stripe PaymentIntent on backend)
  const handleInitiatePayment = async () => {
    setProcessing(true)
    try {
      const result = await paymentApi.process({
        bookingId: bookingResult.id,
        payerUserId: user.id,
        amount: bookingResult.amount,
        paymentMethod,
      })
      setPaymentInfo(result)

      // For non-card methods, payment is already complete
      if (paymentMethod !== 'CARD') {
        toast.success('Payment complete. Bon voyage!')
        navigate('/bookings')
      }
      // For CARD, the StripePaymentForm will appear and let user pay
    } catch (err) {
      toast.error(err.message)
      setProcessing(false)
    }
  }

  // Step 2: After Stripe processes the card successfully
  const handleStripeSuccess = () => {
    navigate('/bookings')
  }

  if (loading) {
    return <div className="detail-loading"><div className="loader" /></div>
  }
  if (!ride) return null

  const total = (Number(ride.price) * seats).toFixed(2)
  const canBook = ride.status === 'SCHEDULED' && ride.availableSeats >= seats && user.role === 'PASSENGER'

  return (
    <div className="detail-page">
      <div className="container">
        <button onClick={() => navigate(-1)} className="back-link mono">← Back to rides</button>

        <div className="detail-grid">
          <div className="detail-main fade-up">
            <div className="detail-eyebrow">
              <span className="eyebrow">Ride #{String(ride.id).padStart(4, '0')}</span>
              <span className={`tag ${ride.status === 'SCHEDULED' ? 'olive' : ride.status === 'CANCELLED' ? 'accent' : 'slate'}`}>
                {ride.status}
              </span>
            </div>

            <h1 className="detail-title">
              <span className="serif">{ride.source}</span>
              <span className="detail-arrow italic-serif">to</span>
              <span className="serif italic-serif">{ride.destination}</span>
            </h1>

            <div className="detail-meta">
              <div className="meta-cell"><div className="eyebrow">Departs</div><div className="meta-cell-value">{formatDate(ride.departureDatetime)}</div></div>
              <div className="meta-cell"><div className="eyebrow">Arrives</div><div className="meta-cell-value">{formatDate(ride.arrivalDatetime)}</div></div>
              <div className="meta-cell"><div className="eyebrow">Driver ID</div><div className="meta-cell-value mono">#{ride.driverId}</div></div>
              <div className="meta-cell"><div className="eyebrow">Available seats</div><div className="meta-cell-value mono">{ride.availableSeats}</div></div>
              <div className="meta-cell"><div className="eyebrow">Price per seat</div><div className="meta-cell-value">€{Number(ride.price).toFixed(2)}</div></div>
              <div className="meta-cell"><div className="eyebrow">Listed</div><div className="meta-cell-value mono">{ride.createdAt ? new Date(ride.createdAt).toLocaleDateString('en-IE') : '—'}</div></div>
            </div>
          </div>

          <aside className="detail-side fade-up">
            {!showPayment ? (
              // ── Booking widget ──
              <div className="book-widget">
                <div className="eyebrow">Reserve your seat</div>
                <div className="book-price serif">€{total}</div>
                <div className="book-price-sub">{seats} × €{Number(ride.price).toFixed(2)}</div>

                <div className="seat-picker">
                  <span className="input-label">Seats</span>
                  <div className="seat-counter">
                    <button onClick={() => setSeats(Math.max(1, seats - 1))} disabled={seats <= 1}>−</button>
                    <span className="seat-num mono">{seats}</span>
                    <button onClick={() => setSeats(Math.min(ride.availableSeats, seats + 1))} disabled={seats >= ride.availableSeats}>+</button>
                  </div>
                </div>

                {!canBook && user.role === 'DRIVER' && (
                  <div className="book-warning">Drivers cannot book rides.</div>
                )}
                {!canBook && ride.status !== 'SCHEDULED' && (
                  <div className="book-warning">This ride is no longer available.</div>
                )}

                <button onClick={handleBook} disabled={!canBook || booking} className="btn btn-primary book-btn">
                  {booking ? <span className="loader light" /> : 'Confirm booking →'}
                </button>
                <p className="book-fine mono">No charge until you confirm payment.</p>
              </div>
            ) : !paymentInfo ? (
              // ── Payment method picker ──
              <div className="book-widget pay-widget">
                <div className="pay-status">
                  <span className="pay-tick">✓</span>
                  <span className="eyebrow">Booking confirmed</span>
                </div>
                <div className="pay-amount serif">€{Number(bookingResult.amount).toFixed(2)}</div>
                <div className="pay-ref mono">REF · {String(bookingResult.id).padStart(6, '0')}</div>

                <div className="pay-methods">
                  <div className="input-label">Pay with</div>
                  {['CARD','WALLET','BANK_TRANSFER','CASH'].map(m => (
                    <button key={m}
                            type="button"
                            className={`pay-method ${paymentMethod === m ? 'active' : ''}`}
                            onClick={() => setPaymentMethod(m)}>
                      {m.replace('_',' ')}
                    </button>
                  ))}
                </div>

                <button onClick={handleInitiatePayment} disabled={processing} className="btn btn-accent book-btn">
                  {processing ? <span className="loader light" /> : 'Continue →'}
                </button>
              </div>
            ) : paymentMethod === 'CARD' && paymentInfo.stripeClientSecret ? (
              // ── Stripe card form ──
              <div className="book-widget pay-widget">
                <div className="eyebrow">Enter your card details</div>
                <div className="pay-amount serif" style={{marginBottom: 24}}>€{Number(bookingResult.amount).toFixed(2)}</div>
                <StripeProvider clientSecret={paymentInfo.stripeClientSecret}>
                  <StripePaymentForm amount={bookingResult.amount} onSuccess={handleStripeSuccess} />
                </StripeProvider>
              </div>
            ) : null}
          </aside>
        </div>
      </div>
    </div>
  )
}