// Lokalno (npm run dev) backend je na drugom portu; na deployu frontend i backend dijele istu adresu
const BACKEND_URL = import.meta.env.DEV ? 'http://localhost:8080' : ''

// backend role -> frontend role
const ROLE_MAP = {
    USER: 'client',
    ADMIN: 'admin',
    ORGANIZER: 'organizer',
    GUIDE: 'guide',
    MANAGER: 'manager',
}

// provider is 'google' or 'microsoft'
export function loginWith(provider){
    window.location.href = `${BACKEND_URL}/oauth2/authorization/${provider}`
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
