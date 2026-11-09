
import { useState, useId, useEffect } from 'react';
import { GetData } from '../api/baseApiClient.js';
import { getTripById } from '../api/tripClient.js';
import { createReservation, formatDateTime } from '../api/reservationClient.js';


const ADD_ON_PRICES = {
  "Audio guide": 8,
  "Museum entry ticket": 15,
  "Transport from Zagreb": 20,
  "Packed lunch": 12,
  "Hiking pole rental": 7,
  "Bicycle rental": 15,
  "Helmet rental": 5,
  "Transport from Omiš": 15,
  "Wetsuit rental": 10,
  "Waterproof bag rental": 6,
  "Snorkelling equipment rental": 12,
  "Lunch": 18,
  "Hotel transfer": 25,
  "City walls entry ticket": 35,
  "Refreshments": 8,
  "Local food tasting": 22,
  "Kayak rental": 20,
};

function formatSlotDate(isoString) {
  try {
    const d = new Date(isoString);
    return d.toLocaleString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return isoString;
  }
}

function TourDetailsPage({ navigate, tourId, tours = [], userRole = 'unregistered' }) {
  const isUnregistered = userRole === 'unregistered';
  const slotSelectId = useId();
  const participantsInputId = useId();

  const initialTour = tours.find((t) => String(t.id) === String(tourId)) || null;

  const [selectedTour, setSelectedTour] = useState(initialTour);
  const [loading, setLoading] = useState(!initialTour);
  const [error, setError] = useState(null);

  // Podaci potrebni za rezervaciju: termini i usluge s ID-jevima.
  const [details, setDetails] = useState(undefined);
  const [reloadKey, setReloadKey] = useState(0);

  const [selectedSlotKey, setSelectedSlotKey] = useState('');
  const [participants, setParticipants] = useState(1);
  const [selectedAddons, setSelectedAddons] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [bookingError, setBookingError] = useState('');
  const [confirmation, setConfirmation] = useState(null);

  // Dohvat osnovnih podataka o izletu.
  useEffect(() => {
    if (!tourId) return undefined;

    let isMounted = true;

    getTripById(tourId)
      .then((data) => {
        if (isMounted) {
          setSelectedTour(data);
          setError(null);
        }
      })
      .catch((err) => {
        if (isMounted) {
          setError(err.message);
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [tourId]);

  const selectedTourId = selectedTour?.id ?? tourId;

  // Dohvat termina i dodatnih usluga potrebnih za stvarnu rezervaciju.
  useEffect(() => {
    if (selectedTourId === undefined || selectedTourId === null) {
      return undefined;
    }

    let cancelled = false;

    GetData(`/trips/${selectedTourId}`)
      .then((data) => {
        if (!cancelled) {
          setDetails(data);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setDetails(null);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [selectedTourId, reloadKey]);

  if (loading) {
    return (
      <div className="page-container" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
        <h2>Loading Tour Details...</h2>
        <p style={{ color: '#6b7280' }}>Fetching latest information from server...</p>
      </div>
    );
  }

  if (error || !selectedTour) {
    return (
      <div className="page-container">
        <h2>Tour Not Found</h2>
        <p>{error || `No tour matches ID "${tourId}".`}</p>
        <button type="button" className="btn btn-primary" onClick={() => navigate('/tours')}>
          ← Return to Tour Search
        </button>
      </div>
    );
  }

  const backendReady = Boolean(details);

  // Termini s backenda imaju ID potreban za rezervaciju.
  const slotOptions = backendReady
    ? (details.scheduledSessions || [])
        .filter((session) => session.active !== false)
        .map((session) => ({
          key: String(session.id),
          sessionId: session.id,
          startTime: session.startTime,
          availableSeats: session.availableSeats,
        }))
    : (selectedTour.scheduledTimeSlots || []).map((slot) => ({
        key: slot,
        sessionId: null,
        startTime: slot,
        availableSeats: null,
      }));

  // Dodatne usluge s backenda imaju ID-jeve i stvarne cijene.
  const addonOptions = backendReady
    ? (details.additionalServices || []).map((service) => ({
        key: String(service.id),
        id: service.id,
        name: service.name,
        price: Number(service.price),
      }))
    : (selectedTour.additionalServices || []).map((name) => ({
        key: name,
        id: null,
        name,
        price: ADD_ON_PRICES[name] || 10,
      }));

  const currentSlot =
    slotOptions.find((slot) => slot.key === selectedSlotKey) ||
    slotOptions[0] ||
    null;

  const maxForSlot =
    currentSlot && currentSlot.availableSeats != null
      ? Math.max(
          1,
          Math.min(
            Number(selectedTour.maxParticipants) || 1,
            currentSlot.availableSeats
          )
        )
      : Number(selectedTour.maxParticipants) || 1;

  const handleToggleAddon = (key) => {
    setSelectedAddons((prev) =>
      prev.includes(key)
        ? prev.filter((k) => k !== key)
        : [...prev, key]
    );
  };

  const handleParticipantsChange = (e) => {
    const val = parseInt(e.target.value, 10);
    if (isNaN(val) || val < 1) {
      setParticipants(1);
    } else if (val > maxForSlot) {
      setParticipants(maxForSlot);
    } else {
      setParticipants(val);
    }
  };

  const participantCount = Math.min(participants, maxForSlot);
  const baseTotal = (Number(selectedTour.price) || 0) * participantCount;

  const addonsPerPerson = addonOptions
    .filter((addon) => selectedAddons.includes(addon.key))
    .reduce((sum, addon) => sum + addon.price, 0);

  const addonsTotal = addonsPerPerson * participantCount;
  const grandTotal = Number((baseTotal + addonsTotal).toFixed(2));

  const canBook = backendReady && currentSlot !== null && currentSlot.sessionId !== null;

  const handleBookReservation = async (e) => {
    e.preventDefault();
    if (!canBook || submitting) return;

    setSubmitting(true);
    setBookingError('');
    setConfirmation(null);
    try {
      const addonIds = addonOptions
        .filter((addon) => selectedAddons.includes(addon.key))
        .map((addon) => addon.id);

      const reservation = await createReservation({
        sessionId: currentSlot.sessionId,
        participants: participantCount,
        addonIds,
      });

      setConfirmation(reservation);
      setSelectedAddons([]);
      setParticipants(1);
      setReloadKey((k) => k + 1); // osvježi broj slobodnih mjesta
    } catch (err) {
      setBookingError(err.message);
      if (err.status === 409) {
        setReloadKey((k) => k + 1); // npr. netko je u međuvremenu popunio termin
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page-container">
      <div style={{ marginBottom: '1.25rem' }}>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={() => navigate('/tours')}
          style={{ fontSize: '0.875rem', padding: '0.4rem 0.8rem' }}
        >
          ← Back to Tour Search
        </button>
      </div>

      <div className="tour-details-layout">
        <section className="details-column" aria-labelledby="tour-title">
          <span className="tour-card-badge" style={{ marginBottom: '0.75rem' }}>
            {selectedTour.activityCategory}
          </span>
          <h2 id="tour-title" style={{ margin: '0 0 1rem 0', fontSize: '1.85rem', color: '#111827', lineHeight: 1.25 }}>
            {selectedTour.title}
          </h2>

          <div style={{ borderBottom: '1px solid #e5e7eb', paddingBottom: '1.25rem', marginBottom: '1.25rem' }}>
            <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.1rem', color: '#374151' }}>Overview</h3>
            <p style={{ margin: 0, color: '#4b5563', lineHeight: 1.6, fontSize: '0.95rem' }}>
              {selectedTour.description}
            </p>
          </div>

          <h3 style={{ margin: '0 0 1rem 0', fontSize: '1.1rem', color: '#374151' }}>
            Tour Information &amp; Logistics
          </h3>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
            <div style={{ padding: '0.75rem', background: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
              <span style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: 600 }}>DURATION</span>
              <strong style={{ fontSize: '1rem', color: '#0f172a' }}>{selectedTour.duration}</strong>
            </div>

            <div style={{ padding: '0.75rem', background: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
              <span style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: 600 }}>PRICE PER PERSON</span>
              <strong style={{ fontSize: '1rem', color: '#0f172a' }}>€{selectedTour.price}</strong>
            </div>

            <div style={{ padding: '0.75rem', background: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
              <span style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: 600 }}>MAXIMUM CAPACITY</span>
              <strong style={{ fontSize: '1rem', color: '#0f172a' }}>{selectedTour.maxParticipants} participants</strong>
            </div>

            <div style={{ padding: '0.75rem', background: '#f8fafc', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
              <span style={{ display: 'block', fontSize: '0.8rem', color: '#64748b', fontWeight: 600 }}>CATEGORY</span>
              <strong style={{ fontSize: '1rem', color: '#0f172a' }}>{selectedTour.activityCategory}</strong>
            </div>
          </div>

          <div style={{ marginTop: '1.25rem', padding: '1rem', background: '#f9fafb', borderRadius: '6px', border: '1px solid #e5e7eb' }}>
            <div style={{ marginBottom: '0.5rem' }}>
              <span style={{ fontSize: '0.85rem', color: '#6b7280', fontWeight: 600 }}>DEPARTURE POINT</span>
              <div style={{ fontSize: '0.95rem', color: '#1f2937', fontWeight: 500 }}>
                📍 {selectedTour.departurePoint || selectedTour.location || 'Not specified'}
              </div>
            </div>
            <div>
              <span style={{ fontSize: '0.85rem', color: '#6b7280', fontWeight: 600 }}>LOCATION</span>
              <div style={{ fontSize: '0.95rem', color: '#1f2937', fontWeight: 500 }}>
                🗺️ {selectedTour.location}
              </div>
            </div>
          </div>
        </section>

        <aside className="booking-column" aria-label="Tour Reservation Form">
          <h3 style={{ margin: '0 0 1rem 0', fontSize: '1.35rem', color: '#111827' }}>
            Book Your Spot
          </h3>

          <form onSubmit={handleBookReservation}>
            <div style={{ marginBottom: '1.25rem' }}>
              <label htmlFor={slotSelectId} className="form-label">
                Select Scheduled Time Slot
              </label>

              <select
                id={slotSelectId}
                className="form-select"
                value={currentSlot ? currentSlot.key : ''}
                onChange={(e) => setSelectedSlotKey(e.target.value)}
                disabled={isUnregistered || submitting}
              >
                {slotOptions.length === 0 && (
                  <option value="">No upcoming time slots</option>
                )}

                {slotOptions.map((slot) => (
                  <option key={slot.key} value={slot.key}>
                    {formatSlotDate(slot.startTime)}
                    {slot.availableSeats != null
                      ? ` — ${slot.availableSeats} seats left`
                      : ''}
                  </option>
                ))}
              </select>
            </div>

            <div style={{ marginBottom: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <label htmlFor={participantsInputId} className="form-label" style={{ margin: 0 }}>
                  Number of Participants
                </label>
                <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                  Max: {maxForSlot}
                </span>
              </div>
              <input
                id={participantsInputId}
                type="number"
                className="form-input"
                min="1"
                max={maxForSlot}
                value={participantCount}
                onChange={handleParticipantsChange}
                disabled={isUnregistered || submitting}
              />
            </div>

<div style={{ marginBottom: '1.25rem' }}>
  <label className="form-label">Optional Add-on Services</label>

  {addonOptions.length > 0 ? (
    <div>
      {addonOptions.map((service) => {
        const isChecked = selectedAddons.includes(service.key);

        return (
          <label
            key={service.key}
            className="service-checkbox-item"
            style={{ cursor: isUnregistered ? 'not-allowed' : 'pointer' }}
          >
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.9rem' }}>
              <input
                type="checkbox"
                checked={isChecked}
                onChange={() => handleToggleAddon(service.key)}
                disabled={isUnregistered || submitting}
              />
              <span>{service.name}</span>
            </span>

            <strong style={{ fontSize: '0.85rem', color: '#0369a1' }}>
              +€{service.price} / person
            </strong>
          </label>
        );
      })}
    </div>
  ) : (
    <p style={{ fontSize: '0.85rem', color: '#6b7280', margin: 0 }}>
      No extra services available for this tour.
    </p>
  )}
</div>
            <div className="price-summary-box">
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: '#4b5563', marginBottom: '0.35rem' }}>
                <span>Base ({participantCount} × €{selectedTour.price}):</span>
                <span>€{baseTotal}</span>
              </div>
              {selectedAddons.length > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: '#4b5563', marginBottom: '0.35rem' }}>
                  <span>Add-ons ({participantCount} × €{addonsPerPerson}):</span>
                  <span>€{addonsTotal}</span>
                </div>
              )}
              <hr style={{ border: 'none', borderTop: '1px solid #bbf7d0', margin: '0.5rem 0' }} />
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontWeight: 700, fontSize: '1rem', color: '#166534' }}>
                  Total Price:
                </span>
                <span style={{ fontWeight: 800, fontSize: '1.5rem', color: '#166534' }}>
                  €{grandTotal}
                </span>
              </div>
            </div>

            {isUnregistered ? (
              <div>
                <button
                  type="button"
                  className="btn btn-secondary btn-block"
                  disabled
                  style={{ padding: '0.8rem 1rem', fontSize: '1.05rem', cursor: 'not-allowed', opacity: 0.7 }}
                >
                  Book / Confirm Reservation (Login Required)
                </button>
                <p style={{ margin: '0.6rem 0 0 0', fontSize: '0.85rem', color: '#6b7280', textAlign: 'center' }}>
                  Please{' '}
                  <button
                    type="button"
                    onClick={() => navigate('/login')}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#0066cc',
                      textDecoration: 'underline',
                      cursor: 'pointer',
                      padding: 0,
                      font: 'inherit',
                      fontWeight: 600,
                    }}
                  >
                    log in
                  </button>{' '}
                  to reserve this tour.
                </p>
              </div>
            ) : (
              <>
                {bookingError && (
                  <div className="error-banner" role="alert">
                    {bookingError}
                  </div>
                )}
                {confirmation && (
                  <div className="toast-banner" role="status" style={{ display: 'block' }}>
                    <strong>Reservation #{confirmation.id} confirmed!</strong>
                    <div style={{ marginTop: '0.25rem' }}>
                      {confirmation.tourTitle}, {formatDateTime(confirmation.startTime)},{' '}
                      {confirmation.participants} person(s), total €{confirmation.totalPrice}.
                      A confirmation e-mail is on its way.
                    </div>
                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      style={{ marginTop: '0.5rem' }}
                      onClick={() => navigate('/my-bookings')}
                    >
                      View My Bookings
                    </button>
                  </div>
                )}
                {details === null && (
                  <p style={{ margin: '0 0 0.6rem 0', fontSize: '0.85rem', color: '#b45309' }}>
                    Booking is currently unavailable for this tour.
                  </p>
                )}
                <button
                  type="submit"
                  className="btn btn-primary btn-block"
                  style={{ padding: '0.8rem 1rem', fontSize: '1.05rem' }}
                  disabled={!canBook || submitting}
                >
                  {submitting ? 'Booking…' : 'Book / Confirm Reservation'}
                </button>
              </>
            )}
          </form>
        </aside>
      </div>
    </div>
  );
}

export default TourDetailsPage;
