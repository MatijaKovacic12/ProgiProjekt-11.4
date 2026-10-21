function AdminManagementPage({ navigate }) {
  return (
    <div style={{ padding: '1.5rem' }}>
      <h2>Administrator Management Screen (/admin)</h2>
      <p>Placeholder for managing tours, scheduled slots, pricing, capacities, and bookings.</p>
      <button onClick={() => navigate('/login')}>Logout / Switch User</button>
    </div>
  );
}

export default AdminManagementPage;
