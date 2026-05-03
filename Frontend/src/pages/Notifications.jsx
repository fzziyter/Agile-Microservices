import { useApi, useMutation } from '../hooks/useApi'
import { notificationsApi } from '../api/services'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'

export default function Notifications() {
  const { user } = useAuth()
  const toast = useToast()
  const apiCall = async () => {
    if (!user?.id && !user?.username) return notificationsApi.list()

    const calls = []
    if (user?.id) calls.push(notificationsApi.listByUser(user.id))
    if (user?.username) calls.push(notificationsApi.listByEmail(user.username))

    const responses = await Promise.all(calls)
    const merged = responses.flatMap(res => res.data || [])
    const unique = Array.from(new Map(merged.map(n => [n.id, n])).values())
    unique.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    return { data: unique }
  }
  const { data: notifications, loading, refetch } = useApi(apiCall, user?.id, user?.username)
  const readMut = useMutation(notificationsApi.markRead)
  const deleteMut = useMutation(notificationsApi.remove)

  const markRead = async (id) => {
    const res = await readMut.mutate(id)
    if (res.ok) { toast.success('Notification marked as read'); refetch() }
    else toast.error(res.message)
  }

  const remove = async (id) => {
    const res = await deleteMut.mutate(id)
    if (res.ok) { toast.success('Notification deleted'); refetch() }
    else toast.error(res.message)
  }

  const unread = (notifications || []).filter(n => !n.readFlag).length

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">{unread} unread notification{unread !== 1 ? 's' : ''}</p>
        </div>
      </div>

      {loading ? (
        <div className="spinner-page"><div className="spinner" /></div>
      ) : (notifications || []).length === 0 ? (
        <div className="card empty-state"><BellIcon /><p>No notifications.</p></div>
      ) : (
        <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
          {(notifications || []).map(n => (
            <div key={n.id} className="card" style={{ padding:16, display:'flex', justifyContent:'space-between', gap:16, alignItems:'center', borderLeft:`4px solid ${n.readFlag ? 'var(--gray-200)' : 'var(--blue-500)'}` }}>
              <div>
                <div style={{ display:'flex', gap:8, alignItems:'center', marginBottom:4 }}>
                  <span className={`badge ${typeBadge(n.type)}`}>{typeLabel(n.type)}</span>
                  {!n.readFlag && <span className="badge badge-blue">New</span>}
                </div>
                <p style={{ fontWeight:600 }}>{n.message}</p>
                <p style={{ color:'var(--text-muted)', fontSize:'.8rem' }}>{formatDate(n.createdAt)}</p>
              </div>
              <div style={{ display:'flex', gap:6 }}>
                {!n.readFlag && <button className="btn btn-sm btn-secondary" onClick={() => markRead(n.id)}>Read</button>}
                <button className="btn btn-sm btn-danger" onClick={() => remove(n.id)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

const typeLabel = t => ({ ASSIGNMENT: 'Assignment', STATUS_CHANGE: 'Status', INVITATION: 'Invitation' })[t] || t
const typeBadge = t => ({ ASSIGNMENT: 'badge-blue', STATUS_CHANGE: 'badge-amber', INVITATION: 'badge-green' })[t] || 'badge-gray'
const formatDate = d => d ? new Date(d).toLocaleString('fr-FR') : ''
function BellIcon() { return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24"><path d="M18 8a6 6 0 10-12 0c0 7-3 7-3 7h18s-3 0-3-7"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg> }
