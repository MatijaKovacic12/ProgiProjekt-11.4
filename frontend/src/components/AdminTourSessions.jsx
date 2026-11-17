import { useEffect, useState } from 'react';
import { cancelTripSession, getTripSessions } from '../api/tripClient.js';
import { countConfirmedForSession, formatDateTime } from '../api/reservationClient.js';

function hasStarted(session) {
  return new Date(session.startTime) <= new Date();
}

function AdminTourSessions({ tour, onClose, onChanged }) {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busyId, setBusyId] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const list = await getTripSessions(tour.id);
        const withCounts = await Promise.all(
          list.map(async (session) => ({
            ...session,
            confirmedCount: await countConfirmedForSession(session.id).catch(() => null),
          })),
        );
        if (!cancelled) {
          setSessions(withCounts);
          setError('');
        }
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [tour.id, reloadKey]);

  const handleCancel = async (session) => {
    const affected = session.confirmedCount;
    const warning =
      affected === null
        ? 'Potvrđene rezervacije za ovaj termin bit će otkazane, a korisnici će dobiti e-mail.'
        : affected === 0
          ? 'Za ovaj termin nema potvrđenih rezervacija.'
          : `Broj potvrđenih rezervacija koje će biti otkazane: ${affected}. Korisnici će dobiti e-mail.`;

    if (!window.confirm(`Otkazati termin ${formatDateTime(session.startTime)}?\n\n${warning}`)) {
      return;
    }

    setBusyId(session.id);
    setError('');
    setMessage('');
    try {
      await cancelTripSession(tour.id, session.id);
      setMessage(`Termin ${formatDateTime(session.startTime)} je otkazan.`);
      setLoading(true);
      setReloadKey((k) => k + 1);
      if (onChanged) onChanged();
    } catch (err) {
      setError(err.message);
      if (err.status === 409) {
        setLoading(true);
        setReloadKey((k) => k + 1);
      }
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 style={{ margin: 0, fontSize: '1.25rem', color: '#111827' }}>
            Sessions: {tour.title}
          </h3>
          <button type="button" className="btn btn-sm btn-secondary" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="modal-body">
          {error && <div className="error-banner">{error}</div>}
          {message && (
            <div className="error-banner" style={{ background: '#f0fdf4', borderColor: '#bbf7d0', color: '#166534' }}>
              {message}
            </div>
          )}

          {loading && <p style={{ color: '#6b7280', margin: 0 }}>Loading sessions...</p>}

          {!loading && sessions.length === 0 && (
            <p style={{ color: '#6b7280', margin: 0 }}>No active sessions.</p>
          )}

          {!loading && sessions.length > 0 && (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Start</th>
                  <th>Free seats</th>
                  <th>Confirmed reservations</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map((session) => (
                  <tr key={session.id}>
                    <td>{formatDateTime(session.startTime)}</td>
                    <td>{session.availableSeats}</td>
                    <td>{session.confirmedCount === null ? '?' : session.confirmedCount}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        type="button"
                        className="btn btn-sm btn-secondary"
                        style={{ color: '#b91c1c' }}
                        disabled={busyId === session.id || hasStarted(session)}
                        title={hasStarted(session) ? 'Termin je već počeo' : 'Cancel session'}
                        onClick={() => handleCancel(session)}
                      >
                        {busyId === session.id ? 'Cancelling...' : 'Cancel session'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="modal-footer">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

export default AdminTourSessions;
