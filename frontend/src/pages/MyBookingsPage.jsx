import { useCallback, useEffect, useState } from 'react';
import {
  cancelReservation,
  formatDateTime,
  getMyReservations,
  statusLabel,
} from '../api/reservationClient.js';

function getStatusBadgeClass(status) {
  switch (status) {
    case 'Confirmed':
      return 'badge badge-success';
    case 'Completed':
      return 'badge badge-info';
    case 'Cancelled':
      return 'badge badge-danger';
    default:
      return 'badge badge-neutral';
  }
}

function canCancel(booking) {
  return booking.status === 'CONFIRMED' && new Date(booking.startTime) > new Date();
}

function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [cancellingId, setCancellingId] = useState(null);

  const loadBookings = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setBookings(await getMyReservations());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBookings();
  }, [loadBookings]);

  const handleCancel = async (booking) => {
    if (!window.confirm('Are you sure you want to cancel this reservation?')) {
      return;
    }
    setCancellingId(booking.id);
    setError('');
    setMessage('');
    try {
      await cancelReservation(booking.id);
      setMessage(`Reservation for "${booking.tourTitle}" has been cancelled.`);
      await loadBookings();
    } catch (e) {
      setError(e.message);
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div className="page-container">
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
          My Bookings
        </h2>
        <p style={{ margin: 0, color: '#4b5563', fontSize: '0.95rem' }}>
          Overview of your personal reservations, departure details, and current statuses.
        </p>
      </div>

      {message && (
        <div className="toast-banner" role="status">
          <span>{message}</span>
          <button
            type="button"
            className="btn btn-sm btn-secondary"
            onClick={() => setMessage('')}
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="error-banner" role="alert">
          {error}
        </div>
      )}

      {loading ? (
        <p style={{ color: '#6b7280' }}>Loading your reservations…</p>
      ) : (
        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Tour Name</th>
                <th>Date</th>
                <th>Departure Point</th>
                <th>Participant Count</th>
                <th>Selected Add-ons</th>
                <th>Total Price</th>
                <th>Current Reservation Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                    You have no reservations yet.
                  </td>
                </tr>
              ) : (
                bookings.map((booking) => (
                  <tr key={booking.id}>
                    <td style={{ fontWeight: 600, color: '#111827' }}>{booking.tourTitle}</td>
                    <td>{formatDateTime(booking.startTime)}</td>
                    <td>{booking.departurePoint ? `📍 ${booking.departurePoint}` : '—'}</td>
                    <td>{booking.participants} person(s)</td>
                    <td>
                      {booking.addons && booking.addons.length > 0 ? (
                        <span style={{ fontSize: '0.85rem', color: '#4b5563' }}>
                          {booking.addons.join(', ')}
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.85rem', color: '#9ca3af' }}>None</span>
                      )}
                    </td>
                    <td style={{ fontWeight: 600, color: '#0f172a' }}>€{booking.totalPrice}</td>
                    <td>
                      <span className={getStatusBadgeClass(statusLabel(booking.status))}>
                        {statusLabel(booking.status)}
                      </span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-sm btn-secondary"
                        style={{ color: '#b91c1c' }}
                        onClick={() => handleCancel(booking)}
                        disabled={!canCancel(booking) || cancellingId === booking.id}
                      >
                        {cancellingId === booking.id ? 'Cancelling…' : 'Cancel'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default MyBookingsPage;
