import { useState } from 'react';
import { dashboardMetrics, tourPerformanceData } from '../mock-backoffice-data.js';

function AgencyDashboardPage() {
  const [isExportMenuOpen, setIsExportMenuOpen] = useState(false);

  return (
    <div className="page-container">
      {/* Top / Action Bar */}
      <div
        className="section-header-row"
        style={{ marginBottom: '1.75rem', alignItems: 'center' }}
      >
        <div>
          <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
            Agency Manager Dashboard
          </h2>
          <p style={{ margin: 0, color: '#4b5563', fontSize: '0.95rem' }}>
            High-level business analytics, tour occupancy rates, generated revenues, and performance reports.
          </p>
        </div>

        {/* Export Report Dropdown Button */}
        <div style={{ position: 'relative' }}>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setIsExportMenuOpen(!isExportMenuOpen)}
            style={{
              whiteSpace: 'nowrap',
              display: 'inline-flex',
              gap: '0.5rem',
              alignItems: 'center',
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
            >
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
            <span>Export Report</span>
            <span style={{ fontSize: '0.75rem' }}>{isExportMenuOpen ? '▲' : '▼'}</span>
          </button>

          {isExportMenuOpen && (
            <div
              style={{
                position: 'absolute',
                right: 0,
                top: 'calc(100% + 0.35rem)',
                backgroundColor: '#ffffff',
                border: '1px solid #cbd5e1',
                borderRadius: '6px',
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
                zIndex: 100,
                minWidth: '160px',
                padding: '0.35rem',
                display: 'flex',
                flexDirection: 'column',
                gap: '0.25rem',
              }}
            >
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setIsExportMenuOpen(false)}
                style={{
                  width: '100%',
                  justifyContent: 'flex-start',
                  padding: '0.45rem 0.75rem',
                  fontSize: '0.85rem',
                }}
              >
                Export as PDF
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setIsExportMenuOpen(false)}
                style={{
                  width: '100%',
                  justifyContent: 'flex-start',
                  padding: '0.45rem 0.75rem',
                  fontSize: '0.85rem',
                }}
              >
                Export as CSV
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Metric Summary Cards (Data Widgets) */}
      <section style={{ marginBottom: '2.5rem' }}>
        <h3 style={{ margin: '0 0 1rem 0', fontSize: '1.15rem', color: '#1f2937' }}>
          Key Performance Indicators
        </h3>
        <div className="stat-card-grid">
          {/* 1. Total Bookings */}
          <div className="stat-card">
            <span className="stat-card-label">Total Bookings</span>
            <div className="stat-card-value">{dashboardMetrics.totalBookings}</div>
            <span className="stat-card-sub" style={{ color: '#16a34a' }}>
              ↑ 12% vs previous period
            </span>
          </div>

          {/* 2. Occupancy Rate (%) */}
          <div className="stat-card">
            <span className="stat-card-label">Occupancy Rate</span>
            <div className="stat-card-value">{dashboardMetrics.occupancyRate}%</div>
            <span className="stat-card-sub" style={{ color: '#0369a1' }}>
              Average across all scheduled slots
            </span>
          </div>

          {/* 3. Generated Revenue */}
          <div className="stat-card">
            <span className="stat-card-label">Generated Revenue</span>
            <div className="stat-card-value">
              €{dashboardMetrics.generatedRevenue.toLocaleString()}
            </div>
            <span className="stat-card-sub" style={{ color: '#16a34a' }}>
              Gross bookings &amp; extras revenue
            </span>
          </div>

          {/* 4. Most Popular Activity */}
          <div className="stat-card">
            <span className="stat-card-label">Most Popular Activity</span>
            <div
              className="stat-card-value"
              style={{ fontSize: '1.15rem', fontWeight: 600, color: '#1e293b' }}
            >
              {dashboardMetrics.popularActivity}
            </div>
            <span className="stat-card-sub" style={{ color: '#475569' }}>
              Highest demand &amp; 91.7% occupancy
            </span>
          </div>

          {/* 5. Most Requested Add-ons */}
          <div className="stat-card">
            <span className="stat-card-label">Most Requested Add-ons</span>
            <div
              className="stat-card-value"
              style={{ fontSize: '1.15rem', fontWeight: 600, color: '#1e293b' }}
            >
              {dashboardMetrics.popularAddons}
            </div>
            <span className="stat-card-sub" style={{ color: '#475569' }}>
              Top ancillary service selections
            </span>
          </div>
        </div>
      </section>

      {/* Tour Performance Breakdown Table */}
      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Tour Performance Breakdown
            </h3>
            <p style={{ margin: 0, color: '#6b7280', fontSize: '0.875rem' }}>
              Individual tour listings alongside their respective occupancy rates and generated revenues.
            </p>
          </div>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Tour Name</th>
                <th>Category</th>
                <th>Total Bookings</th>
                <th>Capacity</th>
                <th>Occupancy Rate (%)</th>
                <th style={{ textAlign: 'right' }}>Generated Revenue (€)</th>
              </tr>
            </thead>
            <tbody>
              {tourPerformanceData.map((item) => {
                const isHighOccupancy = item.occupancyRate >= 80;
                const isMediumOccupancy = item.occupancyRate >= 65 && item.occupancyRate < 80;

                return (
                  <tr key={item.id}>
                    <td style={{ fontWeight: 600 }}>{item.title}</td>
                    <td>
                      <span className="badge badge-info">{item.category}</span>
                    </td>
                    <td>{item.bookingsCount} reservations</td>
                    <td>{item.capacity} seats</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span
                          className={`badge ${
                            isHighOccupancy
                              ? 'badge-success'
                              : isMediumOccupancy
                              ? 'badge-warning'
                              : 'badge-neutral'
                          }`}
                        >
                          {item.occupancyRate}%
                        </span>
                      </div>
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: '#0f172a' }}>
                      €{item.revenue.toLocaleString()}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default AgencyDashboardPage;
