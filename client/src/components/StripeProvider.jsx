import { Elements } from '@stripe/react-stripe-js'
import { loadStripe } from '@stripe/stripe-js'

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY)

export default function StripeProvider({ clientSecret, children }) {
  if (!clientSecret) return null

  const options = {
    clientSecret,
    appearance: {
      theme: 'stripe',
      variables: {
        colorPrimary: '#d94f2e',
        colorBackground: '#f5f1e8',
        colorText: '#0a0a0a',
        fontFamily: 'Inter Tight, system-ui, sans-serif',
        borderRadius: '4px',
      }
    }
  }

  return (
    <Elements stripe={stripePromise} options={options}>
      {children}
    </Elements>
  )
}