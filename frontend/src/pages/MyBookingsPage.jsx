const mockClientBookings = [
  {
    id: 'MB-101',
    tourTitle: 'Plitvice Lakes Guided Hiking Tour',
    dateTime: '2026-11-08 08:30',
    departurePoint: 'Entrance 1, Plitvice Lakes National Park',
    participantCount: 2,
    selectedAddons: ['Transport from Zagreb', 'Packed lunch'],
    totalPrice: 194,
    status: 'Confirmed',
  },
  {
    id: 'MB-102',
    tourTitle: 'Zagreb Historic Centre Walking Tour',
    dateTime: '2026-10-15 10:00',
    departurePoint: 'Ban Jelačić Square, Zagreb',
    participantCount: 1,
    selectedAddons: ['Audio guide'],
    totalPrice: 33,
    status: 'Completed',
  },
  {
    id: 'MB-103',
    tourTitle: 'Split Coastline and Islands Boat Excursion',
    dateTime: '2026-10-22 09:30',
    departurePoint: 'Split Harbour, Pier 4',
    participantCount: 2,
    selectedAddons: ['Snorkelling equipment rental'],
    totalPrice: 164,
    status: 'Cancelled',
  },
];

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

function MyBookingsPage() {
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
            </tr>
          </thead>
          <tbody>
            {mockClientBookings.map((booking) => (
              <tr key={booking.id}>
                <td style={{ fontWeight: 600, color: '#111827' }}>{booking.tourTitle}</td>
                <td>{booking.dateTime}</td>
                <td>📍 {booking.departurePoint}</td>
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
                <td style={{ fontWeight: 600, color: '#0f172a' }}>€{booking.totalPrice}</td>
                <td>
                  <span className={getStatusBadgeClass(booking.status)}>
                    {booking.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default MyBookingsPage;
