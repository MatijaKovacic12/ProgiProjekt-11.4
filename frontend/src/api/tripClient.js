async function handleResponse(res) {
    if (!res.ok) {
        let message = `Request failed (${res.status})`;
        try {
            const body = await res.json();
            if (body?.message) {
                message = body.message;
            } else if (body?.errors && Array.isArray(body.errors)) {
                message = body.errors.join(', ');
            }
        } catch {

        }
        const error = new Error(message);
        error.status = res.status;
        throw error;
    }


    if (res.status === 204) {
        return null;
    }

    return res.json();
}

export async function getTrips(criteria = {}) {
    const params = new URLSearchParams();

    Object.entries(criteria).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            params.append(key, value);
        }
    });

    const query = params.toString() ? `?${params.toString()}` : '';
    const res = await fetch(`/api/trips${query}`);
    return handleResponse(res);
}


export async function getTripById(id) {
    const res = await fetch(`/api/trips/${id}`);
    return handleResponse(res);
}


export async function createTrip(tripData) {
    const res = await fetch('/api/trips', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(tripData),
    });
    return handleResponse(res);
}

export async function updateTrip(id, tripData) {
    const res = await fetch(`/api/trips/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(tripData),
    });
    return handleResponse(res);
}


export async function deleteTrip(id) {
    const res = await fetch(`/api/trips/${id}`, {
        method: 'DELETE',
    });
    return handleResponse(res);
}


export async function getTripSessions(tripId) {
    const res = await fetch(`/api/trips/${tripId}/sessions`);
    return handleResponse(res);
}


export async function addTripSession(tripId, sessionData) {
    const res = await fetch(`$/api/trips/${tripId}/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(sessionData),
    });
    return handleResponse(res);
}