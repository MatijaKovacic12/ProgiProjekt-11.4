import { useState } from 'react';

function TourSearchPage({ navigate, tours = [], loading = false }) {
    const [showFilters, setShowFilters] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('');
    const [maxPrice, setMaxPrice] = useState('');

    const filteredTours = tours.filter((tour) => {
        if (searchTerm.trim()) {
            const q = searchTerm.toLowerCase();
            const matchTitle = tour.title && tour.title.toLowerCase().includes(q);
            const matchDesc = tour.description && tour.description.toLowerCase().includes(q);
            const matchLoc = tour.location && tour.location.toLowerCase().includes(q);
            if (!matchTitle && !matchDesc && !matchLoc) return false;
        }
        if (selectedCategory && tour.activityCategory !== selectedCategory) {
            return false;
        }
        if (maxPrice && Number(tour.price) > Number(maxPrice)) {
            return false;
        }
        return true;
    });
  return (
    <div className="page-container">
      <div className="search-bar-row">
        <input
          type="text"
          className="search-input-field"
          placeholder="Search by tour name or landmark..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
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

      {showFilters && (
        <div className="filter-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ margin: 0, fontSize: '1rem', color: '#1f2937' }}>
              Filter Tours
            </h3>
          </div>

          <div className="filter-grid">
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

            <div>
                <label className="form-label" htmlFor="filter-category">Activity Category</label>
                <select
                    id="filter-category"
                    className="form-select"
                    value={selectedCategory}
                    onChange={(e) => setSelectedCategory(e.target.value)}
                >
                    <option value="">All categories</option>
                    <option value="City tour">City tour</option>
                    <option value="Hiking">Hiking</option>
                    <option value="Cycling">Cycling</option>
                    <option value="Rafting">Rafting</option>
                    <option value="Boat trip">Boat trip</option>
                    <option value="Kayaking">Kayaking</option>
                </select>
            </div>

            <div>
              <label className="form-label" htmlFor="filter-duration">Duration</label>
              <select id="filter-duration" className="form-select" defaultValue="">
                <option value="">Any duration</option>
                <option value="short">Up to 2.5 hours</option>
                <option value="medium">3 to 5 hours</option>
                <option value="full">Full day (5+ hours)</option>
              </select>
            </div>

            <div>
                <label className="form-label" htmlFor="filter-price">Max Price (€)</label>
                <input
                    id="filter-price"
                    type="number"
                    className="form-input"
                    placeholder="e.g. 50"
                    min="0"
                    step="5"
                    value={maxPrice}
                    onChange={(e) => setMaxPrice(e.target.value)}
                />
            </div>

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

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '1rem 0' }}>
            <p style={{ margin: 0, fontSize: '0.9rem', color: '#4b5563' }}>
                {loading ? 'Loading tours...' : <>Showing <strong>{filteredTours.length}</strong> available tours</>}
            </p>
        </div>

        {loading && (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#6b7280' }}>
                <p>Loading tours from backend...</p>
            </div>
        )}

        {!loading && filteredTours.length === 0 && (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#6b7280' }}>
                <p>No tours found matching your criteria.</p>
            </div>
        )}

        {!loading && filteredTours.length > 0 && (
            <div className="tours-grid">
                {filteredTours.map((tour) => (
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
                                <span>🚩 {tour.departurePoint ? tour.departurePoint.split(',')[0] : (tour.location || 'N/A')}</span>
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
        )}

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
