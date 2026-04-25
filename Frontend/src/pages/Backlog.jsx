import { useState } from 'react'
import { useApi, useMutation } from '../hooks/useApi'
import { useAuth } from '../context/AuthContext'
import { backlogApi, projectsApi } from '../api/services'
import { useToast } from '../context/ToastContext'
import BacklogItemForm from './BacklogItemForm'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import './Backlog.css'

export default function Backlog() {
  const { can } = useAuth()
  const toast = useToast()

  const { data: projects } = useApi(projectsApi.list)
  const [selectedProject, setSelectedProject] = useState('')
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [search, setSearch] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)

  const { data: items, loading, refetch } = useApi(
    selectedProject ? backlogApi.listByProject : () => Promise.resolve({ data: [] }),
    selectedProject
  )

  const createMut = useMutation((pid, d) => backlogApi.create(pid, d))
  const updateMut = useMutation((id, d) => backlogApi.update(id, d))
  const deleteMut = useMutation(backlogApi.remove)

  const filtered = (items || []).filter(item => {
    const matchSearch = item.title?.toLowerCase().includes(search.toLowerCase())
    const matchType   = typeFilter === 'ALL' || item.type === typeFilter
    return matchSearch && matchType
  })

  const handleNewClick = () => {
    const perm = can('createBacklogItem')
    if (!perm.allowed) { toast.error(perm.message); return }
    if (!selectedProject) { toast.error('Please select a project first.'); return }
    setEditing(null); setFormOpen(true)
  }

  const handleEditClick = (item) => {
    const perm = can('editBacklogItem')
    if (!perm.allowed) { toast.error(perm.message); return }
    setEditing(item); setFormOpen(true)
  }

  const handleDeleteClick = (item) => {
    const perm = can('deleteBacklogItem')
    if (!perm.allowed) { toast.error(perm.message); return }
    setDeleting(item)
  }

  const handleSubmit = async (form) => {
    if (editing) {
      const res = await updateMut.mutate(editing.id, form)
      if (res.ok) { toast.success('Item updated'); setFormOpen(false); setEditing(null); refetch() }
      else toast.error(res.message)
    } else {
      const res = await createMut.mutate(selectedProject, form)
      if (res.ok) { toast.success('Item created'); setFormOpen(false); refetch() }
      else toast.error(res.message)
    }
  }

  const handleDelete = async () => {
    const res = await deleteMut.mutate(deleting.id)
    if (res.ok) { toast.success('Item deleted'); setDeleting(null); refetch() }
    else toast.error(res.message)
  }

  const typeStats = {
    USER_STORY: filtered.filter(i => i.type === 'USER_STORY').length,
    BUG:        filtered.filter(i => i.type === 'BUG').length,
    TECH_TASK:  filtered.filter(i => i.type === 'TECH_TASK').length,
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Product Backlog</h1>
          <p className="page-subtitle">{filtered.length} item{filtered.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={handleNewClick}>
          <PlusIcon /> New item
        </button>
      </div>

      <div className="card backlog-toolbar" style={{ padding: '14px 16px', marginBottom: 16 }}>
        <div className="form-group" style={{ flex: 1, gap: 4 }}>
          <label className="form-label">Project</label>
          <select className="form-select" value={selectedProject} onChange={e => setSelectedProject(e.target.value)}>
            <option value="">-- Select a project --</option>
            {(projects || []).map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>
        <div className="backlog-search">
          <SearchIcon />
          <input className="projects-search-input" placeholder="Search…" value={search} onChange={e => setSearch(e.target.value)} disabled={!selectedProject} />
        </div>
      </div>

      {selectedProject && (
        <div className="backlog-type-filters">
          {[
            { key: 'ALL',        label: 'All',            count: filtered.length },
            { key: 'USER_STORY', label: 'User Stories',   count: typeStats.USER_STORY },
            { key: 'BUG',        label: 'Bugs',           count: typeStats.BUG },
            { key: 'TECH_TASK',  label: 'Tech tasks',     count: typeStats.TECH_TASK },
          ].map(f => (
            <button key={f.key} className={`backlog-filter-btn ${typeFilter === f.key ? 'active' : ''}`} onClick={() => setTypeFilter(f.key)}>
              {f.label} <span className="backlog-filter-count">{f.count}</span>
            </button>
          ))}
        </div>
      )}

      {!selectedProject ? (
        <div className="card empty-state"><FolderIcon /><p>Select a project to view its backlog.</p></div>
      ) : loading ? (
        <div className="spinner-page"><div className="spinner" /></div>
      ) : filtered.length === 0 ? (
        <div className="card empty-state"><ListIcon /><p>No items in this backlog.</p>
          <button className="btn btn-primary btn-sm" style={{ marginTop: 12 }} onClick={handleNewClick}>Add first item</button>
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th style={{ width: 40 }}>#</th>
                  <th>Title</th>
                  <th>Type</th>
                  <th>Priority</th>
                  <th>Points</th>
                  <th>Hours</th>
                  <th style={{ width: 80 }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((item, idx) => (
                  <tr key={item.id}>
                    <td style={{ color: 'var(--text-muted)', fontSize: '.8rem' }}>{idx + 1}</td>
                    <td>
                      <p style={{ fontWeight: 500, fontSize: '.875rem' }}>{item.title}</p>
                      {item.description && <p style={{ fontSize: '.78rem', color: 'var(--text-muted)', marginTop: 2, overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 1, WebkitBoxOrient: 'vertical' }}>{item.description}</p>}
                    </td>
                    <td><span className={`badge ${typeBadge(item.type)}`}>{typeLabel(item.type)}</span></td>
                    <td><span className={`badge ${priorityBadge(item.priority)}`}>{priorityLabel(item.priority)}</span></td>
                    <td style={{ fontWeight: 600, color: 'var(--blue-600)' }}>{item.storyPoints ?? '—'}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{item.estimatedHours ? `${item.estimatedHours}h` : '—'}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button className="btn btn-icon btn-ghost btn-sm" onClick={() => handleEditClick(item)} title="Edit"><EditIcon /></button>
                        <button className="btn btn-icon btn-danger btn-sm" onClick={() => handleDeleteClick(item)} title="Delete"><TrashIcon /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <BacklogItemForm open={formOpen} onClose={() => { setFormOpen(false); setEditing(null) }} onSubmit={handleSubmit} initial={editing} loading={createMut.loading || updateMut.loading} />
      <ConfirmDialog open={!!deleting} onClose={() => setDeleting(null)} onConfirm={handleDelete} loading={deleteMut.loading} title="Delete item" message={`Delete "${deleting?.title}"?`} />
    </div>
  )
}

const typeLabel     = t => ({ USER_STORY: 'User Story', BUG: 'Bug', TECH_TASK: 'Tech task' })[t] || t
const typeBadge     = t => ({ USER_STORY: 'badge-blue', BUG: 'badge-red', TECH_TASK: 'badge-gray' })[t] || 'badge-gray'
const priorityLabel = p => ({ CRITICAL: 'Critical', HIGH: 'High', MEDIUM: 'Medium', LOW: 'Low' })[p] || p
const priorityBadge = p => ({ CRITICAL: 'badge-red', HIGH: 'badge-amber', MEDIUM: 'badge-blue', LOW: 'badge-gray' })[p] || 'badge-gray'

function PlusIcon()   { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.2" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> }
function SearchIcon() { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" style={{ color: 'var(--text-muted)' }}><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg> }
function EditIcon()   { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> }
function TrashIcon()  { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg> }
function FolderIcon() { return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24"><path d="M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7z"/></svg> }
function ListIcon()   { return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg> }
