function LoginPage({ navigate }) {
  return (
    <div style={{ padding: '1.5rem' }}>
      <h2>Login Screen (/login)</h2>
      <p>Placeholder for OpenID Connect / OAuth 2.0 authentication.</p>
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginTop: '1rem' }}>
        <button onClick={() => navigate('/tours')}>Login as Client (Tour Search)</button>
        <button onClick={() => navigate('/admin')}>Login as Admin (Administrator Management)</button>
        <button onClick={() => navigate('/organizer')}>Login as Organizer (Organizer Assignment)</button>
        <button onClick={() => navigate('/dashboard')}>Login as Agency Manager (Dashboard)</button>
      </div>
    </div>
  );
}

export default LoginPage;
