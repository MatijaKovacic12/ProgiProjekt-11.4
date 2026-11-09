import { useEffect, useMemo, useState } from 'react';
import { GetData } from '../api/baseApiClient.js';
import {
  adminCancelReservation,
  formatDateTime,
  searchAdminReservations,
  statusLabel,
} from '../api/reservationClient.js';

const PAGE_SIZE = 10;

function getStatusBadgeClass(status) {
  switch (status) {
    case 'Confirmed':
      return 'badge badge-success';
    case 'Cancelled':
      return 'badge badge-danger';
    case 'Completed':
      return 'badge badge-info';
    default:
      return 'badge badge-neutral';
  }
}

function AdminReservations() {
  const [searchInput, setSearchInput] = useState('');
  const [q, setQ] = useState('');
  const [tripId, setTripId] = useState('');
  const [status, setStatus] = useState('');
  const [createdDate, setCreatedDate] = useState('');
  const [page, setPage] = useState(0);
  const [reloadKey, setReloadKey] = useState(0);

  const [trips, setTrips] = useState([]);
  const [result, setResult] = useState({ key: null, data: null, error: '' });
  const [message, setMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    let cancelled = false;
    GetData('/trips').then((data) => {
        if (!cancelled) setTrips(data);
      })
      .catch(() => {
        if (!cancelled) setTrips([]);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      setQ(searchInput);
      setPage(0);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchInput]);

  const filters = useMemo(
    () => ({ q, tripId, status, createdDate, page, reloadKey }),
    [q, tripId, status, createdDate, page, reloadKey]
  );
  const requestKey = JSON.stringify(filters);

  useEffect(() => {
    let cancelled = false;
    searchAdminReservations({
      q: filters.q,
      tripId: filters.tripId,
      status: filters.status,
      createdDate: filters.createdDate,
      page: filters.page,
      size: PAGE_SIZE
    })
      .then((data) => {
        if (!cancelled) setResult({ key: requestKey, data, error: '' });
      })
      .catch((e) => {
        if (!cancelled) setResult({ key: requestKey, data: null, error: e.message });
      });
    return () => {
      cancelled = true;
    };
  }, [filters, requestKey]);

  const loading = result.key !== requestKey;
  const reservations = result.data?.content || [];
  const totalElements = result.data?.totalElements ?? 0;
  const totalPages = result.data?.totalPages ?? 0;

  const handleCancel = async (reservation) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return;
    
    setCancellingId(reservation.id);
    setActionError('');
    setMessage('');
    try {
      await adminCancelReservation(reservation.id);
      setMessage(`Reservation #${reservation.id} has been cancelled.`);
      setReloadKey((k) => k + 1);
    } catch (e) {
      setActionError(e.message);
    } finally {
      setCancellingId(null);
    }
  };

  const handleResetFilters = () => {
    setSearchInput('');
    setQ('');
    setTripId('');
    setStatus('');
    setCreatedDate('');
    setPage(0);
  };

  const hasFilters = Boolean(searchInput || tripId || status || createdDate);

  return (
    <section style={{ marginBottom: '2.5rem' }}>
      <div className="section-header-row">
        <div>
          <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
            Bookings Management
          </h3>
        </div>
      </div>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '0.75rem',
          marginBottom: '1rem',
          backgroundColor: '#f8fafc',
          padding: '1rem',
          borderRadius: '8px',
          border: '1px solid #e2e8f0',
          alignItems: 'flex-end',
        }}
      >
        <div>
          <label className="form-label" htmlFor="booking-search-input">Search Client</label>
          <input
            id="booking-search-input"
            type="text"
            className="form-input"
            placeholder="Search name or email..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
        </div>

        <div>
          <label className="form-label" htmlFor="booking-tour-filter">Filter by Tour</label>
          <select
            id="booking-tour-filter"
            className="form-select"
            value={tripId}
            onChange={(e) => {
              setTripId(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All Tours</option>
            {trips.map((trip) => (
              <option key={trip.id} value={trip.id}>
                {trip.title || trip.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="form-label" htmlFor="booking-status-filter">Filter by Status</label>
          <select
            id="booking-status-filter"
            className="form-select"
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All Statuses</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>

        <div>
          <label className="form-label" htmlFor="booking-date-filter">Filter by Creation Date</label>
          <input
            id="booking-date-filter"
            type="date"
            className="form-input"
            value={createdDate}
            onChange={(e) => {
              setCreatedDate(e.target.value);
              setPage(0);
            }}
          />
        </div>

        {hasFilters && (
          <div>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={handleResetFilters}
              style={{ padding: '0.55rem' }}
            >
              Reset Filters
            </button>
          </div>
        )}
      </div>

      {message && (
        <div className="toast-banner" role="status">
          <span>{message}</span>
          <button type="button" className="btn btn-sm btn-secondary" onClick={() => setMessage('')}>
            ✕
          </button>
        </div>
      )}

      {(actionError || result.error) && (
        <div className="error-banner" role="alert">
          {actionError || result.error}
        </div>
      )}

      <div style={{ marginBottom: '0.5rem', fontSize: '0.85rem', color: '#64748b' }}>
        {loading ? 'Loading reservations…' : `Showing ${reservations.length} of ${totalElements} reservation(s)`}
      </div>

      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Booking ID</th>
              <th>Tour Name</th>
              <th>Tour Date</th>
              <th>User Name / Email</th>
              <th>Participant Count</th>
              <th>Selected Add-ons</th>
              <th>Total Price</th>
              <th>Reservation Status</th>
              <th style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {reservations.length > 0 ? (
              reservations.map((booking) => (
                <tr key={booking.id}>
                  <td style={{ fontWeight: 600, color: '#0066cc' }}>{booking.id}</td>
                  <td style={{ fontWeight: 500 }}>{booking.tourTitle}</td>
                  <td>{formatDateTime(booking.startTime)}</td>
                  <td>
                    <div style={{ fontWeight: 500 }}>{booking.userName}</div>
                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{booking.userEmail}</div>
                  </td>
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
                  <td style={{ textAlign: 'right' }}>
                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      style={{ color: '#b91c1c' }}
                      onClick={() => handleCancel(booking)}
                      disabled={booking.status !== 'CONFIRMED' || cancellingId === booking.id}
                      title="Cancel this reservation"
                    >
                      {cancellingId === booking.id ? 'Cancelling…' : 'Cancel'}
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="9" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                  {loading
                    ? 'Loading…'
                    : 'No reservations match the selected search or filter criteria.'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '0.75rem',
            marginTop: '0.75rem',
          }}
        >
          <button
            type="button"
            className="btn btn-sm btn-secondary"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0 || loading}
          >
            ← Previous
          </button>
          <span style={{ fontSize: '0.85rem', color: '#475569' }}>
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            className="btn btn-sm btn-secondary"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page + 1 >= totalPages || loading}
          >
            Next →
          </button>
        </div>
      )}
    </section>
  );
}

export default AdminReservations;