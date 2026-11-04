import { useCallback, useEffect, useState } from 'react';
import {
  assignGuide,
  getAvailableGuides,
  getGuideSchedules,
  getUpcomingSessions,
  unassignGuide,
} from '../api/guideClient.js';

function formatShortDateTime(dtStr) {
  if (!dtStr) return '';
  const d = new Date(dtStr);
  if (isNaN(d.getTime())) return dtStr;
  const month = d.toLocaleString('en-US', { month: 'short' });
  const day = d.getDate();
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${month} ${day}, ${hours}:${minutes}`;
}

function guideName(guide) {
  return `${guide.firstName} ${guide.lastName}`.trim();
}

function errorText(error) {
  if (error.status === 401) return 'Please log in to manage guide assignments.';
  if (error.status === 403) return 'Only users with the organizer role can manage guide assignments.';
  return error.message;
}

function OrganizerAssignmentPage() {
  const [sessions, setSessions] = useState([]);
  const [guides, setGuides] = useState([]);
  const [availableBySession, setAvailableBySession] = useState({});
  const [selectedGuidesPerSession, setSelectedGuidesPerSession] = useState({});
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [notification, setNotification] = useState(null);

  const loadData = useCallback(
    () =>
      Promise.all([getUpcomingSessions(), getGuideSchedules()])
        .then(([sessionData, guideData]) => {
          setSessions(sessionData);
          setGuides(guideData);
          // availability changes after every assignment, so it is fetched again on demand
          setAvailableBySession({});
          setLoadError(null);
        })
        .catch(setLoadError)
        .finally(() => setLoading(false)),
    []
  );

  useEffect(() => {
    loadData();
  }, [loadData]);

  const showNotice = (text, isError = false) => {
    setNotification({ text, isError });
    setTimeout(() => {
      setNotification(null);
    }, 4000);
  };

  const loadAvailableGuides = (sessionId) => {
    if (availableBySession[sessionId]) return;
    getAvailableGuides(sessionId)
      .then((available) =>
        setAvailableBySession((prev) => ({ ...prev, [sessionId]: available }))
      )
      .catch((error) => showNotice(errorText(error), true));
  };

  const handleSelectGuideForSession = (sessionId, guideId) => {
    setSelectedGuidesPerSession((prev) => ({
      ...prev,
      [sessionId]: guideId,
    }));
  };

  const handleAssignGuide = async (session) => {
    const guideId = selectedGuidesPerSession[session.sessionId];
    if (!guideId) {
      alert('Please select a guide to assign.');
      return;
    }

    try {
      await assignGuide(session.sessionId, Number(guideId));
      showNotice(`Guide assigned to "${session.tripName}".`);
      setSelectedGuidesPerSession((prev) => ({ ...prev, [session.sessionId]: '' }));
      await loadData();
    } catch (error) {
      showNotice(errorText(error), true);
    }
  };

  const handleUnassignGuide = async (session, guide) => {
    if (!window.confirm(`Remove ${guideName(guide)} from "${session.tripName}"?`)) {
      return;
    }

    try {
      await unassignGuide(session.sessionId, guide.id);
      showNotice(`Unassigned guide ${guideName(guide)} from "${session.tripName}".`);
      await loadData();
    } catch (error) {
      showNotice(errorText(error), true);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <p style={{ color: '#4b5563' }}>Loading guide assignments...</p>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="page-container">
        <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
          Organizer Dashboard
        </h2>
        <p style={{ color: '#dc2626', fontWeight: 500 }}>{errorText(loadError)}</p>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
          Organizer Dashboard
        </h2>
      </div>

      {notification && (
        <div
          className="toast-banner"
          role="status"
          style={
            notification.isError
              ? { backgroundColor: '#fef2f2', borderColor: '#fecaca', color: '#991b1b' }
              : undefined
          }
        >
          <span>{notification.isError ? '✕' : '✓'} {notification.text}</span>
          <button
            type="button"
            className="btn btn-sm btn-secondary"
            onClick={() => setNotification(null)}
          >
            Dismiss
          </button>
        </div>
      )}

      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Guide Assignment
            </h3>
          </div>
        </div>

        {sessions.length === 0 ? (
          <p style={{ color: '#4b5563' }}>There are no upcoming tour sessions.</p>
        ) : (
          <div className="data-table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Tour Name</th>
                  <th>Date / Time</th>
                  <th>Required Guides</th>
                  <th>Assigned Guides</th>
                  <th style={{ minWidth: '260px' }}>Assign Guide</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map((session) => {
                  const available = availableBySession[session.sessionId];

                  return (
                    <tr key={session.sessionId}>
                      <td style={{ fontWeight: 600 }}>{session.tripName}</td>
                      <td>
                        {formatShortDateTime(session.startTime)} -{' '}
                        {formatShortDateTime(session.endTime)}
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            session.fullyStaffed ? 'badge-success' : 'badge-warning'
                          }`}
                        >
                          {session.assignedGuides.length} / {session.requiredGuides} Guide(s)
                        </span>
                      </td>
                      <td>
                        {session.assignedGuides.length > 0 ? (
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                            {session.assignedGuides.map((guide) => (
                              <span
                                key={guide.id}
                                className="badge badge-info"
                                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                              >
                                {guideName(guide)}
                                <button
                                  type="button"
                                  onClick={() => handleUnassignGuide(session, guide)}
                                  title={`Remove ${guideName(guide)}`}
                                  style={{
                                    background: 'none',
                                    border: 'none',
                                    color: '#0369a1',
                                    cursor: 'pointer',
                                    padding: 0,
                                    fontWeight: 'bold',
                                    fontSize: '0.75rem',
                                    lineHeight: 1,
                                  }}
                                >
                                  ×
                                </button>
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.85rem', color: '#dc2626', fontWeight: 500 }}>
                            No guide assigned yet
                          </span>
                        )}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                          <select
                            className="form-select"
                            style={{ padding: '0.35rem 0.6rem', fontSize: '0.85rem' }}
                            value={selectedGuidesPerSession[session.sessionId] || ''}
                            onFocus={() => loadAvailableGuides(session.sessionId)}
                            onChange={(e) =>
                              handleSelectGuideForSession(session.sessionId, e.target.value)
                            }
                          >
                            <option value="">
                              {available && available.length === 0
                                ? 'No free guides'
                                : 'Select free guide...'}
                            </option>
                            {(available || []).map((g) => (
                              <option key={g.id} value={g.id}>
                                {guideName(g)}
                              </option>
                            ))}
                          </select>
                          <button
                            type="button"
                            className="btn btn-sm btn-primary"
                            style={{ whiteSpace: 'nowrap' }}
                            onClick={() => handleAssignGuide(session)}
                          >
                            Assign Guide
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Resource Availability
            </h3>
          </div>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
            gap: '1rem',
          }}
        >
          {guides.map((guide) => (
            <div key={guide.id} className="card" style={{ padding: '1.25rem' }}>
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: '0.75rem',
                }}
              >
                <div>
                  <h4 style={{ margin: 0, fontSize: '1.05rem', color: '#111827' }}>
                    {guideName(guide)}
                  </h4>
                  <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>
                    {guide.email}
                  </span>
                </div>
              </div>

              <div>
                <span
                  style={{
                    display: 'block',
                    fontSize: '0.75rem',
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    color: '#64748b',
                    marginBottom: '0.35rem',
                  }}
                >
                  Currently Assigned Activities:
                </span>
                {guide.upcomingSessions.length > 0 ? (
                  <ul
                    style={{
                      margin: 0,
                      paddingLeft: '1.2rem',
                      fontSize: '0.85rem',
                      color: '#334155',
                    }}
                  >
                    {guide.upcomingSessions.map((s) => (
                      <li key={s.sessionId} style={{ marginBottom: '0.2rem' }}>
                        {s.tripName} ({formatShortDateTime(s.startTime)})
                      </li>
                    ))}
                  </ul>
                ) : (
                  <span style={{ fontSize: '0.85rem', color: '#6b7280' }}>None</span>
                )}
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

export default OrganizerAssignmentPage;
