import { useState } from 'react';
import { initialGuides, initialScheduledExcursions } from '../mock-backoffice-data.js';

function OrganizerAssignmentPage() {
  const [excursions, setExcursions] = useState(initialScheduledExcursions);
  const [guides, setGuides] = useState(initialGuides);
  const [selectedGuidesPerExcursion, setSelectedGuidesPerExcursion] = useState({});
  const [notification, setNotification] = useState(null);

  const showNotice = (msg) => {
    setNotification(msg);
    setTimeout(() => {
      setNotification(null);
    }, 4000);
  };

  const handleSelectGuideForExcursion = (excursionId, guideName) => {
    setSelectedGuidesPerExcursion((prev) => ({
      ...prev,
      [excursionId]: guideName,
    }));
  };

  const handleAssignGuide = (excursionId) => {
    const guideName = selectedGuidesPerExcursion[excursionId];
    if (!guideName) {
      alert('Please select a guide to assign.');
      return;
    }

    const excursion = excursions.find((e) => e.id === excursionId);
    if (!excursion) return;

    if (excursion.assignedGuides.includes(guideName)) {
      alert(`${guideName} is already assigned to this excursion.`);
      return;
    }

    // 1. Update Excursion Assigned Guides
    setExcursions((prev) =>
      prev.map((e) =>
        e.id === excursionId
          ? { ...e, assignedGuides: [...e.assignedGuides, guideName] }
          : e
      )
    );

    // 2. Update Guide's Assigned Activities & Status in Tracker
    setGuides((prev) =>
      prev.map((g) =>
        g.name === guideName
          ? {
              ...g,
              status: 'Assigned',
              assignedActivities: [
                ...g.assignedActivities,
                `${excursion.tourTitle} (${excursion.dateTime})`,
              ],
            }
          : g
      )
    );

    showNotice(`Assigned guide ${guideName} to "${excursion.tourTitle}".`);

    // Reset selector
    setSelectedGuidesPerExcursion((prev) => ({
      ...prev,
      [excursionId]: '',
    }));
  };

  const handleUnassignGuide = (excursionId, guideName) => {
    const excursion = excursions.find((e) => e.id === excursionId);
    if (!excursion) return;

    // 1. Remove guide from excursion
    setExcursions((prev) =>
      prev.map((e) =>
        e.id === excursionId
          ? {
              ...e,
              assignedGuides: e.assignedGuides.filter((g) => g !== guideName),
            }
          : e
      )
    );

    // 2. Remove activity from guide & update status if no activities remain
    setGuides((prev) =>
      prev.map((g) => {
        if (g.name === guideName) {
          const updatedActivities = g.assignedActivities.filter(
            (act) => !act.startsWith(excursion.tourTitle)
          );
          return {
            ...g,
            assignedActivities: updatedActivities,
            status: updatedActivities.length > 0 ? 'Assigned' : 'Available',
          };
        }
        return g;
      })
    );

    showNotice(`Unassigned guide ${guideName} from "${excursion.tourTitle}".`);
  };

  return (
    <div className="page-container">
      {/* Screen Title & Description */}
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
          Organizer Assignment
        </h2>
        <p style={{ margin: 0, color: '#4b5563', fontSize: '0.95rem' }}>
          Allocate qualified tour guides to scheduled departures and monitor guide availability in real time.
        </p>
      </div>

      {/* Notification Toast */}
      {notification && (
        <div className="toast-banner" role="status">
          <span>✓ {notification}</span>
          <button
            type="button"
            className="btn btn-sm btn-secondary"
            onClick={() => setNotification(null)}
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Guide Assignment */}
      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Guide Assignment
            </h3>
            <p style={{ margin: 0, color: '#6b7280', fontSize: '0.875rem' }}>
              Manage guide requirements and roster assignments for scheduled tour departures.
            </p>
          </div>
        </div>

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
              {excursions.map((excursion) => {
                const assignedCount = excursion.assignedGuides.length;
                const requiredCount = excursion.requiredGuides;
                const isFullyStaffed = assignedCount >= requiredCount;

                return (
                  <tr key={excursion.id}>
                    <td style={{ fontWeight: 600 }}>{excursion.tourTitle}</td>
                    <td>{excursion.dateTime}</td>
                    <td>
                      <span
                        className={`badge ${
                          isFullyStaffed ? 'badge-success' : 'badge-warning'
                        }`}
                      >
                        {assignedCount} / {requiredCount} Guide(s)
                      </span>
                    </td>
                    <td>
                      {excursion.assignedGuides.length > 0 ? (
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.35rem' }}>
                          {excursion.assignedGuides.map((guideName) => (
                            <span
                              key={guideName}
                              className="badge badge-info"
                              style={{ display: 'inline-flex', alignItems: 'center', gap: '0.35rem' }}
                            >
                              {guideName}
                              <button
                                type="button"
                                onClick={() => handleUnassignGuide(excursion.id, guideName)}
                                title={`Remove ${guideName}`}
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
                          value={selectedGuidesPerExcursion[excursion.id] || ''}
                          onChange={(e) =>
                            handleSelectGuideForExcursion(excursion.id, e.target.value)
                          }
                        >
                          <option value="">Select guide...</option>
                          {guides.map((g) => (
                            <option key={g.id} value={g.name}>
                              {g.name} ({g.status})
                            </option>
                          ))}
                        </select>
                        <button
                          type="button"
                          className="btn btn-sm btn-primary"
                          style={{ whiteSpace: 'nowrap' }}
                          onClick={() => handleAssignGuide(excursion.id)}
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
      </section>

      {/* Resource Availability Tracker */}
      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Resource Availability Tracker
            </h3>
            <p style={{ margin: 0, color: '#6b7280', fontSize: '0.875rem' }}>
              Real-time directory of tour guides, current operational assignments, and availability statuses.
            </p>
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
                    {guide.name}
                  </h4>
                  <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>
                    {guide.phone}
                  </span>
                </div>
                <span
                  className={`badge ${
                    guide.status === 'Available' ? 'badge-success' : 'badge-info'
                  }`}
                >
                  {guide.status}
                </span>
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
                {guide.assignedActivities.length > 0 ? (
                  <ul
                    style={{
                      margin: 0,
                      paddingLeft: '1.2rem',
                      fontSize: '0.85rem',
                      color: '#334155',
                    }}
                  >
                    {guide.assignedActivities.map((act, idx) => (
                      <li key={idx} style={{ marginBottom: '0.2rem' }}>
                        {act}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p
                    style={{
                      margin: 0,
                      fontSize: '0.85rem',
                      color: '#16a34a',
                      fontStyle: 'italic',
                    }}
                  >
                    No excursions currently assigned (Ready for assignment)
                  </p>
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
