function TourSearchPage({ navigate, tours = [] }) {
  return (
    <div style={{ padding: '1.5rem' }}>
      <h2>Tour Search Screen (/tours)</h2>
      <p>Placeholder for tour discovery, search filters, and results list.</p>
      <p>Total tours loaded from mock data: <strong>{tours.length}</strong></p>
      <div style={{ marginTop: '1rem' }}>
        <h3>Available Mock Tours:</h3>
        <ul>
          {tours.map((tour) => (
            <li key={tour.id} style={{ marginBottom: '0.5rem' }}>
              <strong>{tour.title}</strong> — {tour.location} ({tour.price} EUR)
              {' '}
              <button onClick={() => navigate(`/tours/${tour.id}`)}>
                View Details
              </button>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default TourSearchPage;
