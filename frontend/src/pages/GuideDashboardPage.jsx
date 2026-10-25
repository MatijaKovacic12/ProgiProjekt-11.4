const mockGuideSchedule = [
  {
    id: 'GS-1',
    tourTitle: 'Zagreb Historic Centre Walking Tour',
    dateTime: '2026-11-07 10:00',
    departurePoint: 'Ban Jelačić Square, Zagreb',
  },
  {
    id: 'GS-2',
    tourTitle: 'Plitvice Lakes Guided Hiking Tour',
    dateTime: '2026-11-08 08:30',
    departurePoint: 'Entrance 1, Plitvice Lakes National Park',
  },
  {
    id: 'GS-3',
    tourTitle: 'Cetina River Rafting Adventure',
    dateTime: '2027-05-02 09:00',
    departurePoint: 'Omiš town bridge, Omiš',
  },
];

function GuideDashboardPage() {
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

      <div className="data-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>Tour Name</th>
              <th>Departure Date and Time</th>
              <th>Departure Point</th>
            </tr>
          </thead>
          <tbody>
            {mockGuideSchedule.map((slot) => (
              <tr key={slot.id}>
                <td style={{ fontWeight: 600, color: '#111827' }}>{slot.tourTitle}</td>
                <td>{slot.dateTime}</td>
                <td>📍 {slot.departurePoint}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default GuideDashboardPage;
