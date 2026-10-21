function TourDetailsPage({ navigate, tourId, tours = [] }) {
  const selectedTour = tours.find((t) => String(t.id) === String(tourId)) || tours[0];

  return (
    <div style={{ padding: '1.5rem' }}>
      <h2>Tour Details & Booking Screen (/tours/{tourId || ':id'})</h2>
      <p>Placeholder for excursion details, add-on options, and booking submission.</p>
      {selectedTour ? (
        <div style={{ border: '1px solid #ccc', padding: '1rem', borderRadius: '4px', maxWidth: '600px', marginTop: '1rem' }}>
          <h3>{selectedTour.title}</h3>
          <p><strong>Category:</strong> {selectedTour.activityCategory}</p>
          <p><strong>Location:</strong> {selectedTour.location}</p>
          <p><strong>Price:</strong> {selectedTour.price} EUR</p>
          <p><strong>Description:</strong> {selectedTour.description}</p>
        </div>
      ) : (
        <p>Tour with ID {tourId} not found.</p>
      )}
      <div style={{ marginTop: '1rem' }}>
        <button onClick={() => navigate('/tours')}>← Back to Tour Search</button>
      </div>
    </div>
  );
}

export default TourDetailsPage;
