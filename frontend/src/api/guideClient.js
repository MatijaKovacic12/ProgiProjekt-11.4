import { GetData, PostData, DeleteData } from './baseApiClient'

// guide: sessions the logged in guide is assigned to
export async function getMySessions(){
    return GetData('/guides/me/sessions')
}

// organizer: all guides with their upcoming sessions
export async function getGuideSchedules(){
    return GetData('/guides')
}

// organizer: upcoming sessions with assigned / required guides
export async function getUpcomingSessions(){
    return GetData('/guides/sessions')
}

// organizer: guides that are free during the given session
export async function getAvailableGuides(sessionId){
    return GetData(`/guides/sessions/${sessionId}/available`)
}

export async function assignGuide(sessionId, guideId){
    return PostData(`/guides/sessions/${sessionId}/assignments`, { guideId })
}

export async function unassignGuide(sessionId, guideId){
    return DeleteData(`/guides/sessions/${sessionId}/assignments/${guideId}`)
}
