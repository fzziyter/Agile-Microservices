/**
 * context/AuthContext.jsx
 */
import { createContext, useContext, useState, useCallback } from 'react'
import axios from 'axios'
import { getPermissionsForRole } from '../api/contract'

const AuthContext = createContext(null)

function roleLabel(role) {
  if (!role) return 'unknown role'
  return role.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ')
}

function denialMessage(action, role) {
  const labels = {
    createProject: 'create a project', editProject: 'edit a project',
    deleteProject: 'delete a project', viewBacklog: 'access the backlog',
    createBacklogItem: 'add items to the backlog', editBacklogItem: 'edit backlog items',
    deleteBacklogItem: 'delete backlog items', manageUsers: 'manage users',
    createUser: 'create users', editUser: 'edit users', deleteUser: 'delete users',
  }
  return `You are connected as ${roleLabel(role)}. You do not have permission to ${labels[action] || action}.`
}

export function AuthProvider({ children }) {
  const [user,        setUser]        = useState(() => { try { return JSON.parse(localStorage.getItem('user')) } catch { return null } })
  const [permissions, setPermissions] = useState(() => { try { return JSON.parse(localStorage.getItem('permissions')) } catch { return null } })
  const [loading,     setLoading]     = useState(false)

  const login = useCallback(async (username, password) => {
    setLoading(true)
    try {
      // 1. Call the JWT login endpoint
      const { data } = await axios.post('/api/auth/login', { username, password })
      // data = { token: "...", id: 1, username: "...", role: "..." }

      // 2. Store the JWT so client.js interceptor attaches it on every request
      localStorage.setItem('token', data.token)

      // 3. Derive permissions directly from the role — no probing needed
      const perms = getPermissionsForRole(data.role)

      // 4. Persist user info
      const userData = { id: data.id, username: data.username, role: data.role }
      localStorage.setItem('user', JSON.stringify(userData))
      localStorage.setItem('permissions', JSON.stringify(perms))
      setUser(userData)
      setPermissions(perms)
      return { ok: true }

    } catch (err) {
      const status = err.response?.status
      if (status === 401) return { ok: false, message: 'Incorrect username or password.' }
      if (status === 403) return { ok: false, message: 'Access denied.' }
      if (!err.response)  return { ok: false, message: 'Server unreachable — make sure Spring Boot is running on :8080.' }
      return { ok: false, message: `Server error (${status}).` }
    } finally {
      setLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('permissions')
    setUser(null)
    setPermissions(null)
  }, [])

  const can = useCallback((action) => {
    if (!permissions) return { allowed: false, message: 'Not authenticated.' }
    const allowed = permissions[action] === true
    return { allowed, message: allowed ? '' : denialMessage(action, user?.role) }
  }, [permissions, user])

  return (
    <AuthContext.Provider value={{ user, permissions, loading, login, logout, can }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
