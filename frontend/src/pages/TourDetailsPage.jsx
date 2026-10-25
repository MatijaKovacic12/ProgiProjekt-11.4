import { useState, useId } from 'react';

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

  const selectedTour = tours.find((t) => String(t.id) === String(tourId)) || tours[0];

  const [selectedSlot, setSelectedSlot] = useState(
    selectedTour?.scheduledTimeSlots?.[0] || ''
  );
  const [participants, setParticipants] = useState(1);
  const [selectedAddons, setSelectedAddons] = useState([]);

  if (!selectedTour) {
    return (
      <div className="page-container">
        <h2>Tour Not Found</h2>
        <p>No tour matches ID &quot;{tourId}&quot;.</p>
        <button type="button" className="btn btn-primary" onClick={() => navigate('/tours')}>
          ← Return to Tour Search
        </button>
      </div>
    );
  }

  // if service already selected -> remove it, else -> add it to list
  const handleToggleAddon = (service) => {
    setSelectedAddons((prev) => // prev = selectedAddons
      prev.includes(service) ? prev.filter((s) => s !== service) : [...prev, service]
    );
  };

  const handleParticipantsChange = (e) => {
    const val = parseInt(e.target.value, 10);
    if (isNaN(val) || val < 1) {
      setParticipants(1);
    } else if (val > selectedTour.maxParticipants) {
      setParticipants(selectedTour.maxParticipants);
    } else {
      setParticipants(val);
    }
  };

  const baseTotal = selectedTour.price * participants;
  const addonsPerPerson = selectedAddons.reduce(
    (sum, service) => sum + (ADD_ON_PRICES[service] || 10),
    0
  );
  const addonsTotal = addonsPerPerson * participants;
  const grandTotal = baseTotal + addonsTotal;

  const handleBookReservation = (e) => {
    e.preventDefault();
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
                📍 {selectedTour.departurePoint}
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
                value={selectedSlot}
                onChange={(e) => setSelectedSlot(e.target.value)}
                disabled={isUnregistered}
              >
                {selectedTour.scheduledTimeSlots.map((slot) => (
                  <option key={slot} value={slot}>
                    {formatSlotDate(slot)}
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
                  Max: {selectedTour.maxParticipants}
                </span>
              </div>
              <input
                id={participantsInputId}
                type="number"
                className="form-input"
                min="1"
                max={selectedTour.maxParticipants}
                value={participants}
                onChange={handleParticipantsChange}
                disabled={isUnregistered}
              />
            </div>

            <div style={{ marginBottom: '1.25rem' }}>
              <label className="form-label">Optional Add-on Services</label>
              {selectedTour.additionalServices && selectedTour.additionalServices.length > 0 ? (
                <div>
                  {selectedTour.additionalServices.map((service) => {
                    const price = ADD_ON_PRICES[service] || 10;
                    const isChecked = selectedAddons.includes(service);
                    return (
                      <label
                        key={service}
                        className="service-checkbox-item"
                        style={{ cursor: isUnregistered ? 'not-allowed' : 'pointer' }}
                      >
                        <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.9rem' }}>
                          <input
                            type="checkbox"
                            checked={isChecked}
                            onChange={() => handleToggleAddon(service)}
                            disabled={isUnregistered}
                          />
                          <span>{service}</span>
                        </span>
                        <strong style={{ fontSize: '0.85rem', color: '#0369a1' }}>
                          +€{price} / person
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
                <span>Base ({participants} × €{selectedTour.price}):</span>
                <span>€{baseTotal}</span>
              </div>
              {selectedAddons.length > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: '#4b5563', marginBottom: '0.35rem' }}>
                  <span>Add-ons ({participants} × €{addonsPerPerson}):</span>
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
              <button
                type="submit"
                className="btn btn-primary btn-block"
                style={{ padding: '0.8rem 1rem', fontSize: '1.05rem' }}
              >
                Book / Confirm Reservation
              </button>
            )}
          </form>
        </aside>
      </div>
    </div>
  );
}

export default TourDetailsPage;
