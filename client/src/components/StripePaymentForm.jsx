import { useState } from 'react'
import { PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js'
import { useToast } from '../context/ToastContext'

export default function StripePaymentForm({ amount, onSuccess }) {
  const stripe = useStripe()
  const elements = useElements()
  const toast = useToast()
  const [processing, setProcessing] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!stripe || !elements) return

    setProcessing(true)

    const { error, paymentIntent } = await stripe.confirmPayment({
      elements,
      redirect: 'if_required',
    })

    if (error) {
      toast.error(error.message)
      setProcessing(false)
      return
    }

    if (paymentIntent && paymentIntent.status === 'succeeded') {
      toast.success('Payment successful! Bon voyage!')
      onSuccess?.(paymentIntent)
    } else {
      toast.info('Payment processing...')
    }

    setProcessing(false)
  }

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      <PaymentElement />
      <button
        type="submit"
        disabled={!stripe || processing}
        className="btn btn-accent book-btn"
        style={{ width: '100%' }}
      >
        {processing ? <span className="loader light" /> : `Pay €${Number(amount).toFixed(2)} →`}
      </button>
      <p style={{ fontSize: '11px', color: 'var(--slate)', textAlign: 'center', marginTop: '8px' }}>
        Test card: <span className="mono">4242 4242 4242 4242</span> · any future date · any CVC
      </p>
    </form>
  )
}