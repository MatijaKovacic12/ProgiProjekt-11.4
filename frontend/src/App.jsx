import { useState, useEffect } from 'react';
import tours from './mock-tours.js';
import LoginPage from './pages/LoginPage.jsx';
import TourSearchPage from './pages/TourSearchPage.jsx';
import TourDetailsPage from './pages/TourDetailsPage.jsx';
import MapsPage from './pages/MapsPage.jsx';
import MyBookingsPage from './pages/MyBookingsPage.jsx';
import GuideDashboardPage from './pages/GuideDashboardPage.jsx';
import AdminManagementPage from './pages/AdminManagementPage.jsx';
import OrganizerAssignmentPage from './pages/OrganizerAssignmentPage.jsx';
import AgencyDashboardPage from './pages/AgencyDashboardPage.jsx';
import { getCurrentUser, logout } from './api/authClient.js';

function App() {
  const [currentPath, setCurrentPath] = useState(
    () => (window.location.pathname === '/' ? '/tours' : window.location.pathname)
  );
  const [userRole, setUserRole] = useState(
    () => localStorage.getItem('userRole') || 'unregistered'
  );

  useEffect(() => {
    if (window.location.pathname === '/') {
      window.history.replaceState({}, '', '/tours');
    }

    const handlePopState = () => {
      const path = window.location.pathname === '/' ? '/tours' : window.location.pathname;
      setCurrentPath(path);
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  // if user is logged in on backend (OAuth), use their real role
  useEffect(() => {
    getCurrentUser().then((user) => {
      if (user) {
        setUserRole(user.role);
        localStorage.setItem('userRole', user.role);
      }
    });
  }, []);

  const navigate = (path) => {
    window.history.pushState({}, '', path);
    setCurrentPath(path);
  };

  const handleSelectRole = (role) => {
    setUserRole(role);
    localStorage.setItem('userRole', role);
  };

  const handleLogout = () => {
    logout();
    setUserRole('unregistered');
    localStorage.setItem('userRole', 'unregistered');
    navigate('/login');
  };

  const isClientOrPublicRoute =
    currentPath === '/tours' ||
    currentPath.startsWith('/tours/') ||
    currentPath === '/my-bookings' ||
    currentPath === '/maps';

  const isGuideRoute = currentPath === '/guide-dashboard';

  const isBackofficeRoute =
    currentPath === '/admin' ||
    currentPath === '/organizer' ||
    currentPath === '/agency-manager' ||
    currentPath === '/dashboard';

  const isSearchActive = currentPath === '/tours' || currentPath.startsWith('/tours/');
  const isMyBookingsActive = currentPath === '/my-bookings';
  const isMapsActive = currentPath === '/maps';

  const isUnregistered = userRole === 'unregistered';

  const renderRoute = () => {
    if (currentPath === '/login') {
      return <LoginPage navigate={navigate} onSelectRole={handleSelectRole} />;
    }
    if (currentPath === '/tours' || currentPath === '/') {
      return <TourSearchPage navigate={navigate} tours={tours} />;
    }
    if (currentPath.startsWith('/tours/')) {
      const tourId = currentPath.replace('/tours/', '');
      return (
        <TourDetailsPage
          navigate={navigate}
          tourId={tourId}
          tours={tours}
          userRole={userRole}
        />
      );
    }
    if (currentPath === '/my-bookings') {
      if (isUnregistered) {
        return (
          <div className="page-container" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
            <h2 style={{ color: '#111827' }}>My Bookings</h2>
            <p style={{ color: '#4b5563', margin: '0.75rem 0 1.5rem 0' }}>
              Please log in as a registered client to view your reservations.
            </p>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => navigate('/login')}
            >
              Go to Login
            </button>
          </div>
        );
      }
      return <MyBookingsPage navigate={navigate} />;
    }
    if (currentPath === '/maps') {
      if (isUnregistered) {
        return (
          <div className="page-container" style={{ textAlign: 'center', padding: '3.5rem 1rem' }}>
            <h2 style={{ color: '#111827' }}>Access Restricted</h2>
            <p style={{ color: '#4b5563', margin: '0.75rem 0 1.5rem 0' }}>
              The Maps feature is only accessible to registered clients.
            </p>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => navigate('/tours')}
            >
              Back to Tours
            </button>
          </div>
        );
      }
      return <MapsPage navigate={navigate} />;
    }
    if (currentPath === '/guide-dashboard') {
      return <GuideDashboardPage navigate={navigate} />;
    }
    if (currentPath === '/admin') {
      return <AdminManagementPage navigate={navigate} />;
    }
    if (currentPath === '/organizer') {
      return <OrganizerAssignmentPage navigate={navigate} />;
    }
    if (currentPath === '/agency-manager' || currentPath === '/dashboard') {
      return <AgencyDashboardPage navigate={navigate} />;
    }
    return (
      <div style={{ padding: '1.5rem' }}>
        <h2>404 - Not Found</h2>
        <p>Path &apos;{currentPath}&apos; does not exist.</p>
        <button className="btn btn-primary" onClick={() => navigate('/tours')}>
          Back to Tours
        </button>
      </div>
    );
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {(isClientOrPublicRoute || isGuideRoute) && (
        <>
          <header className="desktop-nav-header">
            <h1
              style={{ margin: 0, fontSize: '1.25rem', color: '#111827', cursor: 'pointer' }}
              onClick={() => navigate(isGuideRoute ? '/guide-dashboard' : '/tours')}
            >
              PROGI Tours
            </h1>
            <nav className="desktop-nav-links">
              {!isGuideRoute && (
                <>
                  <button
                    type="button"
                    onClick={() => navigate('/tours')}
                    className={`btn ${isSearchActive ? 'btn-primary' : 'btn-secondary'} nav-link-desktop`}
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '0.45rem',
                      padding: '0.4rem 0.9rem',
                      fontSize: '0.875rem',
                    }}
                  >
                    <svg
                      width="18"
                      height="18"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      aria-hidden="true"
                    >
                      <circle cx="11" cy="11" r="8" />
                      <line x1="21" y1="21" x2="16.65" y2="16.65" />
                    </svg>
                    <span>Search</span>
                  </button>

                  {!isUnregistered && (
                    <>
                      <button
                        type="button"
                        onClick={() => navigate('/my-bookings')}
                        className={`btn ${isMyBookingsActive ? 'btn-primary' : 'btn-secondary'} nav-link-desktop`}
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.45rem',
                          padding: '0.4rem 0.9rem',
                          fontSize: '0.875rem',
                        }}
                      >
                        <svg
                          width="18"
                          height="18"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          aria-hidden="true"
                        >
                          <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                          <line x1="16" y1="2" x2="16" y2="6" />
                          <line x1="8" y1="2" x2="8" y2="6" />
                          <line x1="3" y1="10" x2="21" y2="10" />
                        </svg>
                        <span>My Bookings</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => navigate('/maps')}
                        className={`btn ${isMapsActive ? 'btn-primary' : 'btn-secondary'} nav-link-desktop`}
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.45rem',
                          padding: '0.4rem 0.9rem',
                          fontSize: '0.875rem',
                        }}
                      >
                        <svg
                          width="18"
                          height="18"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth="2"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          aria-hidden="true"
                        >
                          <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                          <line x1="9" y1="3" x2="9" y2="18" />
                          <line x1="15" y1="6" x2="15" y2="21" />
                        </svg>
                        <span>Maps</span>
                      </button>
                    </>
                  )}
                </>
              )}

              {isUnregistered ? (
                <button
                  type="button"
                  onClick={() => navigate('/login')}
                  className="btn btn-secondary"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.45rem',
                    padding: '0.4rem 0.9rem',
                    fontSize: '0.875rem',
                  }}
                >
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                  >
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                    <polyline points="10 17 15 12 10 7" />
                    <line x1="15" y1="12" x2="3" y2="12" />
                  </svg>
                  <span>Login</span>
                </button>
              ) : (
                <button
                  type="button"
                  onClick={handleLogout}
                  className="btn btn-secondary"
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.45rem',
                    padding: '0.4rem 0.9rem',
                    fontSize: '0.875rem',
                  }}
                >
                  <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                  >
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                    <polyline points="16 17 21 12 16 7" />
                    <line x1="21" y1="12" x2="9" y2="12" />
                  </svg>
                  <span>Logout</span>
                </button>
              )}
            </nav>
          </header>

          {isClientOrPublicRoute && !isUnregistered && (
            <nav className="mobile-bottom-nav" aria-label="Mobile Navigation">
              <button
                type="button"
                onClick={() => navigate('/tours')}
                className={`mobile-nav-btn ${isSearchActive ? 'active' : ''}`}
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <span>Search</span>
              </button>
              <button
                type="button"
                onClick={() => navigate('/my-bookings')}
                className={`mobile-nav-btn ${isMyBookingsActive ? 'active' : ''}`}
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                <span>My Bookings</span>
              </button>
              <button
                type="button"
                onClick={() => navigate('/maps')}
                className={`mobile-nav-btn ${isMapsActive ? 'active' : ''}`}
              >
                <svg
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                  <line x1="9" y1="3" x2="9" y2="18" />
                  <line x1="15" y1="6" x2="15" y2="21" />
                </svg>
                <span>Maps</span>
              </button>
            </nav>
          )}
        </>
      )}

      {isBackofficeRoute && (
        <div className="backoffice-top-bar">
          <button
            type="button"
            onClick={handleLogout}
            className="btn btn-secondary"
            style={{ padding: '0.4rem 0.9rem', fontSize: '0.875rem' }}
          >
            Logout
          </button>
        </div>
      )}

      <main
        className={isClientOrPublicRoute && !isUnregistered ? 'regular-user-main' : ''}
        style={{ flex: 1 }}
      >
        {renderRoute()}
      </main>
    </div>
  );
}

export default App;