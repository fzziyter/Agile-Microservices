import { useState, useEffect } from 'react'
import Modal from '../../components/ui/Modal'

const EMPTY = { username: '', password: '', role: 'DEVELOPER' }

export default function UserForm({ open, onClose, onSubmit, initial, loading }) {
  const [form, setForm] = useState(EMPTY)

  useEffect(() => {
    setForm(initial ? { ...EMPTY, ...initial, password: '' } : EMPTY)
  }, [initial, open])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  const submit = e => { e.preventDefault(); onSubmit(form) }
  const isEdit = !!initial?.id

  return (
    <Modal
      open={open} onClose={onClose}
      title={isEdit ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur'}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>Annuler</button>
          <button type="submit" form="user-form" className="btn btn-primary" disabled={loading}>
            {loading && <span className="spinner" style={{ width:16,height:16,borderColor:'rgba(255,255,255,.3)',borderTopColor:'#fff' }} />}
            {isEdit ? 'Enregistrer' : 'Créer'}
          </button>
        </>
      }
    >
      <form id="user-form" onSubmit={submit} style={{ display:'flex', flexDirection:'column', gap:16 }}>
        <div className="form-group">
          <label className="form-label">Nom d'utilisateur *</label>
          <input className="form-input" name="username" value={form.username} onChange={handle} placeholder="ex: jdupont" required autoFocus />
        </div>

        <div className="form-group">
          <label className="form-label">{isEdit ? 'Nouveau mot de passe (laisser vide pour conserver)' : 'Mot de passe *'}</label>
          <input className="form-input" type="password" name="password" value={form.password} onChange={handle} placeholder="••••••••" required={!isEdit} />
        </div>

        <div className="form-group">
          <label className="form-label">Rôle</label>
          <select className="form-select" name="role" value={form.role} onChange={handle}>
            <option value="ADMIN">Administrateur</option>
            <option value="PRODUCT_OWNER">Product Owner</option>
            <option value="SCRUM_MASTER">Scrum Master</option>
            <option value="DEVELOPER">Développeur</option>
            <option value="MANAGER">Manager</option>
          </select>
        </div>
      </form>
    </Modal>
  )
}
