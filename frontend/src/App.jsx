import { useState, useEffect } from 'react';
import tours from './mock-tours.js';
import LoginPage from './pages/LoginPage.jsx';
import TourSearchPage from './pages/TourSearchPage.jsx';
import TourDetailsPage from './pages/TourDetailsPage.jsx';
import MapsPage from './pages/MapsPage.jsx';
import AdminManagementPage from './pages/AdminManagementPage.jsx';
import OrganizerAssignmentPage from './pages/OrganizerAssignmentPage.jsx';
import AgencyDashboardPage from './pages/AgencyDashboardPage.jsx';

function App() {
  const [currentPath, setCurrentPath] = useState(
    () => (window.location.pathname === '/' ? '/login' : window.location.pathname)
  );

  useEffect(() => {
    const handlePopState = () => {
      setCurrentPath(window.location.pathname === '/' ? '/login' : window.location.pathname);
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigate = (path) => {
    window.history.pushState({}, '', path);
    setCurrentPath(path);
  };

  const isRegularUserRoute =
    currentPath === '/tours' ||
    currentPath.startsWith('/tours/') ||
    currentPath === '/maps';

  const isBackofficeRoute =
    currentPath === '/admin' ||
    currentPath === '/organizer' ||
    currentPath === '/dashboard';

  const isSearchActive = currentPath === '/tours' || currentPath.startsWith('/tours/');

  const renderRoute = () => {
    if (currentPath === '/login' || currentPath === '/') {
      return <LoginPage navigate={navigate} />;
    }
    if (currentPath === '/tours') {
      return <TourSearchPage navigate={navigate} tours={tours} />;
    }
    if (currentPath.startsWith('/tours/')) {
      const tourId = currentPath.replace('/tours/', '');
      return <TourDetailsPage navigate={navigate} tourId={tourId} tours={tours} />;
    }
    if (currentPath === '/maps') {
      return <MapsPage navigate={navigate} />;
    }
    if (currentPath === '/admin') {
      return <AdminManagementPage navigate={navigate} />;
    }
    if (currentPath === '/organizer') {
      return <OrganizerAssignmentPage navigate={navigate} />;
    }
    if (currentPath === '/dashboard') {
      return <AgencyDashboardPage navigate={navigate} />;
    }
    return (
      <div style={{ padding: '1.5rem' }}>
        <h2>404 - Not Found</h2>
        <p>Path &apos;{currentPath}&apos; does not exist.</p>
        <button className="btn btn-primary" onClick={() => navigate('/login')}>
          Back to Login
        </button>
      </div>
    );
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Regular User Routes Navigation */}
      {isRegularUserRoute && (
        <>
          {/* Desktop View Header (>768px): Reduced to Search, Maps, Logout */}
          <header className="desktop-nav-header">
            <h1 style={{ margin: 0, fontSize: '1.25rem', color: '#111827' }}>
              Tour Management System
            </h1>
            <nav className="desktop-nav-links">
              <button
                type="button"
                onClick={() => navigate('/tours')}
                className={`btn ${isSearchActive ? 'btn-primary' : 'btn-secondary'}`}
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', padding: '0.4rem 0.9rem', fontSize: '0.875rem' }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <circle cx="11" cy="11" r="8" />
                  <line x1="21" y1="21" x2="16.65" y2="16.65" />
                </svg>
                <span>Search</span>
              </button>
              <button
                type="button"
                onClick={() => navigate('/maps')}
                className={`btn ${currentPath === '/maps' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', padding: '0.4rem 0.9rem', fontSize: '0.875rem' }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                  <line x1="9" y1="3" x2="9" y2="18" />
                  <line x1="15" y1="6" x2="15" y2="21" />
                </svg>
                <span>Maps</span>
              </button>
              <button
                type="button"
                onClick={() => navigate('/login')}
                className="btn btn-secondary"
                style={{ display: 'inline-flex', alignItems: 'center', gap: '0.45rem', padding: '0.4rem 0.9rem', fontSize: '0.875rem' }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                  <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                  <polyline points="16 17 21 12 16 7" />
                  <line x1="21" y1="12" x2="9" y2="12" />
                </svg>
                <span>Logout</span>
              </button>
            </nav>
          </header>

          {/* Mobile View Fixed Bottom Footer (<=768px): Ordered Search, Maps, Logout */}
          <nav className="mobile-bottom-nav" aria-label="Mobile Navigation">
            <button
              type="button"
              onClick={() => navigate('/tours')}
              className={`mobile-nav-btn ${isSearchActive ? 'active' : ''}`}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <span>Search</span>
            </button>
            <button
              type="button"
              onClick={() => navigate('/maps')}
              className={`mobile-nav-btn ${currentPath === '/maps' ? 'active' : ''}`}
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polygon points="3 6 9 3 15 6 21 3 21 18 15 21 9 18 3 21" />
                <line x1="9" y1="3" x2="9" y2="18" />
                <line x1="15" y1="6" x2="15" y2="21" />
              </svg>
              <span>Maps</span>
            </button>
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="mobile-nav-btn"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
              <span>Logout</span>
            </button>
          </nav>
        </>
      )}

      {/* Backoffice Placeholders Navigation (Admin, Organizer, Dashboard): Top-right Logout button */}
      {isBackofficeRoute && (
        <div className="backoffice-top-bar">
          <button
            type="button"
            onClick={() => navigate('/login')}
            className="btn btn-secondary"
            style={{ padding: '0.4rem 0.9rem', fontSize: '0.875rem' }}
          >
            Logout
          </button>
        </div>
      )}

      {/* Main View Area */}
      <main className={isRegularUserRoute ? 'regular-user-main' : ''} style={{ flex: 1 }}>
        {renderRoute()}
      </main>
    </div>
  );
}

export default App;