function AgencyDashboardPage({ navigate }) {
  return (
    <div style={{ padding: '1.5rem' }}>
      <h2>Agency Manager Dashboard Screen (/dashboard)</h2>
      <p>Placeholder for key business metrics, booking analytics, and report exporting.</p>
      <button onClick={() => navigate('/login')}>Logout / Switch User</button>
    </div>
  );
}

export default AgencyDashboardPage;
