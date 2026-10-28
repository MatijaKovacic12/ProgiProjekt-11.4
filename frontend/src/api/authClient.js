const BACKEND_URL = 'http://localhost:8080'

// backend role -> frontend role
const ROLE_MAP = {
    USER: 'client',
    ADMIN: 'admin',
    ORGANIZER: 'organizer',
    GUIDE: 'guide',
    MANAGER: 'manager',
}

export function loginWithGoogle(){
    window.location.href = `${BACKEND_URL}/oauth2/authorization/google`
}

export async function getCurrentUser(){
    const res = await fetch('/api/auth/me');
    if(!res.ok){
        return null
    }
    const user = await res.json()
    return { ...user, role: ROLE_MAP[user.role] }
}

export async function logout(){
    await fetch('/api/auth/logout', { method: 'POST' })
}
