function LoginPage({ navigate, onSelectRole }) {
  const handleRoleClick = (role, path) => {
    if (onSelectRole) {
      onSelectRole(role);
    }
    navigate(path);
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
          <h2 style={{ margin: '0 0 0.5rem 0', fontSize: '1.5rem', color: '#111827' }}>
            Sign In
          </h2>
        </div>

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

        <div>
          <div className="role-buttons-grid">
            <button
              type="button"
              className="btn btn-primary btn-block"
              onClick={() => handleRoleClick('client', '/tours')}
            >
              Regular User (Search)
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => handleRoleClick('unregistered', '/tours')}
            >
              Unregistered User
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => handleRoleClick('guide', '/guide-dashboard')}
            >
              Guide
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => handleRoleClick('admin', '/admin')}
            >
              Admin
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => handleRoleClick('organizer', '/organizer')}
            >
              Organizer
            </button>
            <button
              type="button"
              className="btn btn-secondary btn-block"
              onClick={() => handleRoleClick('manager', '/agency-manager')}
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
