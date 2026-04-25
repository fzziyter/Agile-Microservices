import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import './Sidebar.css'

function GridIcon()   { return <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg> }
function FolderIcon() { return <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><path d="M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7z"/></svg> }
function ListIcon()   { return <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg> }
function UsersIcon()  { return <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/></svg> }
function LogoutIcon() { return <svg width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg> }

const ROLE_LABELS = {
  ADMIN: 'Administrator', DEVELOPER: 'Developer',
  SCRUM_MASTER: 'Scrum Master', PRODUCT_OWNER: 'Product Owner', MANAGER: 'Manager',
}

// Same navigation for ALL roles — restrictions are shown inside each page
const NAV = [
  { to: '/projects', label: 'Projects',  icon: <FolderIcon /> },
  { to: '/backlog',  label: 'Backlog',   icon: <ListIcon /> },
  { to: '/dashboard',label: 'Dashboard', icon: <GridIcon /> },
]
const ADMIN_NAV = [
  { to: '/admin/users', label: 'Users', icon: <UsersIcon /> },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <span className="sidebar-logo-mark">A</span>
        <span className="sidebar-logo-text">AgileTool</span>
      </div>

      <nav className="sidebar-nav">
        <p className="sidebar-section-label">Navigation</p>
        {NAV.map(n => (
          <NavLink key={n.to} to={n.to} className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            {n.icon}<span>{n.label}</span>
          </NavLink>
        ))}

        <p className="sidebar-section-label" style={{ marginTop: 20 }}>Administration</p>
        {ADMIN_NAV.map(n => (
          <NavLink key={n.to} to={n.to} className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            {n.icon}<span>{n.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <div className="sidebar-avatar">{user?.username?.[0]?.toUpperCase() || 'U'}</div>
          <div className="sidebar-user-info">
            <p className="sidebar-username">{user?.username}</p>
            <p className="sidebar-role">{ROLE_LABELS[user?.role] || user?.role}</p>
          </div>
        </div>
        <button className="sidebar-logout" onClick={handleLogout} title="Logout">
          <LogoutIcon />
        </button>
      </div>
    </aside>
  )
}

