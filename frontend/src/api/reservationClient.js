import { apiRequest } from './baseApiClient'
import { getTripSessions } from './tripClient.js'

export const STATUS_LABELS = {
    CONFIRMED: 'Confirmed',CANCELLED: 'Cancelled',COMPLETED: 'Completed'
}

export function statusLabel(status) {
    return STATUS_LABELS[status] || status;
}

export function formatDateTime(isoString) {
    if (!isoString) return '';
    const d = new Date(isoString);
    if (Number.isNaN(d.getTime())) return isoString;
    return d.toLocaleString('hr-HR', {
        day: '2-digit',month: '2-digit',year: 'numeric',hour: '2-digit',minute: '2-digit'
    })
}

export function createReservation({ sessionId, participants, addonIds }) {
    return apiRequest('POST', '/reservations', { sessionId, participants, addonIds })
}

export function getMyReservations() {
    return apiRequest('GET', '/reservations/mine')
}

export function cancelReservation(reservationId) {
    return apiRequest('POST', `/reservations/${reservationId}/cancel`)
}

export function searchAdminReservations({ tripId, sessionId, status, q, createdDate, page = 0, size = 10 } = {}) {
    const params = new URLSearchParams()
    if (tripId) params.set('tripId', tripId)
    if (sessionId) params.set('sessionId', sessionId)
    if (status) params.set('status', status)
    if (q && q.trim()) params.set('q', q.trim())
    if (createdDate) params.set('createdDate', createdDate)
    params.set('page', page)
    params.set('size', size)
    return apiRequest('GET', `/admin/reservations?${params.toString()}`)
}

export function adminCancelReservation(reservationId) {
    return apiRequest('POST', `/admin/reservations/${reservationId}/cancel`)
}

export async function countConfirmedForSession(sessionId) {
    const page = await searchAdminReservations({ sessionId, status: 'CONFIRMED', size: 1 })
    return page.totalElements ?? 0
}

export async function countAffectedByTripDeletion(tripId) {
    const sessions = await getTripSessions(tripId)
    const upcoming = sessions.filter((s) => new Date(s.startTime) > new Date())
    const counts = await Promise.all(upcoming.map((s) => countConfirmedForSession(s.id)))
    return counts.reduce((sum, n) => sum + n, 0)
}
