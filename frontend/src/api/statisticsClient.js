const BASE = '/api/manager/statistics'

async function handleResponse(res) {
    if (!res.ok) {
        let message = `Request failed (${res.status})`
        try {
            const body = await res.json()
            if (body?.message) message = body.message
        } catch {
            // odgovor nije JSON, ostaje zadana poruka
        }
        const error = new Error(message)
        error.status = res.status
        throw error
    }
    return res.json()
}

export async function getStatistics(from, to) {
    const res = await fetch(`${BASE}?from=${from}&to=${to}`)
    return handleResponse(res)
}

export function getExportUrl(from, to) {
    return `${BASE}/export?from=${from}&to=${to}`
}