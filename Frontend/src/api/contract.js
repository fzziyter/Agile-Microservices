/**
 * api/contract.js
 *
 * Derives permissions from the role returned by POST /api/auth/login.
 * No probing — the role in the JWT response is the source of truth.
 */

// Role → permission map (mirrors SecurityConfig exactly)
const ROLE_PERMISSIONS = {
  ADMIN: {
    createProject:     true,
    editProject:       true,
    deleteProject:     true,
    viewBacklog:       true,
    createBacklogItem: true,
    editBacklogItem:   true,
    deleteBacklogItem: true,
    manageUsers:       true,
    createUser:        true,
    editUser:          true,
    deleteUser:        true,
  },
  PRODUCT_OWNER: {
    createProject:     true,
    editProject:       true,
    deleteProject:     false,
    viewBacklog:       true,
    createBacklogItem: true,
    editBacklogItem:   true,
    deleteBacklogItem: true,
    manageUsers:       false,
    createUser:        false,
    editUser:          false,
    deleteUser:        false,
  },
  SCRUM_MASTER: {
    createProject:     false,
    editProject:       false,
    deleteProject:     false,
    viewBacklog:       true,
    createBacklogItem: false,
    editBacklogItem:   false,
    deleteBacklogItem: false,
    manageUsers:       false,
    createUser:        false,
    editUser:          false,
    deleteUser:        false,
  },
  DEVELOPER: {
    createProject:     false,
    editProject:       false,
    deleteProject:     false,
    viewBacklog:       true,
    createBacklogItem: true,
    editBacklogItem:   false,
    deleteBacklogItem: false,
    manageUsers:       false,
    createUser:        false,
    editUser:          false,
    deleteUser:        false,
  },
  MANAGER: {
    createProject:     false,
    editProject:       false,
    deleteProject:     false,
    viewBacklog:       true,
    createBacklogItem: false,
    editBacklogItem:   false,
    deleteBacklogItem: false,
    manageUsers:       false,
    createUser:        false,
    editUser:          false,
    deleteUser:        false,
  },
}

/**
 * getPermissionsForRole(role)
 * Returns the permission map for a given role string (e.g. "ADMIN").
 * Falls back to all-false if the role is unknown.
 */
export function getPermissionsForRole(role) {
  return ROLE_PERMISSIONS[role] ?? Object.fromEntries(
    Object.keys(ROLE_PERMISSIONS.ADMIN).map(k => [k, false])
  )
}