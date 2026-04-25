import { useApi } from '../hooks/useApi'
import { projectsApi, backlogApi } from '../api/services'
import { useAuth } from '../context/AuthContext'
import StatCard from '../components/ui/StatCard'
import { Link } from 'react-router-dom'
import './Dashboard.css'

export default function Dashboard() {
  const { user } = useAuth()
  const { data: projects, loading: loadP } = useApi(projectsApi.list)

  const totalProjects = projects?.length ?? 0
  const scrum   = projects?.filter(p => p.methodology === 'SCRUM').length   ?? 0
  const kanban  = projects?.filter(p => p.methodology === 'KANBAN').length  ?? 0
  const hybrid  = projects?.filter(p => p.methodology === 'HYBRID').length  ?? 0

  return (
    <div className="dashboard">
      {/* header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Bonjour, {user?.username} 👋</h1>
          <p className="page-subtitle">Voici un aperçu de votre activité aujourd'hui.</p>
        </div>
        <Link to="/projects" className="btn btn-primary">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          Nouveau projet
        </Link>
      </div>

      {/* stats row */}
      {loadP ? (
        <div className="spinner-page"><div className="spinner" /></div>
      ) : (
        <>
          <div className="grid-4 dashboard-stats">
            <StatCard label="Projets totaux" value={totalProjects} color="blue"
              icon={<svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><path d="M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7z"/></svg>} />
            <StatCard label="Scrum" value={scrum} color="green"
              icon={<svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/><polyline points="17 6 23 6 23 12"/></svg>} />
            <StatCard label="Kanban" value={kanban} color="amber"
              icon={<svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/></svg>} />
            <StatCard label="Hybride" value={hybrid} color="gray"
              icon={<svg width="22" height="22" fill="none" stroke="currentColor" strokeWidth="1.7" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z"/></svg>} />
          </div>

          {/* projects list preview */}
          <div className="dashboard-section">
            <div className="dashboard-section-header">
              <h2 className="dashboard-section-title">Projets récents</h2>
              <Link to="/projects" className="btn btn-ghost btn-sm">Voir tout</Link>
            </div>
            {projects?.length === 0 ? (
              <div className="card empty-state">
                <EmptyIcon />
                <p>Aucun projet pour l'instant.</p>
                <Link to="/projects" className="btn btn-primary btn-sm" style={{ marginTop: 12 }}>Créer un projet</Link>
              </div>
            ) : (
              <div className="dashboard-projects-grid">
                {projects?.slice(0, 6).map(p => (
                  <Link key={p.id} to={`/projects/${p.id}`} className="dash-project-card card">
                    <div className="dash-project-top">
                      <div className="dash-project-icon" style={{ background: methodColor(p.methodology) + '22', color: methodColor(p.methodology) }}>
                        {p.name?.[0]?.toUpperCase()}
                      </div>
                      <span className={`badge ${methodBadge(p.methodology)}`}>{p.methodology}</span>
                    </div>
                    <h3 className="dash-project-name">{p.name}</h3>
                    {p.description && <p className="dash-project-desc">{p.description}</p>}
                    <div className="dash-project-meta">
                      {p.startDate && <span>{formatDate(p.startDate)}</span>}
                      {p.endDate   && <span>→ {formatDate(p.endDate)}</span>}
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  )
}

function methodColor(m) {
  if (m === 'SCRUM')  return '#2e90e8'
  if (m === 'KANBAN') return '#22a76b'
  return '#8f9ab5'
}
function methodBadge(m) {
  if (m === 'SCRUM')  return 'badge-blue'
  if (m === 'KANBAN') return 'badge-green'
  return 'badge-gray'
}
function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' })
}
function EmptyIcon() {
  return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.4" viewBox="0 0 24 24"><path d="M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V7z"/></svg>
}
