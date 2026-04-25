import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import './Login.css'

export default function Login() {
  const { login, loading } = useAuth()
  const navigate = useNavigate()
  const [form, setForm]   = useState({ username: '', password: '' })
  const [error, setError] = useState('')

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    const res = await login(form.username, form.password)
    if (res.ok) navigate('/dashboard')
    else setError(res.message)
  }

  return (
    <div className="login-root">
      {/* Left panel — branding */}
      <div className="login-panel">
        <div className="login-panel-content">
          <div className="login-logo">
            <span className="login-logo-mark">A</span>
            <span className="login-logo-name">AgileTool</span>
          </div>
          <h1 className="login-headline">Pilotez vos projets<br />avec clarté.</h1>
          <p className="login-sub">Gestion Agile centralisée — Scrum, Kanban, sprints, backlog et capacité d'équipe en un seul endroit.</p>
          <div className="login-dots">
            {[0,1,2].map(i => <span key={i} className="login-dot" style={{ animationDelay: `${i*.2}s` }} />)}
          </div>
        </div>
        <div className="login-panel-bg" aria-hidden="true">
          <div className="login-blob login-blob-1" />
          <div className="login-blob login-blob-2" />
          <div className="login-grid" />
        </div>
      </div>

      {/* Right panel — form */}
      <div className="login-form-side">
        <form className="login-form" onSubmit={submit} noValidate>
          <div className="login-form-header">
            <h2 className="login-form-title">Connexion</h2>
            <p className="login-form-sub">Accédez à votre espace de travail</p>
          </div>

          {error && (
            <div className="login-error">
              <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {error}
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Nom d'utilisateur</label>
            <input className="form-input" name="username" value={form.username} onChange={handle} placeholder="ex: jdupont" autoFocus required />
          </div>

          <div className="form-group">
            <label className="form-label">Mot de passe</label>
            <input className="form-input" name="password" type="password" value={form.password} onChange={handle} placeholder="••••••••" required />
          </div>

          <button className="btn btn-primary btn-lg login-submit" type="submit" disabled={loading}>
            {loading
              ? <><span className="spinner" style={{ width:18, height:18, borderColor:'rgba(255,255,255,.3)', borderTopColor:'#fff' }} />Connexion…</>
              : 'Se connecter'}
          </button>

          <p className="login-hint">
            Première connexion ? Contactez votre administrateur.
          </p>
        </form>
      </div>
    </div>
  )
}
