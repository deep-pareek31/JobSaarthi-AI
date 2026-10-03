import React from 'react';

export default function AdminDashboard() {
  return (
    <div style={{ padding: '2rem', fontFamily: 'system-ui, sans-serif' }}>
      <h1>JobSaarthi Admin Control Hub</h1>
      <p>Source Health, Ingestion Telemetry, and Dynamic Subscription Plan Controls.</p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', marginTop: '1.5rem' }}>
        <div style={{ padding: '1rem', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
          <h3>Total Active Jobs</h3>
          <p style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>1,248</p>
        </div>
        <div style={{ padding: '1rem', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
          <h3>Healthy Sources</h3>
          <p style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#16a34a' }}>14 / 14</p>
        </div>
        <div style={{ padding: '1rem', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
          <h3>Paid Users</h3>
          <p style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>342</p>
        </div>
        <div style={{ padding: '1rem', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
          <h3>Monthly Run Rate</h3>
          <p style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>₹48,220</p>
        </div>
      </div>
    </div>
  );
}
