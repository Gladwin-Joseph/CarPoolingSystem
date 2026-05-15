import './Footer.css'

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-left">
          <div className="footer-mark italic-serif">d</div>
          <div>
            <div className="serif footer-title">Carpooling System</div>
          </div>
        </div>
        <div className="footer-right">
          <div className="mono footer-meta">
            <span>v1.0</span>
            <span className="dot-sep">•</span>
            <span>Built for CS6652</span>
            <span className="dot-sep">•</span>
            <span>University of Limerick</span>
          </div>
        </div>
      </div>
    </footer>
  )
}
