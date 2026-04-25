import { useState } from 'react'
import { useApi, useMutation } from '../../hooks/useApi'
import { useAuth } from '../../context/AuthContext'
import { adminApi } from '../../api/services'
import { useToast } from '../../context/ToastContext'
import UserForm from './UserForm'
import ConfirmDialog from '../../components/ui/ConfirmDialog'
import './Users.css'

const ROLE_LABELS = { ADMIN: 'Administrator', PRODUCT_OWNER: 'Product Owner', SCRUM_MASTER: 'Scrum Master', DEVELOPER: 'Developer', MANAGER: 'Manager' }
const ROLE_BADGES = { ADMIN: 'badge-red', PRODUCT_OWNER: 'badge-blue', SCRUM_MASTER: 'badge-green', DEVELOPER: 'badge-gray', MANAGER: 'badge-amber' }

export default function Users() {
  const { can, user } = useAuth()
  const toast = useToast()
  const { data: users, loading, error, refetch } = useApi(adminApi.listUsers)

  const [search,    setSearch]    = useState('')
  const [roleFilter,setRoleFilter]= useState('ALL')
  const [formOpen,  setFormOpen]  = useState(false)
  const [editing,   setEditing]   = useState(null)
  const [deleting,  setDeleting]  = useState(null)

  const createMut = useMutation(adminApi.createUser)
  const updateMut = useMutation((id, d) => adminApi.updateUser(id, d))
  const deleteMut = useMutation(adminApi.deleteUser)

  // If the API returns 403, show a clear access-denied banner
  if (error) {
    return (
      <div>
        <div className="page-header">
          <h1 className="page-title">Users</h1>
        </div>
        <div className="card" style={{ padding: '32px 24px', textAlign: 'center' }}>
          <div style={{ fontSize: '2.5rem', marginBottom: 16 }}>
            <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24" style={{ color: 'var(--red-500)', margin: '0 auto' }}><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          </div>
          <p style={{ fontWeight: 600, fontSize: '1rem', marginBottom: 8 }}>Access denied</p>
          <p style={{ color: 'var(--text-secondary)', fontSize: '.875rem', maxWidth: 380, margin: '0 auto' }}>
            You are connected as <strong>{user?.role}</strong>. You do not have permission to manage users.
          </p>
        </div>
      </div>
    )
  }

  const filtered = (users || []).filter(u => {
    const matchSearch = u.username?.toLowerCase().includes(search.toLowerCase())
    const matchRole   = roleFilter === 'ALL' || u.role === roleFilter
    return matchSearch && matchRole
  })

  const handleNewClick = () => {
    const perm = can('createUser')
    if (!perm.allowed) { toast.error(perm.message); return }
    setEditing(null); setFormOpen(true)
  }

  const handleEditClick = (u) => {
    const perm = can('editUser')
    if (!perm.allowed) { toast.error(perm.message); return }
    setEditing(u); setFormOpen(true)
  }

  const handleDeleteClick = (u) => {
    const perm = can('deleteUser')
    if (!perm.allowed) { toast.error(perm.message); return }
    setDeleting(u)
  }

  const handleSubmit = async (form) => {
    if (editing) {
      const payload = { ...form }
      if (!payload.password) delete payload.password
      const res = await updateMut.mutate(editing.id, payload)
      if (res.ok) { toast.success('User updated'); setFormOpen(false); setEditing(null); refetch() }
      else toast.error(res.message)
    } else {
      const res = await createMut.mutate(form)
      if (res.ok) { toast.success('User created'); setFormOpen(false); refetch() }
      else toast.error(res.message)
    }
  }

  const handleDelete = async () => {
    const res = await deleteMut.mutate(deleting.id)
    if (res.ok) { toast.success('User deleted'); setDeleting(null); refetch() }
    else toast.error(res.message)
  }

  const roles = ['ALL', 'ADMIN', 'PRODUCT_OWNER', 'SCRUM_MASTER', 'DEVELOPER', 'MANAGER']

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Users</h1>
          <p className="page-subtitle">{filtered.length} user{filtered.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={handleNewClick}><PlusIcon /> New user</button>
      </div>

      <div className="card users-toolbar" style={{ padding: '14px 16px', marginBottom: 20 }}>
        <div className="projects-search">
          <SearchIcon />
          <input className="projects-search-input" placeholder="Search a user…" value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {roles.map(r => (
            <button key={r} className={`btn btn-sm ${roleFilter === r ? 'btn-primary' : 'btn-ghost'}`} onClick={() => setRoleFilter(r)}>
              {r === 'ALL' ? 'All' : ROLE_LABELS[r] || r}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="spinner-page"><div className="spinner" /></div>
      ) : filtered.length === 0 ? (
        <div className="card empty-state"><UsersIcon /><p>No users found.</p></div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <div className="table-wrap">
            <table>
              <thead><tr><th>User</th><th>Role</th><th>ID</th><th style={{ width: 100 }}>Actions</th></tr></thead>
              <tbody>
                {filtered.map(u => (
                  <tr key={u.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                        <div className="user-avatar">{u.username?.[0]?.toUpperCase()}</div>
                        <p style={{ fontWeight: 500, fontSize: '.875rem' }}>{u.username}</p>
                      </div>
                    </td>
                    <td><span className={`badge ${ROLE_BADGES[u.role] || 'badge-gray'}`}>{ROLE_LABELS[u.role] || u.role}</span></td>
                    <td style={{ fontSize: '.78rem', color: 'var(--text-muted)', fontFamily: 'monospace' }}>#{u.id}</td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button className="btn btn-icon btn-ghost btn-sm" onClick={() => handleEditClick(u)} title="Edit"><EditIcon /></button>
                        <button className="btn btn-icon btn-danger btn-sm" onClick={() => handleDeleteClick(u)} title="Delete"><TrashIcon /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <UserForm open={formOpen} onClose={() => { setFormOpen(false); setEditing(null) }} onSubmit={handleSubmit} initial={editing} loading={createMut.loading || updateMut.loading} />
      <ConfirmDialog open={!!deleting} onClose={() => setDeleting(null)} onConfirm={handleDelete} loading={deleteMut.loading} title="Delete user" message={`Delete "${deleting?.username}"? This is irreversible.`} />
    </div>
  )
}

function PlusIcon()   { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.2" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> }
function SearchIcon() { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" style={{ color: 'var(--text-muted)' }}><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg> }
function EditIcon()   { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> }
function TrashIcon()  { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg> }
function UsersIcon()  { return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg> }
