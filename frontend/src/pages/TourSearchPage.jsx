import { useState } from 'react';

function TourSearchPage({ navigate, tours = [] }) {
  const [showFilters, setShowFilters] = useState(false);

  return (
    <div className="page-container">
      {/* Header */}
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.75rem', color: '#111827' }}>
          Discover Guided Tours &amp; Excursions
        </h2>
        <p style={{ margin: 0, color: '#4b5563', fontSize: '0.95rem' }}>
          Browse available outdoor activities, city walking tours, and cultural experiences.
        </p>
      </div>

      {/* Search Bar (Top): Single row containing text input, Filter button, and Search button */}
      <div className="search-bar-row">
        <input
          type="text"
          className="search-input-field"
          placeholder="Search by tour name, destination, or landmark..."
          defaultValue=""
          aria-label="Search tours"
        />
        <button
          type="button"
          className={`btn ${showFilters ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setShowFilters(!showFilters)}
          style={{ whiteSpace: 'nowrap' }}
        >
          {showFilters ? 'Hide Filters ▲' : 'Filters ▼'}
        </button>
        <button
          type="button"
          className="btn btn-primary"
          style={{ whiteSpace: 'nowrap' }}
          onClick={(e) => e.preventDefault()}
        >
          Search
        </button>
      </div>

      {/* Filters: Expanded panel containing dropdowns/inputs for:
          Time slot, Location, Activity Category, Duration, Price, Number of participants.
          Visually complete but functionally non-usable for this mock. */}
      {showFilters && (
        <div className="filter-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0, fontSize: '1rem', color: '#1f2937' }}>
              Filter Tours
            </h3>
            <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>
              (Mock Filter Panel)
            </span>
          </div>

          <div className="filter-grid">
            {/* 1. Time slot */}
            <div>
              <label className="form-label" htmlFor="filter-timeslot">Time Slot</label>
              <select id="filter-timeslot" className="form-select" defaultValue="">
                <option value="">Any time slot</option>
                <option value="morning">Morning (08:00 - 12:00)</option>
                <option value="afternoon">Afternoon (12:00 - 17:00)</option>
                <option value="evening">Evening (17:00+)</option>
                <option value="weekend">Weekends only</option>
              </select>
            </div>

            {/* 2. Location */}
            <div>
              <label className="form-label" htmlFor="filter-location">Location</label>
              <select id="filter-location" className="form-select" defaultValue="">
                <option value="">All locations</option>
                <option value="zagreb">Zagreb &amp; Medvednica</option>
                <option value="plitvice">Plitvice Lakes</option>
                <option value="split">Split &amp; Islands</option>
                <option value="dubrovnik">Dubrovnik</option>
                <option value="zadar">Zadar archipelago</option>
                <option value="omis">Cetina River / Omiš</option>
                <option value="porec">Poreč / Istria</option>
                <option value="korcula">Korčula</option>
              </select>
            </div>

            {/* 3. Activity Category */}
            <div>
              <label className="form-label" htmlFor="filter-category">Activity Category</label>
              <select id="filter-category" className="form-select" defaultValue="">
                <option value="">All categories</option>
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
              <label className="form-label" htmlFor="filter-duration">Duration</label>
              <select id="filter-duration" className="form-select" defaultValue="">
                <option value="">Any duration</option>
                <option value="short">Up to 2.5 hours</option>
                <option value="medium">3 to 5 hours</option>
                <option value="full">Full day (5+ hours)</option>
              </select>
            </div>

            {/* 5. Price */}
            <div>
              <label className="form-label" htmlFor="filter-price">Max Price (€)</label>
              <input
                id="filter-price"
                type="number"
                className="form-input"
                placeholder="e.g. 50"
                min="0"
                step="5"
              />
            </div>

            {/* 6. Number of participants */}
            <div>
              <label className="form-label" htmlFor="filter-participants">Participants</label>
              <input
                id="filter-participants"
                type="number"
                className="form-input"
                placeholder="e.g. 2"
                min="1"
                max="25"
              />
            </div>
          </div>
        </div>
      )}

      {/* Results Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '1rem 0' }}>
        <p style={{ margin: 0, fontSize: '0.9rem', color: '#4b5563' }}>
          Showing <strong>{tours.length}</strong> available tours
        </p>
      </div>

      {/* Tour List: Display cards with essential info */}
      <div className="tours-grid">
        {tours.map((tour) => (
          <div
            key={tour.id}
            className="tour-card"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(`/tours/${tour.id}`)}
          >
            <div>
              <div className="tour-card-header">
                <span className="tour-card-badge">{tour.activityCategory}</span>
                <h3 style={{ margin: '0 0 0.5rem 0', fontSize: '1.15rem', color: '#111827', lineHeight: 1.3 }}>
                  {tour.title}
                </h3>
                <p style={{ margin: 0, fontSize: '0.875rem', color: '#6b7280' }}>
                  📍 {tour.location}
                </p>
              </div>

              <p style={{ fontSize: '0.875rem', color: '#374151', margin: '0.75rem 0 1rem 0', lineHeight: 1.4 }}>
                {tour.description}
              </p>

              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', fontSize: '0.8rem', color: '#4b5563', marginBottom: '1rem' }}>
                <span>⏱ <strong>{tour.duration}</strong></span>
                <span>👥 Max <strong>{tour.maxParticipants}</strong></span>
                <span>🚩 {tour.departurePoint.split(',')[0]}</span>
              </div>
            </div>

            <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <span style={{ fontSize: '0.75rem', color: '#6b7280', display: 'block' }}>From</span>
                <span className="tour-card-price">€{tour.price}</span>
                <span style={{ fontSize: '0.8rem', color: '#6b7280' }}> / person</span>
              </div>
              <button
                type="button"
                className="btn btn-primary"
                onClick={(e) => {
                  e.stopPropagation();
                  navigate(`/tours/${tour.id}`);
                }}
              >
                View Details
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination: Static "Show more" button */}
      <div style={{ textAlign: 'center', marginTop: '2.5rem', marginBottom: '2rem' }}>
        <button
          type="button"
          className="btn btn-secondary"
          style={{ minWidth: '180px' }}
        >
          Show more
        </button>
      </div>
    </div>
  );
}

export default TourSearchPage;
