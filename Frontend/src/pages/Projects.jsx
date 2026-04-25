import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useApi, useMutation } from '../hooks/useApi'
import { useAuth } from '../context/AuthContext'
import { projectsApi } from '../api/services'
import { useToast } from '../context/ToastContext'
import ProjectForm from './ProjectForm'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import './Projects.css'

export default function Projects() {
  const { can } = useAuth()
  const toast = useToast()
  const { data: projects, loading, refetch } = useApi(projectsApi.list)

  const [search,   setSearch]   = useState('')
  const [filter,   setFilter]   = useState('ALL')
  const [formOpen, setFormOpen] = useState(false)
  const [editing,  setEditing]  = useState(null)
  const [deleting, setDeleting] = useState(null)

  const createMut = useMutation(projectsApi.create)
  const updateMut = useMutation((id, d) => projectsApi.update(id, d))
  const deleteMut = useMutation(projectsApi.remove)

  const filtered = (projects || []).filter(p => {
    const matchSearch = p.name?.toLowerCase().includes(search.toLowerCase())
    const matchFilter = filter === 'ALL' || p.methodology === filter
    return matchSearch && matchFilter
  })

  const handleNewClick = () => {
    const perm = can('createProject')
    if (!perm.allowed) { toast.error(perm.message); return }
    setEditing(null); setFormOpen(true)
  }

  const handleEditClick = (p) => {
    const perm = can('editProject')
    if (!perm.allowed) { toast.error(perm.message); return }
    setEditing(p); setFormOpen(true)
  }

  const handleDeleteClick = (p) => {
    const perm = can('deleteProject')
    if (!perm.allowed) { toast.error(perm.message); return }
    setDeleting(p)
  }

  const handleSubmit = async (form) => {
    if (editing) {
      const res = await updateMut.mutate(editing.id, form)
      if (res.ok) { toast.success('Project updated'); setFormOpen(false); setEditing(null); refetch() }
      else toast.error(res.message)
    } else {
      const res = await createMut.mutate(form)
      if (res.ok) { toast.success('Project created'); setFormOpen(false); refetch() }
      else toast.error(res.message)
    }
  }

  const handleDelete = async () => {
    const res = await deleteMut.mutate(deleting.id)
    if (res.ok) { toast.success('Project deleted'); setDeleting(null); refetch() }
    else toast.error(res.message)
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Projects</h1>
          <p className="page-subtitle">{filtered.length} project{filtered.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={handleNewClick}>
          <PlusIcon /> New project
        </button>
      </div>

      <div className="card projects-toolbar" style={{ padding: '14px 16px', marginBottom: 20 }}>
        <div className="projects-search">
          <SearchIcon />
          <input className="projects-search-input" placeholder="Search a project…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className="projects-filters">
          {['ALL', 'SCRUM', 'KANBAN', 'HYBRID'].map(f => (
            <button key={f} className={`btn btn-sm ${filter === f ? 'btn-primary' : 'btn-ghost'}`} onClick={() => setFilter(f)}>
              {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="spinner-page"><div className="spinner" /></div>
      ) : filtered.length === 0 ? (
        <div className="card empty-state">
          <EmptyIcon />
          <p>{search || filter !== 'ALL' ? 'No project matches your search.' : 'No projects yet.'}</p>
          <button className="btn btn-primary btn-sm" style={{ marginTop: 12 }} onClick={handleNewClick}>Create first project</button>
        </div>
      ) : (
        <div className="projects-grid">
          {filtered.map(p => (
            <div key={p.id} className="project-card card">
              <div className="project-card-top">
                <div className="project-card-icon" style={{ background: methodColor(p.methodology) + '1a', color: methodColor(p.methodology) }}>
                  {p.name?.[0]?.toUpperCase()}
                </div>
                <div className="project-card-actions">
                  <span className={`badge ${methodBadge(p.methodology)}`}>{p.methodology}</span>
                  <button className="btn btn-icon btn-ghost btn-sm" onClick={() => handleEditClick(p)} title="Edit"><EditIcon /></button>
                  <button className="btn btn-icon btn-danger btn-sm" onClick={() => handleDeleteClick(p)} title="Delete"><TrashIcon /></button>
                </div>
              </div>
              <Link to={`/projects/${p.id}`} className="project-card-name">{p.name}</Link>
              {p.description && <p className="project-card-desc">{p.description}</p>}
              <div className="project-card-footer">
                {p.startDate && <span className="project-card-date"><CalIcon /> {formatDate(p.startDate)}{p.endDate ? ` → ${formatDate(p.endDate)}` : ''}</span>}
                {p.sprintCapacityPts > 0 && <span className="badge badge-blue">{p.sprintCapacityPts} pts/sprint</span>}
              </div>
            </div>
          ))}
        </div>
      )}

      <ProjectForm open={formOpen} onClose={() => { setFormOpen(false); setEditing(null) }} onSubmit={handleSubmit} initial={editing} loading={createMut.loading || updateMut.loading} />
      <ConfirmDialog open={!!deleting} onClose={() => setDeleting(null)} onConfirm={handleDelete} loading={deleteMut.loading} title="Delete project" message={`Delete "${deleting?.name}"? All associated data will be lost.`} />
    </div>
  )
}

const methodColor = m => m === 'SCRUM' ? '#2e90e8' : m === 'KANBAN' ? '#22a76b' : '#8f9ab5'
const methodBadge = m => m === 'SCRUM' ? 'badge-blue' : m === 'KANBAN' ? 'badge-green' : 'badge-gray'
const formatDate  = d => d ? new Date(d).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : ''
function PlusIcon()   { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.2" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> }
function SearchIcon() { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" style={{ color: 'var(--text-muted)' }}><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg> }
function EditIcon()   { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> }
function TrashIcon()  { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg> }
function CalIcon()    { return <svg width="12" height="12" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg> }
function EmptyIcon()  { return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24"><path d="M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7z"/></svg> }
