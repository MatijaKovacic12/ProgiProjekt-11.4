import { useState } from 'react';
import { tours as initialTours, initialBookings } from '../mock-backoffice-data.js';

const AVAILABLE_ADDONS = [
  'Audio guide',
  'Museum entry ticket',
  'Transport from Zagreb',
  'Packed lunch',
  'Hiking pole rental',
  'Bicycle rental',
  'Helmet rental',
  'Transport from Omiš',
  'Wetsuit rental',
  'Waterproof bag rental',
  'Snorkelling equipment rental',
  'Lunch',
  'City walls entry ticket',
  'Local food tasting',
  'Kayak rental',
];

function AdminManagementPage() {
  const [toursList, setToursList] = useState(initialTours);
  const [bookingsList, setBookingsList] = useState(initialBookings);
  const [notification, setNotification] = useState(null);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTourId, setEditingTourId] = useState(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    activityCategory: 'City tour',
    duration: '',
    departurePoint: '',
    price: '',
    maxParticipants: '',
    scheduledTimeSlots: [],
    additionalServices: [],
  });

  // Dropdown slot builder state
  const [slotPicker, setSlotPicker] = useState({
    year: '2026',
    month: '11',
    day: '15',
    hour: '10',
    minute: '00',
  });

  const showNotice = (msg) => {
    setNotification(msg);
    setTimeout(() => {
      setNotification(null);
    }, 4000);
  };

  const handleOpenAddModal = () => {
    setEditingTourId(null);
    setFormData({
      title: '',
      description: '',
      activityCategory: 'City tour',
      duration: '3 hours',
      departurePoint: 'Main Square, Zagreb',
      price: 35,
      maxParticipants: 15,
      scheduledTimeSlots: ['2026-11-20 10:00', '2026-11-27 10:00'],
      additionalServices: ['Audio guide'],
    });
    setSlotPicker({
      year: '2026',
      month: '11',
      day: '15',
      hour: '10',
      minute: '00',
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (tour) => {
    setEditingTourId(tour.id);
    const existingSlots = Array.isArray(tour.scheduledTimeSlots)
      ? tour.scheduledTimeSlots.map((s) => s.replace('T', ' ').slice(0, 16))
      : (typeof tour.scheduledTimeSlots === 'string'
          ? tour.scheduledTimeSlots.split(',').map((s) => s.trim()).filter(Boolean)
          : []);

    setFormData({
      title: tour.title || '',
      description: tour.description || '',
      activityCategory: tour.activityCategory || 'City tour',
      duration: tour.duration || '',
      departurePoint: tour.departurePoint || '',
      price: tour.price || 0,
      maxParticipants: tour.maxParticipants || 10,
      scheduledTimeSlots: existingSlots,
      additionalServices: tour.additionalServices || [],
    });
    setSlotPicker({
      year: '2026',
      month: '11',
      day: '15',
      hour: '10',
      minute: '00',
    });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingTourId(null);
  };

  const handleAddSlot = (e) => {
    e.preventDefault();
    const formattedSlot = `${slotPicker.year}-${slotPicker.month}-${slotPicker.day} ${slotPicker.hour}:${slotPicker.minute}`;
    if (!formData.scheduledTimeSlots.includes(formattedSlot)) {
      setFormData((prev) => ({
        ...prev,
        scheduledTimeSlots: [...prev.scheduledTimeSlots, formattedSlot],
      }));
    }
  };

  const handleRemoveSlot = (slotToRemove) => {
    setFormData((prev) => ({
      ...prev,
      scheduledTimeSlots: prev.scheduledTimeSlots.filter((s) => s !== slotToRemove),
    }));
  };

  const handleToggleAddon = (addon) => {
    setFormData((prev) => {
      const exists = prev.additionalServices.includes(addon);
      return {
        ...prev,
        additionalServices: exists
          ? prev.additionalServices.filter((a) => a !== addon)
          : [...prev.additionalServices, addon],
      };
    });
  };

  const handleSaveTour = (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      alert('Please enter a tour title.');
      return;
    }

    if (editingTourId) {
      setToursList((prev) =>
        prev.map((t) =>
          t.id === editingTourId
            ? {
                ...t,
                ...formData,
                price: Number(formData.price),
                maxParticipants: Number(formData.maxParticipants),
                scheduledTimeSlots: formData.scheduledTimeSlots,
              }
            : t
        )
      );
      showNotice(`Tour "${formData.title}" updated successfully.`);
    } else {
      const newTour = {
        id: Date.now(),
        ...formData,
        price: Number(formData.price) || 25,
        maxParticipants: Number(formData.maxParticipants) || 12,
        requiredGuideCount: 1,
        scheduledTimeSlots: formData.scheduledTimeSlots.length > 0
          ? formData.scheduledTimeSlots
          : ['2026-11-20 10:00'],
      };
      setToursList((prev) => [newTour, ...prev]);
      showNotice(`New tour "${formData.title}" created successfully.`);
    }

    handleCloseModal();
  };

  const handleChangeStatus = (bookingId, newStatus) => {
    setBookingsList((prev) =>
      prev.map((b) => (b.id === bookingId ? { ...b, status: newStatus } : b))
    );
    showNotice(`Booking ${bookingId} status changed to ${newStatus}.`);
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'Confirmed':
        return 'badge badge-success';
      case 'Request':
        return 'badge badge-warning';
      case 'Cancelled':
        return 'badge badge-danger';
      case 'Completed':
        return 'badge badge-info';
      default:
        return 'badge badge-neutral';
    }
  };

  return (
    <div className="page-container">
      {/* Screen Title & Role Description */}
      <div style={{ marginBottom: '1.75rem' }}>
        <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
          Administrator Management
        </h2>
        <p style={{ margin: 0, color: '#4b5563', fontSize: '0.95rem' }}>
          Manage catalogue tours, domain configurations, scheduled capacity, and customer reservations.
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

      {/* Tour Management */}
      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Tour Management
            </h3>
            <p style={{ margin: 0, color: '#6b7280', fontSize: '0.875rem' }}>
              Overview of all active excursions, domain parameters, pricing, and required guides.
            </p>
          </div>
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleOpenAddModal}
          >
            + Add New Tour
          </button>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Category</th>
                <th>Price</th>
                <th>Max Capacity</th>
                <th>Required Guides</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {toursList.map((tour) => (
                <tr key={tour.id}>
                  <td style={{ fontWeight: 600 }}>{tour.title}</td>
                  <td>
                    <span className="badge badge-info">{tour.activityCategory}</span>
                  </td>
                  <td>€{tour.price}</td>
                  <td>{tour.maxParticipants} participants</td>
                  <td>{tour.requiredGuideCount || 1} guide(s)</td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      onClick={() => handleOpenEditModal(tour)}
                    >
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Bookings Management */}
      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Bookings Management
            </h3>
            <p style={{ margin: 0, color: '#6b7280', fontSize: '0.875rem' }}>
              Review customer reservations, chosen add-on services, and update fulfillment statuses.
            </p>
          </div>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Booking ID</th>
                <th>Tour Name</th>
                <th>User Name</th>
                <th>Participant Count</th>
                <th>Selected Add-ons</th>
                <th>Reservation Status</th>
                <th style={{ textAlign: 'right' }}>Status Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookingsList.map((booking) => (
                <tr key={booking.id}>
                  <td style={{ fontWeight: 600, color: '#0066cc' }}>{booking.id}</td>
                  <td style={{ fontWeight: 500 }}>{booking.tourTitle}</td>
                  <td>{booking.userName}</td>
                  <td>{booking.participantCount} person(s)</td>
                  <td>
                    {booking.selectedAddons && booking.selectedAddons.length > 0 ? (
                      <span style={{ fontSize: '0.85rem', color: '#4b5563' }}>
                        {booking.selectedAddons.join(', ')}
                      </span>
                    ) : (
                      <span style={{ fontSize: '0.85rem', color: '#9ca3af' }}>None</span>
                    )}
                  </td>
                  <td>
                    <span className={getStatusBadgeClass(booking.status)}>
                      {booking.status}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div className="btn-group" style={{ justifyContent: 'flex-end' }}>
                      <button
                        type="button"
                        className="btn btn-sm btn-secondary"
                        onClick={() => handleChangeStatus(booking.id, 'Confirmed')}
                        disabled={booking.status === 'Confirmed'}
                        title="Mark as Confirmed"
                      >
                        Confirm
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-secondary"
                        onClick={() => handleChangeStatus(booking.id, 'Completed')}
                        disabled={booking.status === 'Completed'}
                        title="Mark as Completed"
                      >
                        Complete
                      </button>
                      <button
                        type="button"
                        className="btn btn-sm btn-secondary"
                        style={{ color: '#b91c1c' }}
                        onClick={() => handleChangeStatus(booking.id, 'Cancelled')}
                        disabled={booking.status === 'Cancelled'}
                        title="Mark as Cancelled"
                      >
                        Cancel
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Add / Edit Tour Modal Placeholder with ALL domain fields */}
      {isModalOpen && (
        <div className="modal-backdrop" onClick={handleCloseModal}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3 style={{ margin: 0, fontSize: '1.25rem', color: '#111827' }}>
                {editingTourId ? 'Edit Tour Specification' : 'Add New Tour'}
              </h3>
              <button
                type="button"
                className="btn btn-sm btn-secondary"
                onClick={handleCloseModal}
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSaveTour}>
              <div className="modal-body">
                {/* 1. Title */}
                <div>
                  <label className="form-label" htmlFor="field-tour-title">
                    Tour Title *
                  </label>
                  <input
                    id="field-tour-title"
                    type="text"
                    className="form-input"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    placeholder="e.g. Istrian Truffle Hunting Excursion"
                    required
                  />
                </div>

                {/* 2. Description */}
                <div>
                  <label className="form-label" htmlFor="field-tour-description">
                    Description *
                  </label>
                  <textarea
                    id="field-tour-description"
                    className="form-input"
                    rows="3"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="Provide a detailed description of the tour route, highlights, and experience..."
                    required
                  />
                </div>

                {/* Grid row: Category & Duration */}
                <div className="form-grid-2">
                  {/* 3. Activity Category */}
                  <div>
                    <label className="form-label" htmlFor="field-tour-category">
                      Activity Category *
                    </label>
                    <select
                      id="field-tour-category"
                      className="form-select"
                      value={formData.activityCategory}
                      onChange={(e) => setFormData({ ...formData, activityCategory: e.target.value })}
                    >
                      <option value="City tour">City tour</option>
                      <option value="Hiking">Hiking</option>
                      <option value="Cycling">Cycling</option>
                      <option value="Rafting">Rafting</option>
                      <option value="Boat trip">Boat trip</option>
                      <option value="Kayaking">Kayaking</option>
                    </select>
                  </div>

                  {/* 4. Duration */}
                  <div>
                    <label className="form-label" htmlFor="field-tour-duration">
                      Duration *
                    </label>
                    <input
                      id="field-tour-duration"
                      type="text"
                      className="form-input"
                      value={formData.duration}
                      onChange={(e) => setFormData({ ...formData, duration: e.target.value })}
                      placeholder="e.g. 3.5 hours"
                      required
                    />
                  </div>
                </div>

                {/* 5. Departure Point */}
                <div>
                  <label className="form-label" htmlFor="field-tour-departure">
                    Departure Point *
                  </label>
                  <input
                    id="field-tour-departure"
                    type="text"
                    className="form-input"
                    value={formData.departurePoint}
                    onChange={(e) => setFormData({ ...formData, departurePoint: e.target.value })}
                    placeholder="e.g. Ban Jelačić Square, Zagreb"
                    required
                  />
                </div>

                {/* Grid row: Price & Capacity */}
                <div className="form-grid-2">
                  {/* 6. Price */}
                  <div>
                    <label className="form-label" htmlFor="field-tour-price">
                      Base Price per Person (€) *
                    </label>
                    <input
                      id="field-tour-price"
                      type="number"
                      className="form-input"
                      min="0"
                      step="1"
                      value={formData.price}
                      onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                      placeholder="e.g. 45"
                      required
                    />
                  </div>

                  {/* 7. Capacity */}
                  <div>
                    <label className="form-label" htmlFor="field-tour-capacity">
                      Max Capacity (Participants) *
                    </label>
                    <input
                      id="field-tour-capacity"
                      type="number"
                      className="form-input"
                      min="1"
                      value={formData.maxParticipants}
                      onChange={(e) => setFormData({ ...formData, maxParticipants: e.target.value })}
                      placeholder="e.g. 15"
                      required
                    />
                  </div>
                </div>

                {/* 8. Scheduled Slots with Dropdown Selection */}
                <div>
                  <label className="form-label">
                    Scheduled Time Slots *
                  </label>
                  <div
                    style={{
                      display: 'flex',
                      gap: '0.4rem',
                      flexWrap: 'wrap',
                      alignItems: 'center',
                      marginBottom: '0.6rem',
                    }}
                  >
                    <select
                      className="form-select"
                      style={{ width: 'auto' }}
                      value={slotPicker.year}
                      onChange={(e) => setSlotPicker({ ...slotPicker, year: e.target.value })}
                      aria-label="Select Year"
                    >
                      <option value="2026">2026</option>
                      <option value="2027">2027</option>
                      <option value="2028">2028</option>
                    </select>

                    <select
                      className="form-select"
                      style={{ width: 'auto' }}
                      value={slotPicker.month}
                      onChange={(e) => setSlotPicker({ ...slotPicker, month: e.target.value })}
                      aria-label="Select Month"
                    >
                      <option value="01">01 - Jan</option>
                      <option value="02">02 - Feb</option>
                      <option value="03">03 - Mar</option>
                      <option value="04">04 - Apr</option>
                      <option value="05">05 - May</option>
                      <option value="06">06 - Jun</option>
                      <option value="07">07 - Jul</option>
                      <option value="08">08 - Aug</option>
                      <option value="09">09 - Sep</option>
                      <option value="10">10 - Oct</option>
                      <option value="11">11 - Nov</option>
                      <option value="12">12 - Dec</option>
                    </select>

                    <select
                      className="form-select"
                      style={{ width: 'auto' }}
                      value={slotPicker.day}
                      onChange={(e) => setSlotPicker({ ...slotPicker, day: e.target.value })}
                      aria-label="Select Day"
                    >
                      {Array.from({ length: 31 }, (_, i) => String(i + 1).padStart(2, '0')).map((d) => (
                        <option key={d} value={d}>
                          {d}
                        </option>
                      ))}
                    </select>

                    <span style={{ fontWeight: 600, color: '#64748b', fontSize: '0.85rem' }}>at</span>

                    <select
                      className="form-select"
                      style={{ width: 'auto' }}
                      value={slotPicker.hour}
                      onChange={(e) => setSlotPicker({ ...slotPicker, hour: e.target.value })}
                      aria-label="Select Hour"
                    >
                      {Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0')).map((h) => (
                        <option key={h} value={h}>
                          {h}:00
                        </option>
                      ))}
                    </select>

                    <select
                      className="form-select"
                      style={{ width: 'auto' }}
                      value={slotPicker.minute}
                      onChange={(e) => setSlotPicker({ ...slotPicker, minute: e.target.value })}
                      aria-label="Select Minute"
                    >
                      <option value="00">00</option>
                      <option value="15">15</option>
                      <option value="30">30</option>
                      <option value="45">45</option>
                    </select>

                    <button
                      type="button"
                      className="btn btn-sm btn-secondary"
                      onClick={handleAddSlot}
                      style={{ whiteSpace: 'nowrap' }}
                    >
                      + Add Slot
                    </button>
                  </div>

                  {/* Configured Slots List (Chips) */}
                  <div
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: '0.4rem',
                      minHeight: '36px',
                      padding: '0.5rem',
                      backgroundColor: '#f8fafc',
                      border: '1px solid #e2e8f0',
                      borderRadius: '6px',
                    }}
                  >
                    {formData.scheduledTimeSlots.length > 0 ? (
                      formData.scheduledTimeSlots.map((slot) => (
                        <span
                          key={slot}
                          className="badge badge-info"
                          style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.4rem',
                            fontSize: '0.8rem',
                            padding: '0.25rem 0.6rem',
                          }}
                        >
                          <span>{slot}</span>
                          <button
                            type="button"
                            onClick={() => handleRemoveSlot(slot)}
                            style={{
                              background: 'none',
                              border: 'none',
                              color: '#0369a1',
                              cursor: 'pointer',
                              padding: 0,
                              fontWeight: 'bold',
                              fontSize: '0.85rem',
                              lineHeight: 1,
                            }}
                            title="Remove slot"
                          >
                            ×
                          </button>
                        </span>
                      ))
                    ) : (
                      <span style={{ fontSize: '0.8rem', color: '#94a3b8', fontStyle: 'italic' }}>
                        No time slots added yet. Select date &amp; time above and click &quot;+ Add Slot&quot;.
                      </span>
                    )}
                  </div>
                </div>

                {/* 9. Add-on Services */}
                <div>
                  <label className="form-label">
                    Add-on Services (Optional extras available for this tour)
                  </label>
                  <div className="form-checkbox-group">
                    {AVAILABLE_ADDONS.map((addon) => (
                      <label
                        key={addon}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '0.4rem',
                          fontSize: '0.85rem',
                          color: '#374151',
                          cursor: 'pointer',
                        }}
                      >
                        <input
                          type="checkbox"
                          checked={formData.additionalServices.includes(addon)}
                          onChange={() => handleToggleAddon(addon)}
                        />
                        <span>{addon}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={handleCloseModal}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  {editingTourId ? 'Save Changes' : 'Create Tour'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminManagementPage;
