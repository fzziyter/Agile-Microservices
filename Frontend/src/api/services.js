import api from './client'

// Auth est gérée via HTTP Basic — pas d'endpoint login dédié.
// Les credentials sont attachés automatiquement par client.js à chaque requête.

// ── Projects ──────────────────────────────────────────────────────────────────
export const projectsApi = {
  list:   ()          => api.get('/projects'),
  get:    (id)        => api.get(`/projects/${id}`),
  create: (data)      => api.post('/projects', data),
  update: (id, data)  => api.put(`/projects/${id}`, data),
  remove: (id)        => api.delete(`/projects/${id}`),
  listMembers: (id)       => api.get(`/projects/${id}/members`),
  inviteMember: (id, data) => api.post(`/projects/${id}/members`, data),
}

// ── Backlog ───────────────────────────────────────────────────────────────────
export const backlogApi = {
  listByProject: (projectId) => api.get(`/backlog/project/${projectId}`),
  listBySprint:  (sprintId)  => api.get(`/backlog/sprint/${sprintId}`),
  create:        (projectId, data) => api.post(`/backlog/${projectId}`, data),
  update:        (itemId, data)    => api.put(`/backlog/${itemId}`, data),
  updateStatus:  (itemId, status, comment = '')  => api.patch(`/backlog/${itemId}/status`, { status, comment }),
  sprintInfo:    (itemId)          => api.get(`/backlog/${itemId}/sprint-info`),
  remove:        (itemId)          => api.delete(`/backlog/${itemId}`),
}

export const sprintsApi = {
  list:          ()          => api.get('/sprints'),
  listByProject: (projectId) => api.get(`/sprints/project/${projectId}`),
  get:           (id)        => api.get(`/sprints/${id}`),
  create:        (data)      => api.post('/sprints', data),
  update:        (id, data)  => api.put(`/sprints/${id}`, data),
  remove:        (id)        => api.delete(`/sprints/${id}`),
}

export const notificationsApi = {
  list:        ()       => api.get('/notifications'),
  listByUser:  (userId) => api.get(`/notifications/user/${userId}`),
  listByEmail: (email)  => api.get(`/notifications/email/${email}`),
  markRead:    (id)     => api.patch(`/notifications/${id}/read`),
  remove:      (id)     => api.delete(`/notifications/${id}`),
}

// ── Admin ─────────────────────────────────────────────────────────────────────
export const adminApi = {
  listUsers:   ()         => api.get('/admin/users'),
  createUser:  (data)     => api.post('/admin/users', data),
  updateUser:  (id, data) => api.put(`/admin/users/${id}`, data),
  deleteUser:  (id)       => api.delete(`/admin/users/${id}`),
}
