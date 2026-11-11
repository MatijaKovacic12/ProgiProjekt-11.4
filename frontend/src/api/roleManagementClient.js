const BASE = '/api/manager/employees'

const ROLE_MAP = {
    USER: 'client',
    ADMIN: 'admin',
    ORGANIZER: 'organizer',
    GUIDE: 'guide',
    MANAGER: 'manager',
}

const ROLE_TO_BACKEND = Object.fromEntries(
    Object.entries(ROLE_MAP).map(([backend, frontend]) => [frontend, backend])
)

function toEmployee(dto) {
    const name = `${dto.firstName ?? ''} ${dto.lastName ?? ''}`.trim()
    return {
        id: dto.id,
        name: name || dto.email,
        email: dto.email,
        role: ROLE_MAP[dto.role],
        isEmployee: dto.employee,
    }
}

async function handleResponse(res) {
    if (!res.ok) {
        let message = `Request failed (${res.status})`
        try {
            const body = await res.json()
            if (body?.message) message = body.message
        } catch {
        }
        const error = new Error(message)
        error.status = res.status
        throw error
    }
    return res.json()
}

export async function getEmployees() {
    const res = await fetch(`${BASE}`)
    const data = await handleResponse(res)
    return data.map(toEmployee)
}


export async function findUserByEmail(email) {
    const res = await fetch(`${BASE}?email=${encodeURIComponent(email)}`)
    if (res.status === 404) {
        return null
    }
    return toEmployee(await handleResponse(res))
}


export async function changeRole(userId, role) {
    const res = await fetch(`${BASE}/${userId}/role`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ role: ROLE_TO_BACKEND[role] }),
    })
    return toEmployee(await handleResponse(res))
}