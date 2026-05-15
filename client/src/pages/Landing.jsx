import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import './Landing.css'

export default function Landing() {
  const { user } = useAuth()

  return (
    <div className="landing">
      <section className="hero">
        <div className="container hero-grid">
          <div className="hero-text">
            <div className="eyebrow fade-in">A carpooling platform &mdash; Issue 01</div>
            <h1 className="hero-title">
              <span className="hero-title-line">The road,</span>
              <span className="hero-title-line italic-serif">shared</span>
              <span className="hero-title-line">beautifully.</span>
            </h1>
            <p className="hero-lede">
              Our CarPooling System connects drivers with empty seats to passengers heading the same way.
              Fewer cars on the road, lower costs, better commutes; built on a
              foundation of verified profiles and clean architecture.
            </p>
            <div className="hero-actions">
              {user ? (
                <Link to="/rides" className="btn btn-primary">Find a ride &rarr;</Link>
              ) : (
                <>
                  <Link to="/register" className="btn btn-primary">Get started &rarr;</Link>
                  <Link to="/login" className="btn btn-ghost">I have an account</Link>
                </>
              )}
            </div>
          </div>

          <div className="hero-visual">
            <div className="ticket card">
              <div className="ticket-stub">
                <div className="eyebrow">Sample Ride</div>
                <div className="ticket-route">
                  <div className="serif">Limerick</div>
                  <div className="ticket-arrow">&mdash;&mdash;&rarr;</div>
                  <div className="serif italic-serif">Dublin</div>
                </div>
                <div className="ticket-meta">
                  <div>
                    <div className="eyebrow">Departs</div>
                    <div className="mono">08:00 &middot; Tue</div>
                  </div>
                  <div>
                    <div className="eyebrow">Seats</div>
                    <div className="mono">3 left</div>
                  </div>
                  <div>
                    <div className="eyebrow">Fare</div>
                    <div className="mono">€15.00</div>
                  </div>
                </div>
              </div>
              <div className="ticket-perforation" />
              <div className="ticket-tear">
                <span className="eyebrow">Built by</span>
                <span className="serif" style={{fontSize:'18px'}}>Kiran,Gladwin,Chirag and Ganesh</span>
                <span className="ticket-rating">★ 4.9</span>
              </div>
            </div>
            <div className="hero-numeral italic-serif">01</div>
          </div>
        </div>
      </section>

      <section className="manifesto">
        <div className="container">
          <div className="manifesto-row">
            <div className="manifesto-num mono">01</div>
            <div>
              <h2 className="serif manifesto-h">Verified hosts, real trust.</h2>
              <p className="manifesto-p">
                Every host driver verifies their license and vehicle. Passengers see who they're riding with
                before they tap "book"; no surprises, no anonymity.
              </p>
            </div>
          </div>
          <div className="manifesto-row">
            <div className="manifesto-num mono">02</div>
            <div>
              <h2 className="serif manifesto-h">Search, book, ride, rate.</h2>
              <p className="manifesto-p">
                Find rides by route and date. Confirm in two taps. Pay digitally. Rate the experience
                afterwards. The whole loop, without friction.
              </p>
            </div>
          </div>
          <div className="manifesto-row">
            <div className="manifesto-num mono">03</div>
            <div>
              <h2 className="serif manifesto-h">Architected to scale.</h2>
              <p className="manifesto-p">
                Microservices behind an API gateway, JWT-stateless auth, circuit breakers between every hop.
                The plumbing is built to outlast the demo.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="stats">
        <div className="container stats-grid">
          <div className="stat">
            <div className="stat-num serif">3</div>
            <div className="stat-lbl">Microservices</div>
          </div>
          <div className="stat">
            <div className="stat-num serif italic-serif">JWT</div>
            <div className="stat-lbl">Stateless auth</div>
          </div>
          <div className="stat">
            <div className="stat-num serif">∞</div>
            <div className="stat-lbl">Horizontally scalable</div>
          </div>
          <div className="stat">
            <div className="stat-num serif italic-serif">5★</div>
            <div className="stat-lbl">Two-way ratings</div>
          </div>
        </div>
      </section>

      <section className="cta">
        <div className="container cta-inner">
          <div>
            <div className="eyebrow" style={{color: 'var(--paper)', opacity: 0.7}}>Begin</div>
            <h2 className="cta-title serif">
              Ready to share <span className="italic-serif">the road?</span>
            </h2>
          </div>
          <Link to={user ? "/rides" : "/register"} className="btn btn-accent">
            {user ? 'Find a ride' : 'Create an account'} &rarr;
          </Link>
        </div>
      </section>
    </div>
  )
}
