function LoginPage({ navigate }) {
  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
          <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.5rem', color: '#111827' }}>
            Sign In
          </h2>
          <p style={{ margin: 0, fontSize: '0.9rem', color: '#6b7280' }}>
            Access the Tour Management &amp; Reservation Platform
          </p>
        </div>

        {/* Core Element: OAuth 2.0 / OpenID Connect login button (mocked, non-reactive) */}
        <div style={{ marginBottom: '1.75rem' }}>
          <label className="form-label" style={{ textAlign: 'center', marginBottom: '0.75rem' }}>
            Single Sign-On Authentication
          </label>
          <button
            type="button"
            className="oauth-button"
            disabled
            style={{ width: '100%', cursor: 'default' }}
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="#0066cc"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
            <span>Log in with OAuth 2.0 / OpenID Connect</span>
          </button>
          <p style={{ margin: '0.4rem 0 0 0', fontSize: '0.75rem', color: '#9ca3af', textAlign: 'center' }}>
            (Mocked SSO provider integration)
          </p>
        </div>

        <div style={{ position: 'relative', textAlign: 'center', margin: '1.5rem 0' }}>
          <hr style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: 0 }} />
          <span
            style={{
              position: 'relative',
              top: '-0.7em',
              background: '#ffffff',
              padding: '0 0.75rem',
              fontSize: '0.8rem',
              color: '#6b7280',
              fontWeight: 500,
            }}
          >
            OR TEST A ROLE
          </span>
        </div>

        {/* Navigation/Testing: 4 distinct buttons for testing user roles */}
        <div>
          <p style={{ fontSize: '0.85rem', color: '#4b5563', marginBottom: '0.75rem', fontWeight: 500 }}>
            Quick Navigation by Role:
          </p>
          <div className="role-buttons-grid">
            <button
              type="button"
              className="btn btn-primary btn-block"
              onClick={() => navigate('/tours')}
            >
              Regular User (Search)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => navigate('/admin')}
            >
              Admin
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => navigate('/organizer')}
            >
              Organizer
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => navigate('/dashboard')}
            >
              Agency Manager
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
