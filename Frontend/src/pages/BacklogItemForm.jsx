import { useState, useEffect } from 'react'
import Modal from '../components/ui/Modal'

const EMPTY = { title: '', description: '', type: 'USER_STORY', priority: 'MEDIUM', estimatedHours: '', storyPoints: '' }

export default function BacklogItemForm({ open, onClose, onSubmit, initial, loading, projects = [] }) {
  const [form, setForm] = useState(EMPTY)

  useEffect(() => {
    setForm(initial ? { ...EMPTY, ...initial } : EMPTY)
  }, [initial, open])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  const submit = e => { e.preventDefault(); onSubmit(form) }
  const isEdit = !!initial?.id

  return (
    <Modal
      open={open} onClose={onClose}
      title={isEdit ? 'Modifier l\'item' : 'Nouvel item de backlog'}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>Annuler</button>
          <button type="submit" form="backlog-form" className="btn btn-primary" disabled={loading}>
            {loading && <span className="spinner" style={{ width:16,height:16,borderColor:'rgba(255,255,255,.3)',borderTopColor:'#fff' }} />}
            {isEdit ? 'Enregistrer' : 'Créer'}
          </button>
        </>
      }
    >
      <form id="backlog-form" onSubmit={submit} style={{ display:'flex', flexDirection:'column', gap:14 }}>
        <div className="form-group">
          <label className="form-label">Titre *</label>
          <input className="form-input" name="title" value={form.title} onChange={handle} placeholder="ex : En tant qu'utilisateur, je veux…" required />
        </div>

        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="form-textarea" name="description" value={form.description} onChange={handle} placeholder="Critères d'acceptation, contexte…" />
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Type</label>
            <select className="form-select" name="type" value={form.type} onChange={handle}>
              <option value="USER_STORY">User Story</option>
              <option value="BUG">Bug</option>
              <option value="TECH_TASK">Tâche technique</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Priorité</label>
            <select className="form-select" name="priority" value={form.priority} onChange={handle}>
              <option value="CRITICAL">Critique</option>
              <option value="HIGH">Haute</option>
              <option value="MEDIUM">Moyenne</option>
              <option value="LOW">Basse</option>
            </select>
          </div>
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Story points</label>
            <input className="form-input" type="number" min="0" name="storyPoints" value={form.storyPoints} onChange={handle} placeholder="ex : 5" />
          </div>
          <div className="form-group">
            <label className="form-label">Heures estimées</label>
            <input className="form-input" type="number" min="0" step="0.5" name="estimatedHours" value={form.estimatedHours} onChange={handle} placeholder="ex : 8" />
          </div>
        </div>
      </form>
    </Modal>
  )
}
