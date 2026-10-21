import { useState, useEffect } from 'react';
import tours from './mock-tours.js';
import LoginPage from './pages/LoginPage.jsx';
import TourSearchPage from './pages/TourSearchPage.jsx';
import TourDetailsPage from './pages/TourDetailsPage.jsx';
import MobileGuidePage from './pages/MobileGuidePage.jsx';
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

  const navItems = [
    { path: '/login', label: 'Login' },
    { path: '/tours', label: 'Tour Search' },
    { path: '/tours/1', label: 'Tour Details' },
    { path: '/guide', label: 'Mobile Guide' },
    { path: '/admin', label: 'Admin Management' },
    { path: '/organizer', label: 'Organizer' },
    { path: '/dashboard', label: 'Dashboard' },
  ];

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
    if (currentPath === '/guide') {
      return <MobileGuidePage navigate={navigate} />;
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
        <button onClick={() => navigate('/login')}>Back to Login</button>
      </div>
    );
  };

  const isActive = (itemPath) => {
    if (itemPath === '/tours/1') {
      return currentPath.startsWith('/tours/');
    }
    return currentPath === itemPath;
  };

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif', minHeight: '100vh', margin: 0 }}>
      {/* Navigation Shell */}
      <header
        style={{
          borderBottom: '1px solid #ddd',
          padding: '0.75rem 1.5rem',
          backgroundColor: '#f8f9fa',
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
        }}
      >
        <h1 style={{ margin: 0, fontSize: '1.25rem' }}>Tour Management System (Prototype)</h1>
        <nav style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          {navItems.map((item) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              style={{
                padding: '0.4rem 0.8rem',
                cursor: 'pointer',
                borderRadius: '4px',
                border: '1px solid #ccc',
                backgroundColor: isActive(item.path) ? '#0066cc' : '#ffffff',
                color: isActive(item.path) ? '#ffffff' : '#333333',
                fontWeight: isActive(item.path) ? 'bold' : 'normal',
              }}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </header>

      {/* Main View Area */}
      <main>
        {renderRoute()}
      </main>
    </div>
  );
}

export default App;