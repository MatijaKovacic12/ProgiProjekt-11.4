export class ApiError extends Error {
    constructor(status, message){
        super(message)
        this.status = status
    }
}

async function request(url, options = {}){
    const res = await fetch(`/api${url}`, options);
    if(!res.ok){
        // backend returns ErrorResponse { message } for handled errors
        const body = await res.json().catch(() => null)
        throw new ApiError(res.status, body?.message || "Error na backendu: "+res.status);
    }
    if(res.status === 204){
        return null
    }
    return res.json()
}

export async function GetData(url){
    return request(url)
}

export async function PostData(url, data){
    return request(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    })
}

export async function DeleteData(url){
    return request(url, { method: 'DELETE' })
}
