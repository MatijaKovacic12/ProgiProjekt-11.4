function MobileGuidePage({ navigate }) {
  return (
    <div style={{ padding: '1.5rem' }}>
      <h2>Mobile Digital Guide Screen (/guide)</h2>
      <p>Placeholder for GPS-assisted location guide, map, and point-of-interest triggers.</p>
      <button onClick={() => navigate('/tours')}>Back to Tours</button>
    </div>
  );
}

export default MobileGuidePage;
