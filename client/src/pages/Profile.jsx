import { useState, useEffect } from 'react'
import { userApi } from '../api/userApi'
import { rideApi } from '../api/rideApi'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import './Profile.css'

export default function Profile() {
  const { user, refreshUser } = useAuth()
  const toast = useToast()
  const [editing, setEditing] = useState(false)
  const [saving, setSaving] = useState(false)
  const [avgRating, setAvgRating] = useState(null)
  const [form, setForm] = useState({})

  useEffect(() => {
    if (user) {
      setForm({
        name: user.name || '',
        phoneNumber: user.phoneNumber || '',
        licenseNumber: user.licenseNumber || '',
        licensePlate: user.licensePlate || '',
        carModel: user.carModel || '',
        capacity: user.capacity || 4,
        govIdNumber: user.govIdNumber || '',
        preferredPaymentMethod: user.preferredPaymentMethod || 'CARD',
      })

      rideApi.getAverageRating(user.id)
        .then(r => setAvgRating(r.averageRating))
        .catch(() => {})
    }
  }, [user])

  if (!user) return null

  const update = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const save = async () => {
    setSaving(true)
    try {
      await userApi.update(user.id, form)
      await refreshUser()
      toast.success('Profile updated')
      setEditing(false)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="profile-page">
      <section className="profile-hero">
        <div className="container">
          <div className="profile-id-row">
            <div className="avatar serif">{user.name?.[0]?.toUpperCase()}</div>
            <div>
              <div className="eyebrow">Profile</div>
              <h1 className="profile-name serif">{user.name}</h1>
              <div className="profile-tags">
                <span className="tag slate">{user.role}</span>
                <span className="mono profile-uid">UID #{String(user.id).padStart(4, '0')}</span>
                {avgRating !== null && avgRating > 0 && (
                  <span className="profile-rating">★ {Number(avgRating).toFixed(1)}</span>
                )}
              </div>
            </div>
          </div>

          {!editing && (
            <button onClick={() => setEditing(true)} className="btn btn-ghost">Edit profile</button>
          )}
        </div>
      </section>

      <section className="profile-body">
        <div className="container profile-grid">
          {/* Identity */}
          <div className="profile-card card fade-up">
            <div className="profile-card-head">
              <div className="mono profile-card-num">01</div>
              <h2 className="serif">Identity</h2>
            </div>
            <div className="profile-fields">
              <Field label="Email" value={user.email} mono readOnly />
              <Field label="Username" value={user.username} mono readOnly />
              <Field label="Name"
                     value={editing ? form.name : user.name}
                     editing={editing}
                     onChange={(v) => update('name', v)} />
              <Field label="Phone"
                     value={editing ? form.phoneNumber : user.phoneNumber || '—'}
                     editing={editing}
                     onChange={(v) => update('phoneNumber', v)} />
            </div>
          </div>

          {/* Driver fields */}
          {user.role === 'DRIVER' && (
            <div className="profile-card card fade-up">
              <div className="profile-card-head">
                <div className="mono profile-card-num">02</div>
                <h2 className="serif">Vehicle</h2>
              </div>
              <div className="profile-fields">
                <Field label="License number"
                       value={editing ? form.licenseNumber : user.licenseNumber || '—'}
                       editing={editing}
                       onChange={(v) => update('licenseNumber', v)} mono />
                <Field label="License plate"
                       value={editing ? form.licensePlate : user.licensePlate || '—'}
                       editing={editing}
                       onChange={(v) => update('licensePlate', v)} mono />
                <Field label="Car model"
                       value={editing ? form.carModel : user.carModel || '—'}
                       editing={editing}
                       onChange={(v) => update('carModel', v)} />
                <Field label="Capacity"
                       value={editing ? form.capacity : user.capacity || '—'}
                       editing={editing}
                       type="number"
                       onChange={(v) => update('capacity', parseInt(v) || 4)} mono />
              </div>
            </div>
          )}

          {/* Passenger fields */}
          {user.role === 'PASSENGER' && (
            <div className="profile-card card fade-up">
              <div className="profile-card-head">
                <div className="mono profile-card-num">02</div>
                <h2 className="serif">Preferences</h2>
              </div>
              <div className="profile-fields">
                <Field label="Government ID"
                       value={editing ? form.govIdNumber : user.govIdNumber || '—'}
                       editing={editing}
                       onChange={(v) => update('govIdNumber', v)} mono />
                <div className="profile-field">
                  <span className="input-label">Preferred Payment</span>
                  {editing ? (
                    <select className="input"
                            value={form.preferredPaymentMethod}
                            onChange={(e) => update('preferredPaymentMethod', e.target.value)}>
                      <option value="CARD">Card</option>
                      <option value="WALLET">Wallet</option>
                      <option value="BANK_TRANSFER">Bank Transfer</option>
                      <option value="CASH">Cash</option>
                    </select>
                  ) : (
                    <span className="profile-field-value">{user.preferredPaymentMethod || '—'}</span>
                  )}
                </div>
              </div>
            </div>
          )}
        </div>

        {editing && (
          <div className="container">
            <div className="profile-edit-actions">
              <button onClick={() => setEditing(false)} className="btn btn-ghost" disabled={saving}>
                Discard
              </button>
              <button onClick={save} className="btn btn-primary" disabled={saving}>
                {saving ? <span className="loader light" /> : 'Save changes →'}
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  )
}

function Field({ label, value, editing, onChange, mono, readOnly, type = 'text' }) {
  return (
    <div className="profile-field">
      <span className="input-label">{label}</span>
      {editing && !readOnly ? (
        <input
          type={type}
          className="input"
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
      ) : (
        <span className={`profile-field-value ${mono ? 'mono' : ''}`}>{value}</span>
      )}
    </div>
  )
}
