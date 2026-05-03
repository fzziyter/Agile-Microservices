import { useEffect, useMemo, useState } from 'react'
import { useApi, useMutation } from '../hooks/useApi'
import { backlogApi, projectsApi, sprintsApi } from '../api/services'
import { useToast } from '../context/ToastContext'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import SprintForm from './SprintForm'

export default function Sprints() {
  const toast = useToast()
  const { data: projects } = useApi(projectsApi.list)
  const [selectedProject, setSelectedProject] = useState('')
  const [selectedSprintId, setSelectedSprintId] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [deleting, setDeleting] = useState(null)

  const { data: sprints, loading, refetch } = useApi(
    selectedProject ? sprintsApi.listByProject : sprintsApi.list,
    selectedProject
  )

  const selectedSprint = useMemo(
    () => (sprints || []).find(s => String(s.id) === String(selectedSprintId)) || null,
    [sprints, selectedSprintId]
  )

  useEffect(() => {
    if (!selectedSprintId && (sprints || []).length > 0) {
      setSelectedSprintId(String(sprints[0].id))
    }
    if (selectedSprintId && !(sprints || []).some(s => String(s.id) === String(selectedSprintId))) {
      setSelectedSprintId((sprints || [])[0]?.id ? String((sprints || [])[0].id) : '')
    }
  }, [sprints, selectedSprintId])

  const { data: sprintTasks, loading: tasksLoading, refetch: refetchTasks } = useApi(
    selectedSprintId ? backlogApi.listBySprint : () => Promise.resolve({ data: [] }),
    selectedSprintId
  )

  const { data: projectTasks, refetch: refetchProjectTasks } = useApi(
    selectedSprint?.projectId ? backlogApi.listByProject : () => Promise.resolve({ data: [] }),
    selectedSprint?.projectId
  )

  const createMut = useMutation(sprintsApi.create)
  const updateMut = useMutation((id, data) => sprintsApi.update(id, data))
  const deleteMut = useMutation(sprintsApi.remove)
  const assignMut = useMutation((itemId, data) => backlogApi.update(itemId, data))
  const statusMut = useMutation((itemId, status) => backlogApi.updateStatus(itemId, status))

  const handleSubmit = async (form) => {
    const res = editing
      ? await updateMut.mutate(editing.id, form)
      : await createMut.mutate(form)
    if (res.ok) {
      toast.success(editing ? 'Sprint updated' : 'Sprint created')
      setFormOpen(false)
      setEditing(null)
      refetch()
    } else {
      toast.error(res.message)
    }
  }

  const handleDelete = async () => {
    const res = await deleteMut.mutate(deleting.id)
    if (res.ok) {
      toast.success('Sprint deleted')
      setDeleting(null)
      refetch()
    } else {
      toast.error(res.message)
    }
  }

  const updateSprintStatus = async (sprint, status) => {
    const res = await updateMut.mutate(sprint.id, { status })
    if (res.ok) {
      toast.success(`Sprint ${statusLabel(status)}`)
      refetch()
    } else {
      toast.error(res.message)
    }
  }

  const assignToSprint = async (task) => {
    if (!selectedSprint) return
    const res = await assignMut.mutate(task.id, { sprintId: selectedSprint.id })
    if (res.ok) {
      toast.success('Task added to sprint')
      refetchTasks()
      refetchProjectTasks()
    } else {
      toast.error(res.message)
    }
  }

  const removeFromSprint = async (task) => {
    const res = await assignMut.mutate(task.id, { sprintId: null, status: 'TODO' })
    if (res.ok) {
      toast.success('Task returned to backlog')
      refetchTasks()
      refetchProjectTasks()
    } else {
      toast.error(res.message)
    }
  }

  const moveStatus = async (task, status) => {
    const res = await statusMut.mutate(task.id, status)
    if (res.ok) {
      toast.success(`Task moved to ${taskStatusLabel(status)}`)
      refetchTasks()
    } else {
      toast.error(res.message)
    }
  }

  const tasks = sprintTasks || []
  const usedHours = tasks.reduce((sum, t) => sum + Number(t.estimatedHours || 0), 0)
  const capacity = Number(selectedSprint?.capacityHours || 0)
  const capacityPercent = capacity > 0 ? Math.min(100, Math.round((usedHours / capacity) * 100)) : 0
  const doneCount = tasks.filter(t => t.status === 'DONE').length
  const progressPercent = tasks.length > 0 ? Math.round((doneCount / tasks.length) * 100) : 0
  const unplannedTasks = (projectTasks || []).filter(t => !t.sprintId)
  const totalCapacity = (sprints || []).reduce((sum, s) => sum + Number(s.capacityHours || 0), 0)
  const activeCount = (sprints || []).filter(s => s.status === 'IN_PROGRESS').length

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Sprints</h1>
          <p className="page-subtitle">{(sprints || []).length} sprint{(sprints || []).length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setEditing(null); setFormOpen(true) }}>
          <PlusIcon /> New sprint
        </button>
      </div>

      <div className="grid-3" style={{ marginBottom: 16 }}>
        <MetricCard label="Total capacity" value={`${totalCapacity}h`} />
        <MetricCard label="Active sprints" value={activeCount} />
        <MetricCard label="Selected progress" value={`${progressPercent}%`} />
      </div>

      <div className="card" style={{ padding: '14px 16px', marginBottom: 16 }}>
        <div className="grid-2">
          <div className="form-group">
            <label className="form-label">Project</label>
            <select className="form-select" value={selectedProject} onChange={e => { setSelectedProject(e.target.value); setSelectedSprintId('') }}>
              <option value="">All projects</option>
              {(projects || []).map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Sprint board</label>
            <select className="form-select" value={selectedSprintId} onChange={e => setSelectedSprintId(e.target.value)} disabled={(sprints || []).length === 0}>
              {(sprints || []).map(s => <option key={s.id} value={s.id}>{s.name} - {statusLabel(s.status)}</option>)}
            </select>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="spinner-page"><div className="spinner" /></div>
      ) : (sprints || []).length === 0 ? (
        <div className="card empty-state"><CalendarIcon /><p>No sprints yet.</p></div>
      ) : (
        <>
          <div className="card" style={{ padding: 0, overflow: 'hidden', marginBottom: 16 }}>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Name</th><th>Project</th><th>Status</th><th>Dates</th><th>Capacity</th><th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {(sprints || []).map(s => (
                    <tr key={s.id} style={{ background: String(s.id) === String(selectedSprintId) ? 'var(--blue-50)' : undefined }}>
                      <td>
                        <button className="btn btn-sm btn-ghost" onClick={() => setSelectedSprintId(String(s.id))}>{s.name}</button>
                        {s.goal && <p style={{ color:'var(--text-muted)', fontSize:'.8rem', marginTop:4 }}>{s.goal}</p>}
                      </td>
                      <td>{projectName(projects, s.projectId)}</td>
                      <td><span className={`badge ${statusBadge(s.status)}`}>{statusLabel(s.status)}</span></td>
                      <td>{fmt(s.startDate)} {s.endDate ? `-> ${fmt(s.endDate)}` : ''}</td>
                      <td>{s.capacityHours ?? '-'}h</td>
                      <td>
                        <div style={{ display:'flex', gap:4, flexWrap:'wrap' }}>
                          {s.status === 'PLANNED' && <button className="btn btn-sm btn-secondary" onClick={() => updateSprintStatus(s, 'IN_PROGRESS')}>Start</button>}
                          {s.status === 'IN_PROGRESS' && <button className="btn btn-sm btn-secondary" onClick={() => updateSprintStatus(s, 'DONE')}>Complete</button>}
                          <button className="btn btn-icon btn-ghost btn-sm" onClick={() => { setEditing(s); setFormOpen(true) }} title="Edit"><EditIcon /></button>
                          <button className="btn btn-icon btn-danger btn-sm" onClick={() => setDeleting(s)} title="Delete"><TrashIcon /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {selectedSprint && (
            <div style={{ display:'flex', flexDirection:'column', gap:16 }}>
              <div className="card" style={{ padding:18 }}>
                <div className="page-header" style={{ marginBottom:14 }}>
                  <div>
                    <h2 className="page-title">{selectedSprint.name}</h2>
                    <p className="page-subtitle">{projectName(projects, selectedSprint.projectId)} · {tasks.length} task{tasks.length !== 1 ? 's' : ''}</p>
                  </div>
                  <span className={`badge ${statusBadge(selectedSprint.status)}`}>{statusLabel(selectedSprint.status)}</span>
                </div>
                <div className="grid-2">
                  <ProgressPanel label="Capacity used" detail={`${usedHours}h / ${capacity || 0}h`} percent={capacityPercent} danger={capacityPercent > 100} />
                  <ProgressPanel label="Sprint progress" detail={`${doneCount} done / ${tasks.length} tasks`} percent={progressPercent} />
                </div>
              </div>

              <div className="grid-2" style={{ alignItems:'start' }}>
                <div className="card" style={{ padding:16 }}>
                  <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', marginBottom:12 }}>Backlog candidates</h3>
                  {unplannedTasks.length === 0 ? (
                    <p style={{ color:'var(--text-muted)' }}>No unplanned tasks for this project.</p>
                  ) : (
                    <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                      {unplannedTasks.map(task => (
                        <TaskMiniCard key={task.id} task={task} actionLabel="Add to sprint" onAction={() => assignToSprint(task)} />
                      ))}
                    </div>
                  )}
                </div>

                <div className="card" style={{ padding:16 }}>
                  <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.95rem', marginBottom:12 }}>Sprint summary</h3>
                  <div className="grid-2">
                    {['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED'].map(status => (
                      <MetricCard key={status} label={taskStatusLabel(status)} value={tasks.filter(t => t.status === status).length} />
                    ))}
                  </div>
                </div>
              </div>

              {tasksLoading ? (
                <div className="spinner-page"><div className="spinner" /></div>
              ) : (
                <div style={{ display:'grid', gridTemplateColumns:'repeat(4, minmax(180px, 1fr))', gap:12, alignItems:'start' }}>
                  {['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED'].map(status => (
                    <div key={status} className="card" style={{ padding:12 }}>
                      <h3 style={{ fontFamily:'var(--font-display)', fontSize:'.9rem', marginBottom:10 }}>{taskStatusLabel(status)}</h3>
                      <div style={{ display:'flex', flexDirection:'column', gap:8 }}>
                        {tasks.filter(t => (t.status || 'TODO') === status).map(task => (
                          <TaskBoardCard
                            key={task.id}
                            task={task}
                            onMove={moveStatus}
                            onRemove={removeFromSprint}
                          />
                        ))}
                        {tasks.filter(t => (t.status || 'TODO') === status).length === 0 && (
                          <p style={{ color:'var(--text-muted)', fontSize:'.82rem' }}>No tasks</p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </>
      )}

      <SprintForm open={formOpen} onClose={() => { setFormOpen(false); setEditing(null) }} onSubmit={handleSubmit} initial={editing} loading={createMut.loading || updateMut.loading} projects={projects || []} />
      <ConfirmDialog open={!!deleting} onClose={() => setDeleting(null)} onConfirm={handleDelete} loading={deleteMut.loading} title="Delete sprint" message={`Delete "${deleting?.name}"?`} />
    </div>
  )
}

function MetricCard({ label, value }) {
  return <div className="card" style={{ padding:18 }}><p className="page-subtitle">{label}</p><h2 className="page-title">{value}</h2></div>
}

function ProgressPanel({ label, detail, percent, danger }) {
  return (
    <div>
      <div style={{ display:'flex', justifyContent:'space-between', marginBottom:6 }}>
        <span className="form-label">{label}</span>
        <span style={{ fontWeight:600 }}>{detail}</span>
      </div>
      <div style={{ height:10, background:'var(--gray-100)', borderRadius:99, overflow:'hidden' }}>
        <div style={{ width:`${Math.min(100, percent)}%`, height:'100%', background: danger ? 'var(--red-500)' : 'var(--blue-500)' }} />
      </div>
      <p style={{ color: danger ? 'var(--red-600)' : 'var(--text-muted)', fontSize:'.8rem', marginTop:6 }}>{percent}%</p>
    </div>
  )
}

function TaskMiniCard({ task, actionLabel, onAction }) {
  return (
    <div style={{ border:'1px solid var(--border)', borderRadius:'var(--radius-md)', padding:10 }}>
      <p style={{ fontWeight:600, fontSize:'.88rem' }}>{task.title}</p>
      <p style={{ color:'var(--text-muted)', fontSize:'.78rem' }}>{task.estimatedHours || 0}h · {typeLabel(task.type)}</p>
      <button className="btn btn-sm btn-secondary" style={{ marginTop:8 }} onClick={onAction}>{actionLabel}</button>
    </div>
  )
}

function TaskBoardCard({ task, onMove, onRemove }) {
  const next = task.status === 'TODO' ? 'IN_PROGRESS' : task.status === 'IN_PROGRESS' ? 'DONE' : null
  return (
    <div style={{ border:'1px solid var(--border)', borderRadius:'var(--radius-md)', padding:10, background:'#fff' }}>
      <p style={{ fontWeight:600, fontSize:'.88rem' }}>{task.title}</p>
      <div style={{ display:'flex', gap:6, flexWrap:'wrap', marginTop:6 }}>
        <span className={`badge ${typeBadge(task.type)}`}>{typeLabel(task.type)}</span>
        <span className="badge badge-gray">{task.estimatedHours || 0}h</span>
      </div>
      {task.assignedToId && <p style={{ color:'var(--text-muted)', fontSize:'.78rem', marginTop:6 }}>Assigned: {task.assignedToId}</p>}
      <div style={{ display:'flex', gap:6, flexWrap:'wrap', marginTop:10 }}>
        {next && <button className="btn btn-sm btn-secondary" onClick={() => onMove(task, next)}>{next === 'IN_PROGRESS' ? 'Start' : 'Done'}</button>}
        {task.status !== 'BLOCKED' && task.status !== 'DONE' && <button className="btn btn-sm btn-ghost" onClick={() => onMove(task, 'BLOCKED')}>Block</button>}
        <button className="btn btn-sm btn-ghost" onClick={() => onRemove(task)}>Backlog</button>
      </div>
    </div>
  )
}

const projectName = (projects, id) => (projects || []).find(p => String(p.id) === String(id))?.name || id || '-'
const fmt = d => d ? new Date(d).toLocaleDateString('fr-FR') : '-'
const statusLabel = s => ({ PLANNED: 'Planifie', IN_PROGRESS: 'En cours', DONE: 'Termine' })[s] || s
const statusBadge = s => ({ PLANNED: 'badge-gray', IN_PROGRESS: 'badge-blue', DONE: 'badge-green' })[s] || 'badge-gray'
const taskStatusLabel = s => ({ TODO:'To Do', IN_PROGRESS:'In Progress', DONE:'Done', BLOCKED:'Blocked' })[s] || s
const typeLabel = t => ({ USER_STORY:'Story', BUG:'Bug', TECHNICAL_TASK:'Tech', TECH_TASK:'Tech', QA:'QA' })[t] || t
const typeBadge = t => ({ USER_STORY:'badge-blue', BUG:'badge-red', TECHNICAL_TASK:'badge-gray', TECH_TASK:'badge-gray', QA:'badge-green' })[t] || 'badge-gray'
function PlusIcon() { return <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2.2" viewBox="0 0 24 24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> }
function EditIcon() { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> }
function TrashIcon() { return <svg width="15" height="15" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg> }
function CalendarIcon() { return <svg width="48" height="48" fill="none" stroke="currentColor" strokeWidth="1.2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg> }
