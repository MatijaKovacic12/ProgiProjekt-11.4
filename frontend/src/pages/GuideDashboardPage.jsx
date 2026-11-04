import { useEffect, useState } from 'react';
import { getMySessions } from '../api/guideClient.js';

function formatDateTime(isoString) {
  const d = new Date(isoString);
  if (isNaN(d.getTime())) return isoString;
  return d.toLocaleString('hr-HR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function errorText(error) {
  if (error.status === 401) return 'Please log in to see your assigned tours.';
  if (error.status === 403) return 'Only users with the guide role can see this schedule.';
  return error.message;
}

function GuideDashboardPage() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getMySessions()
      .then(setSessions)
      .catch(setError)
      .finally(() => setLoading(false));
  }, []);

  const renderBody = () => {
    if (loading) {
      return <p style={{ color: '#4b5563' }}>Loading schedule...</p>;
    }
    if (error) {
      return <p style={{ color: '#dc2626', fontWeight: 500 }}>{errorText(error)}</p>;
    }
    if (sessions.length === 0) {
      return <p style={{ color: '#4b5563' }}>You are not assigned to any upcoming tours.</p>;
    }
    return (
      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Tour Name</th>
              <th>Departure Date and Time</th>
              <th>Departure Point</th>
              <th>Booked People</th>
            </tr>
          </thead>
          <tbody>
            {sessions.map((slot) => (
              <tr key={slot.sessionId}>
                <td style={{ fontWeight: 600, color: '#111827' }}>{slot.tripName}</td>
                <td>{formatDateTime(slot.startTime)}</td>
                <td>{slot.departurePoint ? `📍 ${slot.departurePoint}` : '-'}</td>
                <td>{slot.bookedPeople}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  return (
    <div className="page-container">
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
          Guide Dashboard
        </h2>
        <p style={{ margin: 0, color: '#4b5563', fontSize: '0.95rem' }}>
          Schedule of assigned tour departures appointed to lead.
        </p>
      </div>

      {renderBody()}
    </div>
  );
}

export default GuideDashboardPage;
