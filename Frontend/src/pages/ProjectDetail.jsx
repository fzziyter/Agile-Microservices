import { useParams, Link } from 'react-router-dom'
import { useApi } from '../hooks/useApi'
import { projectsApi } from '../api/services'
import './ProjectDetail.css'

export default function ProjectDetail() {
  const { id } = useParams()
  const { data: project, loading, error } = useApi(projectsApi.get, id)

  if (loading) return <div className="spinner-page"><div className="spinner" /></div>
  if (error)   return <div className="card empty-state"><p style={{color:'var(--red-600)'}}>{error}</p></div>
  if (!project) return null

  const methodColor = project.methodology === 'SCRUM' ? 'badge-blue' : project.methodology === 'KANBAN' ? 'badge-green' : 'badge-gray'

  return (
    <div className="project-detail">
      {/* breadcrumb */}
      <div className="project-detail-bread">
        <Link to="/projects" className="bread-link">Projets</Link>
        <span className="bread-sep">/</span>
        <span>{project.name}</span>
      </div>

      {/* hero */}
      <div className="project-detail-hero card">
        <div className="pdh-left">
          <div className="pdh-icon" style={{ background: project.methodology === 'SCRUM' ? '#2e90e822' : project.methodology === 'KANBAN' ? '#22a76b22' : '#8f9ab522', color: project.methodology === 'SCRUM' ? '#2e90e8' : project.methodology === 'KANBAN' ? '#22a76b' : '#8f9ab5' }}>
            {project.name?.[0]?.toUpperCase()}
          </div>
          <div>
            <h1 className="page-title" style={{ marginBottom: 4 }}>{project.name}</h1>
            <div style={{ display:'flex', alignItems:'center', gap:10, flexWrap:'wrap' }}>
              <span className={`badge ${methodColor}`}>{project.methodology}</span>
              {project.startDate && <span className="project-detail-date">📅 {fmt(project.startDate)} {project.endDate ? `→ ${fmt(project.endDate)}` : ''}</span>}
              {project.sprintCapacityPts > 0 && <span className="badge badge-blue">{project.sprintCapacityPts} pts/sprint</span>}
            </div>
          </div>
        </div>
        <Link to="/backlog" className="btn btn-secondary">Voir le backlog</Link>
      </div>

      {project.description && (
        <div className="card project-detail-desc">
          <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', marginBottom:8 }}>Description</h3>
          <p style={{ color:'var(--text-secondary)', lineHeight:1.7, fontSize:'.9rem' }}>{project.description}</p>
        </div>
      )}

      {/* Stats mini */}
      <div className="grid-3">
        <div className="card pd-stat">
          <p className="pd-stat-val">{project.members?.length ?? 0}</p>
          <p className="pd-stat-lbl">Membres</p>
        </div>
        <div className="card pd-stat">
          <p className="pd-stat-val">{project.sprintCapacityPts || '—'}</p>
          <p className="pd-stat-lbl">Points / sprint</p>
        </div>
        <div className="card pd-stat">
          <p className="pd-stat-val">{project.methodology}</p>
          <p className="pd-stat-lbl">Méthodologie</p>
        </div>
      </div>

      {/* Members */}
      {project.members?.length > 0 && (
        <div className="card" style={{ padding:0, overflow:'hidden' }}>
          <div style={{ padding:'16px 20px', borderBottom:'1px solid var(--border)' }}>
            <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', fontWeight:600 }}>Équipe</h3>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Nom</th><th>Rôle</th></tr>
              </thead>
              <tbody>
                {project.members.map((m, i) => (
                  <tr key={i}>
                    <td>
                      <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                        <div style={{ width:30, height:30, borderRadius:'50%', background:'var(--blue-100)', color:'var(--blue-700)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'.8rem', fontWeight:600 }}>
                          {m.username?.[0]?.toUpperCase() || m.firstName?.[0]?.toUpperCase() || '?'}
                        </div>
                        {m.username || `${m.firstName || ''} ${m.lastName || ''}`.trim()}
                      </div>
                    </td>
                    <td><span className="badge badge-gray">{m.role}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

const fmt = (d) => d ? new Date(d).toLocaleDateString('fr-FR', { day:'2-digit', month:'short', year:'numeric' }) : ''
