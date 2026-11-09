export async function GetData(url){
    const res = await fetch(`/api${url}`);
    if(!res.ok) throw new Error("Error na backendu: "+res.status);
    
        return res.json()
    
}

export class ApiError extends Error {
    constructor(status, code, message, fieldErrors) {
        super(message)
        this.name = 'ApiError'
        this.status = status
        this.code = code
        this.fieldErrors = fieldErrors || null
    }
}

export async function apiRequest(method, url, body){
    const options = { method, headers: {} }
    if (body !== undefined) {
        options.headers['Content-Type'] = 'application/json'
        options.body = JSON.stringify(body)
    }

    const res = await fetch(`/api${url}`, options)

    if (!res.ok) {
        let data = null
        try {
            data = await res.json()
        } catch {
        }
        const fallback = res.status === 401 ? 'Please log in to continue.' : res.status === 403 ? 'You do not have permission to do this.'
                : 'Error na backendu: ' + res.status
        throw new ApiError(res.status,data?.code || null,data?.message || fallback, data?.fieldErrors)
    }

    if (res.status === 204) return null;
    return res.json();
}