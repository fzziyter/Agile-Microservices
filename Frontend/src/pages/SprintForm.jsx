import { useEffect, useState } from 'react'
import Modal from '../components/ui/Modal'

const EMPTY = {
  name: '',
  goal: '',
  startDate: '',
  endDate: '',
  capacityHours: '',
  remainingCapacityHours: '',
  status: 'PLANNED',
  projectId: '',
}

export default function SprintForm({ open, onClose, onSubmit, initial, loading, projects = [] }) {
  const [form, setForm] = useState(EMPTY)

  useEffect(() => {
    setForm(initial ? { ...EMPTY, ...initial } : EMPTY)
  }, [initial, open])

  const handle = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  const submit = e => {
    e.preventDefault()
    onSubmit({
      ...form,
      capacityHours: form.capacityHours === '' ? null : Number(form.capacityHours),
      remainingCapacityHours: form.remainingCapacityHours === '' ? null : Number(form.remainingCapacityHours),
      projectId: form.projectId === '' ? null : Number(form.projectId),
    })
  }

  const isEdit = !!initial?.id

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isEdit ? 'Modifier le sprint' : 'Nouveau sprint'}
      footer={
        <>
          <button type="button" className="btn btn-ghost" onClick={onClose} disabled={loading}>Annuler</button>
          <button type="submit" form="sprint-form" className="btn btn-primary" disabled={loading}>
            {loading && <span className="spinner" style={{ width:16,height:16,borderColor:'rgba(255,255,255,.3)',borderTopColor:'#fff' }} />}
            {isEdit ? 'Enregistrer' : 'Creer'}
          </button>
        </>
      }
    >
      <form id="sprint-form" onSubmit={submit} style={{ display:'flex', flexDirection:'column', gap:14 }}>
        <div className="form-group">
          <label className="form-label">Projet *</label>
          <select className="form-select" name="projectId" value={form.projectId ?? ''} onChange={handle} required>
            <option value="">Selectionner un projet</option>
            {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>

        <div className="form-group">
          <label className="form-label">Nom *</label>
          <input className="form-input" name="name" value={form.name} onChange={handle} placeholder="ex : Sprint 1" required />
        </div>

        <div className="form-group">
          <label className="form-label">Objectif</label>
          <textarea className="form-textarea" name="goal" value={form.goal ?? ''} onChange={handle} placeholder="Objectif du sprint" />
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Date debut</label>
            <input className="form-input" type="date" name="startDate" value={form.startDate ?? ''} onChange={handle} />
          </div>
          <div className="form-group">
            <label className="form-label">Date fin</label>
            <input className="form-input" type="date" name="endDate" value={form.endDate ?? ''} onChange={handle} />
          </div>
        </div>

        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Capacite heures</label>
            <input className="form-input" type="number" min="0" step="0.5" name="capacityHours" value={form.capacityHours ?? ''} onChange={handle} placeholder="ex : 80" />
          </div>
          <div className="form-group">
            <label className="form-label">Capacite restante</label>
            <input className="form-input" type="number" min="0" step="0.5" name="remainingCapacityHours" value={form.remainingCapacityHours ?? ''} onChange={handle} placeholder="optionnel" />
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">Statut</label>
          <select className="form-select" name="status" value={form.status} onChange={handle}>
            <option value="PLANNED">Planifie</option>
            <option value="IN_PROGRESS">En cours</option>
            <option value="DONE">Termine</option>
          </select>
        </div>
      </form>
    </Modal>
  )
}
