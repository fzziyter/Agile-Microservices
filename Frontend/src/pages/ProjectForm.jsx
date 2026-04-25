import { useState, useEffect } from 'react'
import Modal from '../components/ui/Modal'

const EMPTY = { name: '', description: '', methodology: 'SCRUM', startDate: '', endDate: '', sprintCapacityPts: '' }

export default function ProjectForm({ open, onClose, onSubmit, initial, loading }) {
  const [form, setForm] = useState(EMPTY)

  useEffect(() => {
    setForm(initial ? { ...EMPTY, ...initial } : EMPTY)
  }, [initial, open])

  const handle = (e) => setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  const submit = (e) => { e.preventDefault(); onSubmit(form) }
  const isEdit = !!initial?.id

  return (
    <Modal
      open={open} onClose={onClose}
      title={isEdit ? 'Modifier le projet' : 'Nouveau projet'}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>Annuler</button>
          <button type="submit" form="project-form" className="btn btn-primary" disabled={loading}>
            {loading && <span className="spinner" style={{ width:16, height:16, borderColor:'rgba(255,255,255,.3)', borderTopColor:'#fff' }} />}
            {isEdit ? 'Enregistrer' : 'Créer'}
          </button>
        </>
      }
    >
      <form id="project-form" onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div className="form-group">
          <label className="form-label">Nom du projet *</label>
          <input className="form-input" name="name" value={form.name} onChange={handle} placeholder="ex : Refonte portail client" required />
        </div>

        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="form-textarea" name="description" value={form.description} onChange={handle} placeholder="Décrivez brièvement le projet…" />
        </div>

        <div className="form-group">
          <label className="form-label">Méthodologie</label>
          <select className="form-select" name="methodology" value={form.methodology} onChange={handle}>
            <option value="SCRUM">Scrum</option>
            <option value="KANBAN">Kanban</option>
            <option value="HYBRID">Hybride</option>
          </select>
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Date de début</label>
            <input className="form-input" type="date" name="startDate" value={form.startDate} onChange={handle} />
          </div>
          <div className="form-group">
            <label className="form-label">Date de fin</label>
            <input className="form-input" type="date" name="endDate" value={form.endDate} onChange={handle} />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Capacité sprint (points)</label>
          <input className="form-input" type="number" min="0" name="sprintCapacityPts" value={form.sprintCapacityPts} onChange={handle} placeholder="ex : 40" />
        </div>
      </form>
    </Modal>
  )
}
