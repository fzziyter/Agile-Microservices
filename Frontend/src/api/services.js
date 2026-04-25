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
}

// ── Backlog ───────────────────────────────────────────────────────────────────
export const backlogApi = {
  listByProject: (projectId) => api.get(`/backlog/project/${projectId}`),
  create:        (projectId, data) => api.post(`/backlog/${projectId}`, data),
  update:        (itemId, data)    => api.put(`/backlog/${itemId}`, data),
  remove:        (itemId)          => api.delete(`/backlog/${itemId}`),
}

// ── Admin ─────────────────────────────────────────────────────────────────────
export const adminApi = {
  listUsers:   ()         => api.get('/admin/users'),
  createUser:  (data)     => api.post('/admin/users', data),
  updateUser:  (id, data) => api.put(`/admin/users/${id}`, data),
  deleteUser:  (id)       => api.delete(`/admin/users/${id}`),
}
