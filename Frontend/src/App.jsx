import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import AppLayout from './components/layout/AppLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Projects from './pages/Projects'
import ProjectDetail from './pages/ProjectDetail'
import Backlog from './pages/Backlog'
import Sprints from './pages/Sprints'
import Notifications from './pages/Notifications'
import Users from './pages/admin/Users'
import { useAuth } from './context/AuthContext'

// Only blocks unauthenticated users
// Role-based access is handled INSIDE each page via can()
function PrivateRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

function PublicRoute({ children }) {
  const { user } = useAuth()
  return user ? <Navigate to="/projects" replace /> : children
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
      <Route element={<PrivateRoute><AppLayout /></PrivateRoute>}>
        <Route index element={<Navigate to="/projects" replace />} />
        <Route path="/dashboard"    element={<Dashboard />} />
        <Route path="/projects"     element={<Projects />} />
        <Route path="/projects/:id" element={<ProjectDetail />} />
        <Route path="/backlog"      element={<Backlog />} />
        <Route path="/sprints"      element={<Sprints />} />
        <Route path="/notifications" element={<Notifications />} />
        <Route path="/admin/users"  element={<Users />} />
      </Route>
      <Route path="*" element={<Navigate to="/projects" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <AppRoutes />
      </ToastProvider>
    </AuthProvider>
  )
}
