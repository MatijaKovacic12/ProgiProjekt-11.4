function MapsPage() {
  return (
    <div className="page-container">
      <div className="maps-desktop-notice">
        <div
          className="card"
          style={{
            maxWidth: '450px',
            width: '100%',
            textAlign: 'center',
            padding: '2.5rem 2rem',
            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)',
          }}
        >
          <svg
            width="48"
            height="48"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#6b7280"
            strokeWidth="1.75"
            strokeLinecap="round"
            strokeLinejoin="round"
            style={{ margin: '0 auto 1rem auto' }}
          >
            <rect x="5" y="2" width="14" height="20" rx="2" ry="2" />
            <line x1="12" y1="18" x2="12.01" y2="18" />
          </svg>
          <p style={{ margin: 0, fontSize: '1.15rem', fontWeight: 600, color: '#374151' }}>
            Please switch to mobile for this feature
          </p>
        </div>
      </div>

      <div className="maps-mobile-view">
        <div className="maps-coming-soon-box">
          <svg
            width="48"
            height="48"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#0066cc"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            style={{ marginBottom: '0.75rem' }}
          >
            <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
            <line x1="9" y1="3" x2="9" y2="18" />
            <line x1="15" y1="6" x2="15" y2="21" />
          </svg>
          <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#111827' }}>
            Coming soon
          </h3>
        </div>
      </div>
    </div>
  );
}

export default MapsPage;
