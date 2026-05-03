import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useApi, useMutation } from '../hooks/useApi'
import { projectsApi, sprintsApi } from '../api/services'
import { useToast } from '../context/ToastContext'
import Modal from '../components/ui/Modal'
import './ProjectDetail.css'

const EMPTY_MEMBER = { email: '', role: 'DEV' }

export default function ProjectDetail() {
  const { id } = useParams()
  const toast = useToast()
  const { data: project, loading, error } = useApi(projectsApi.get, id)
  const { data: members, refetch: refetchMembers } = useApi(projectsApi.listMembers, id)
  const { data: sprints } = useApi(sprintsApi.listByProject, id)
  const inviteMut = useMutation((projectId, data) => projectsApi.inviteMember(projectId, data))
  const [memberOpen, setMemberOpen] = useState(false)
  const [memberForm, setMemberForm] = useState(EMPTY_MEMBER)

  if (loading) return <div className="spinner-page"><div className="spinner" /></div>
  if (error) return <div className="card empty-state"><p style={{color:'var(--red-600)'}}>{error}</p></div>
  if (!project) return null

  const methodColor = project.methodology === 'SCRUM' ? 'badge-blue' : project.methodology === 'KANBAN' ? 'badge-green' : 'badge-gray'
  const activeSprints = (sprints || []).filter(s => s.status === 'IN_PROGRESS').length

  const inviteMember = async (e) => {
    e.preventDefault()
    const res = await inviteMut.mutate(id, memberForm)
    if (res.ok) {
      toast.success('Member invited')
      setMemberOpen(false)
      setMemberForm(EMPTY_MEMBER)
      refetchMembers()
    } else {
      toast.error(res.message)
    }
  }

  return (
    <div className="project-detail">
      <div className="project-detail-bread">
        <Link to="/projects" className="bread-link">Projects</Link>
        <span className="bread-sep">/</span>
        <span>{project.name}</span>
      </div>

      <div className="project-detail-hero card">
        <div className="pdh-left">
          <div className="pdh-icon" style={{ background: project.methodology === 'SCRUM' ? '#2e90e822' : project.methodology === 'KANBAN' ? '#22a76b22' : '#8f9ab522', color: project.methodology === 'SCRUM' ? '#2e90e8' : project.methodology === 'KANBAN' ? '#22a76b' : '#8f9ab5' }}>
            {project.name?.[0]?.toUpperCase()}
          </div>
          <div>
            <h1 className="page-title" style={{ marginBottom: 4 }}>{project.name}</h1>
            <div style={{ display:'flex', alignItems:'center', gap:10, flexWrap:'wrap' }}>
              <span className={`badge ${methodColor}`}>{project.methodology}</span>
              {project.startDate && <span className="project-detail-date">{fmt(project.startDate)} {project.endDate ? `-> ${fmt(project.endDate)}` : ''}</span>}
              {project.theoreticalCapacity > 0 && <span className="badge badge-blue">{project.theoreticalCapacity}h capacity</span>}
            </div>
          </div>
        </div>
        <div style={{ display:'flex', gap:8, flexWrap:'wrap' }}>
          <button className="btn btn-secondary" onClick={() => setMemberOpen(true)}>Invite member</button>
          <Link to="/sprints" className="btn btn-secondary">Sprints</Link>
          <Link to="/backlog" className="btn btn-primary">Backlog</Link>
        </div>
      </div>

      {project.description && (
        <div className="card project-detail-desc">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', marginBottom:8 }}>Description</h3>
          <p style={{ color:'var(--text-secondary)', lineHeight:1.7, fontSize:'.9rem' }}>{project.description}</p>
        </div>
      )}

      <div className="grid-3">
        <div className="card pd-stat">
          <p className="pd-stat-val">{(members || []).length}</p>
          <p className="pd-stat-lbl">Members</p>
        </div>
        <div className="card pd-stat">
          <p className="pd-stat-val">{(sprints || []).length}</p>
          <p className="pd-stat-lbl">Sprints</p>
        </div>
        <div className="card pd-stat">
          <p className="pd-stat-val">{activeSprints}</p>
          <p className="pd-stat-lbl">Active sprint</p>
        </div>
      </div>

      <div className="grid-2" style={{ alignItems:'start', marginTop:16 }}>
        <div className="card" style={{ padding:0, overflow:'hidden' }}>
          <div style={{ padding:'16px 20px', borderBottom:'1px solid var(--border)', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', fontWeight:600 }}>Team</h3>
            <button className="btn btn-sm btn-secondary" onClick={() => setMemberOpen(true)}>Invite</button>
          </div>
          {(members || []).length === 0 ? (
            <div style={{ padding:20, color:'var(--text-muted)' }}>No members invited yet.</div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Email</th><th>Role</th></tr></thead>
                <tbody>
                  {(members || []).map(m => (
                    <tr key={m.id}>
                      <td>{m.email}</td>
                      <td><span className="badge badge-gray">{roleLabel(m.role)}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="card" style={{ padding:0, overflow:'hidden' }}>
          <div style={{ padding:'16px 20px', borderBottom:'1px solid var(--border)' }}>
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', fontWeight:600 }}>Sprints</h3>
          </div>
          {(sprints || []).length === 0 ? (
            <div style={{ padding:20, color:'var(--text-muted)' }}>No sprints for this project.</div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Name</th><th>Status</th><th>Capacity</th></tr></thead>
                <tbody>
                  {(sprints || []).map(s => (
                    <tr key={s.id}>
                      <td>{s.name}</td>
                      <td><span className={`badge ${s.status === 'IN_PROGRESS' ? 'badge-blue' : s.status === 'DONE' ? 'badge-green' : 'badge-gray'}`}>{s.status}</span></td>
                      <td>{s.remainingCapacityHours ?? '-'} / {s.capacityHours ?? '-'}h</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      <Modal
        open={memberOpen}
        onClose={() => setMemberOpen(false)}
        title="Invite member"
        footer={
          <>
            <button type="button" className="btn btn-ghost" onClick={() => setMemberOpen(false)} disabled={inviteMut.loading}>Cancel</button>
            <button type="submit" form="invite-member-form" className="btn btn-primary" disabled={inviteMut.loading}>Invite</button>
          </>
        }
      >
        <form id="invite-member-form" onSubmit={inviteMember} style={{ display:'flex', flexDirection:'column', gap:14 }}>
          <div className="form-group">
            <label className="form-label">Email *</label>
            <input className="form-input" type="email" value={memberForm.email} onChange={e => setMemberForm(f => ({ ...f, email:e.target.value }))} required placeholder="member@email.com" />
          </div>
          <div className="form-group">
            <label className="form-label">Role</label>
            <select className="form-select" value={memberForm.role} onChange={e => setMemberForm(f => ({ ...f, role:e.target.value }))}>
              <option value="DEV">Developer</option>
              <option value="QA">QA</option>
              <option value="SCRUM_MASTER">Scrum Master</option>
              <option value="PRODUCT_OWNER">Product Owner</option>
            </select>
          </div>
        </form>
      </Modal>
    </div>
  )
}

const fmt = d => d ? new Date(d).toLocaleDateString('fr-FR', { day:'2-digit', month:'short', year:'numeric' }) : ''
const roleLabel = r => ({ DEV:'Developer', QA:'QA', SCRUM_MASTER:'Scrum Master', PRODUCT_OWNER:'Product Owner' })[r] || r
