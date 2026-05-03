import { useState, useEffect } from 'react'
import Modal from '../components/ui/Modal'

const EMPTY = {
  title: '',
  description: '',
  type: 'USER_STORY',
  priority: 'MEDIUM',
  status: 'TODO',
  estimatedHours: '',
  storyPoints: '',
  assignedToId: '',
  sprintId: '',
}

export default function BacklogItemForm({ open, onClose, onSubmit, initial, loading, users = [] }) {
  const [form, setForm] = useState(EMPTY)

  useEffect(() => {
    setForm(initial ? { ...EMPTY, ...initial } : EMPTY)
  }, [initial, open])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const submit = e => {
    e.preventDefault()
    onSubmit({
      ...form,
      estimatedHours: form.estimatedHours === '' ? null : Number(form.estimatedHours),
      storyPoints: form.storyPoints === '' ? null : Number(form.storyPoints),
      assignedToId: form.assignedToId === '' ? null : Number(form.assignedToId),
      sprintId: form.sprintId === '' ? null : Number(form.sprintId),
    })
  }

  const isEdit = !!initial?.id

  return (
    <Modal
      open={open} onClose={onClose}
      title={isEdit ? "Modifier l'item" : 'Nouvel item de backlog'}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>Annuler</button>
          <button type="submit" form="backlog-form" className="btn btn-primary" disabled={loading}>
            {loading && <span className="spinner" style={{ width:16,height:16,borderColor:'rgba(255,255,255,.3)',borderTopColor:'#fff' }} />}
            {isEdit ? 'Enregistrer' : 'Creer'}
          </button>
        </>
      }
    >
      <form id="backlog-form" onSubmit={submit} style={{ display:'flex', flexDirection:'column', gap:14 }}>
        <div className="form-group">
          <label className="form-label">Titre *</label>
          <input className="form-input" name="title" value={form.title} onChange={handle} placeholder="ex : En tant qu'utilisateur, je veux..." required />
        </div>

        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="form-textarea" name="description" value={form.description} onChange={handle} placeholder="Criteres d'acceptation, contexte..." />
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Type</label>
            <select className="form-select" name="type" value={form.type} onChange={handle}>
              <option value="USER_STORY">User Story</option>
              <option value="BUG">Bug</option>
              <option value="TECHNICAL_TASK">Tache technique</option>
              <option value="QA">QA</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Priorite</label>
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
            <label className="form-label">Statut</label>
            <select className="form-select" name="status" value={form.status} onChange={handle}>
              <option value="TODO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="DONE">Done</option>
              <option value="BLOCKED">Blocked</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Assigne a ID</label>
            {users.length > 0 ? (
              <select className="form-select" name="assignedToId" value={form.assignedToId ?? ''} onChange={handle}>
                <option value="">Non assigne</option>
                {users.map(u => (
                  <option key={u.id} value={u.id}>{u.username} - {u.role} (ID {u.id})</option>
                ))}
              </select>
            ) : (
              <input className="form-input" type="number" min="1" name="assignedToId" value={form.assignedToId ?? ''} onChange={handle} placeholder="ex : 2" />
            )}
          </div>
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Story points</label>
            <input className="form-input" type="number" min="0" name="storyPoints" value={form.storyPoints ?? ''} onChange={handle} placeholder="ex : 5" />
          </div>
          <div className="form-group">
            <label className="form-label">Heures estimees</label>
            <input className="form-input" type="number" min="0" step="0.5" name="estimatedHours" value={form.estimatedHours ?? ''} onChange={handle} placeholder="ex : 8" />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Sprint ID</label>
          <input className="form-input" type="number" min="1" name="sprintId" value={form.sprintId ?? ''} onChange={handle} placeholder="ex : 1" />
        </div>
      </form>
    </Modal>
  )
}
