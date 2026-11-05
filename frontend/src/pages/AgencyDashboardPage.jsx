import { useEffect, useState } from 'react';
import {
  periodMetrics,
  topFiveActivities,
  topFiveAddons,
  tourPerformanceData,
} from '../mock-backoffice-data.js';

import {getEmployees, findUserByEmail, changeRole  } from '../api/roleManagementClient.js';

 const ROLE_LABELS = {
   guide: 'Guide',
   organizer: 'Organizer',
   admin: 'Administrator',
   manager: 'Agency Manager',
   client: 'Client',
 };

 const ROLE_BADGE = {
   admin: 'badge-warning',
   organizer: 'badge-info',
   manager: 'badge-danger',
   guide: 'badge-success',
 };

const EMPLOYEE_ROLE_OPTIONS = ['guide', 'organizer', 'admin'];

function AgencyDashboardPage() {
  const [selectedPeriod, setSelectedPeriod] = useState('month');
  const [notification, setNotification] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [employeesLoading, setEmployeesLoading] = useState(true);
  const [employeesError, setEmployeesError] = useState(null);
  const [searchEmail, setSearchEmail] = useState('');
  const [foundUser, setFoundUser] = useState(null);
  const [searchError, setSearchError] = useState(null);
  const [searching, setSearching] = useState(false);
  const [savingNew, setSavingNew] = useState(false);
  const [updatingId, setUpdatingId] = useState(null);


  useEffect(() => {
   let cancelled = false;
   getEmployees()
     .then((list) => {
       if (!cancelled) setEmployees(list);
       if (!cancelled) setEmployees(list.filter((e) => e.role !== 'manager'));
     })
     .catch((err) => {
       if (!cancelled) setEmployeesError(err.message);
     })
     .finally(() => {
       if (!cancelled) setEmployeesLoading(false);
     });
   return () => {
     cancelled = true;
   };
 }, []);

 
 const handleSearchUser = async (e) => {
  e.preventDefault();
  const query = searchEmail.trim();
  setFoundUser(null);

   if (!query) {
      setSearchError('Please enter a registered user email address.');
      return;
    }

    setSearching(true);
    setSearchError(null);
    try {
      const user = await findUserByEmail(query);
      if (!user) {
        setSearchError(`No registered user found with email "${query}".`);
        return;
      }
      if (user.isEmployee) {
        setSearchError(`"${user.name}" is already an employee with role: ${ROLE_LABELS[user.role]}.`);
        return;
      }
      setFoundUser({ ...user, selectedRole: 'guide' });
    } catch (err) {
      setSearchError(err.message);
    } finally {
      setSearching(false);
    }
 };
  

const handleSaveNewEmployee = async () => {
    if (!foundUser) return;
    setSavingNew(true);
    try {
      const saved = await changeRole(foundUser.id, foundUser.selectedRole);
      setEmployees((prev) => [...prev, saved]);
      setNotification(`${saved.name} added as ${ROLE_LABELS[saved.role]}.`);
      setFoundUser(null);
      setSearchEmail('');
    } catch (err) {
      setSearchError(err.message);
    } finally {
      setSavingNew(false);
    }
  };

  const handleRoleChange = async (employeeId, newRole) => {
    const employee = employees.find((e) => e.id === employeeId);
    if (!employee) return;

    if (!window.confirm(`Are you sure you want to change the role for ${employee.name} to ${ROLE_LABELS[newRole]}?`)) {
      return;
    }

   setUpdatingId(employeeId);
    try {
      const updated = await changeRole(employeeId, newRole);
      if (updated.isEmployee) {
        setEmployees((prev) => prev.map((e) => (e.id === employeeId ? updated : e)));
        setNotification(`${updated.name} is now ${ROLE_LABELS[updated.role]}.`);
      } else {
        setEmployees((prev) => prev.filter((e) => e.id !== employeeId));
        setNotification(`${updated.name} was revoked to Client.`);
      }
    } catch (err) {
      window.alert(`Could not change role: ${err.message}`);
    } finally {
      setUpdatingId(null);
    }
  };

  const currentMetrics = periodMetrics[selectedPeriod] || periodMetrics.month;

  return (
    <div className="page-container">
      <div
        className="section-header-row"
        style={{ marginBottom: '1.75rem', alignItems: 'center' }}
      >
        <div>
          <h2 style={{ margin: '0 0 0.4rem 0', fontSize: '1.75rem', color: '#111827' }}>
            Agency Manager Dashboard
          </h2>
        </div>

        <div>
          <button
            type="button"
            className="btn btn-primary"
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
            <span>Export as CSV</span>
          </button>
        </div>
      </div>

      {notification && (
        <div className="toast-banner" role="status">
          <span>✓ {notification}</span>
          <button
            type="button"
            className="btn btn-sm btn-secondary"
            onClick={() => setNotification(null)}
          >
            Dismiss
          </button>
        </div>
      )}

      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Key Performance Indicators
            </h3>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <label htmlFor="period-selector" className="form-label" style={{ margin: 0, fontWeight: 600 }}>
              Selected Period:
            </label>
            <select
              id="period-selector"
              className="form-select"
              style={{ width: 'auto', padding: '0.4rem 0.8rem', fontSize: '0.9rem' }}
              value={selectedPeriod}
              onChange={(e) => setSelectedPeriod(e.target.value)}
            >
              <option value="week">This Week</option>
              <option value="month">This Month</option>
              <option value="year">This Year</option>
            </select>
          </div>
        </div>

        <div className="stat-card-grid" style={{ marginBottom: '1.25rem' }}>
          <div className="stat-card">
            <span className="stat-card-label">Total Bookings</span>
            <div className="stat-card-value">{currentMetrics.totalBookings}</div>
            <span className="stat-card-sub" style={{ color: '#16a34a' }}>
              Confirmed reservations in selected period
            </span>
          </div>

          <div className="stat-card">
            <span className="stat-card-label">Generated Revenue</span>
            <div className="stat-card-value">
              €{currentMetrics.generatedRevenue.toLocaleString()}
            </div>
            <span className="stat-card-sub" style={{ color: '#16a34a' }}>
              Gross bookings &amp; extras revenue
            </span>
          </div>

          <div className="stat-card" style={{ gridColumn: 'span 1' }}>
            <span className="stat-card-label">Top 5 Most Popular Activities</span>
            <ol style={{ margin: '0.5rem 0 0 0', paddingLeft: '1.2rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
              {topFiveActivities.map((act) => (
                <li key={act.rank} style={{ fontSize: '0.85rem', color: '#1f2937' }}>
                  <span style={{ fontWeight: 600 }}>{act.title}</span>
                  <span style={{ color: '#64748b', fontSize: '0.8rem' }}> ({act.bookingsCount} bookings)</span>
                </li>
              ))}
            </ol>
          </div>

          <div className="stat-card" style={{ gridColumn: 'span 1' }}>
            <span className="stat-card-label">Top 5 Most Requested Add-ons</span>
            <ol style={{ margin: '0.5rem 0 0 0', paddingLeft: '1.2rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
              {topFiveAddons.map((addon) => (
                <li key={addon.rank} style={{ fontSize: '0.85rem', color: '#1f2937' }}>
                  <span style={{ fontWeight: 600 }}>{addon.name}</span>
                  <span style={{ color: '#64748b', fontSize: '0.8rem' }}> ({addon.count} requests)</span>
                </li>
              ))}
            </ol>
          </div>
        </div>
      </section>

      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Occupancy Tracker (Per Time Slot)
            </h3>
          </div>
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Tour Name</th>
                <th>Departure Slot</th>
                <th>Category</th>
                <th>Bookings / Capacity</th>
                <th>Occupancy Rate (%)</th>
                <th style={{ textAlign: 'right' }}>Generated Revenue (€)</th>
              </tr>
            </thead>
            <tbody>
              {tourPerformanceData.map((slot) => {
                const isHighOccupancy = slot.occupancyRate >= 80;
                const isMediumOccupancy = slot.occupancyRate >= 60 && slot.occupancyRate < 80;

                return (
                  <tr key={slot.id}>
                    <td style={{ fontWeight: 600 }}>{slot.title}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>{slot.dateTime}</td>
                    <td>
                      <span className="badge badge-info">{slot.category}</span>
                    </td>
                    <td>{slot.bookingsCount} / {slot.capacity} seats</td>
                    <td>
                      <span
                        className={`badge ${
                          isHighOccupancy
                            ? 'badge-success'
                            : isMediumOccupancy
                            ? 'badge-warning'
                            : 'badge-neutral'
                        }`}
                      >
                        {slot.occupancyRate}%
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 600, color: '#0f172a' }}>
                      €{slot.revenue.toLocaleString()}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>

      <section style={{ marginBottom: '2.5rem' }}>
        <div className="section-header-row">
          <div>
            <h3 style={{ margin: '0 0 0.25rem 0', fontSize: '1.25rem', color: '#1f2937' }}>
              Employee Management
            </h3>
          </div>
        </div>

        <div
          className="card"
          style={{
            marginBottom: '1.25rem',
            padding: '1.25rem',
            backgroundColor: '#f8fafc',
            border: '1px solid #e2e8f0',
          }}
        >
          <h4 style={{ margin: '0 0 0.6rem 0', fontSize: '1.05rem', color: '#111827' }}>
            Add new employee
          </h4>
          <form onSubmit={handleSearchUser} style={{ display: 'flex', gap: '0.6rem', flexWrap: 'wrap', alignItems: 'center' }}>
            <div style={{ flex: '1', minWidth: '240px' }}>
              <input
                type="email"
                className="form-input"
                placeholder="Enter registered user email (e.g. ana.horvat@example.com)..."
                value={searchEmail}
                onChange={(e) => setSearchEmail(e.target.value)}
              />
            </div>
            <button type="submit" className="btn btn-primary" style={{ whiteSpace: 'nowrap' }} disabled={searching}>
            {searching ? 'Searching...' : 'Search User'}
             </button>
          </form>

          {searchError && (
            <div style={{ marginTop: '0.75rem', color: '#b91c1c', fontSize: '0.875rem' }}>
              ✕ {searchError}
            </div>
          )}

          {foundUser && (
            <div
              style={{
                marginTop: '1rem',
                padding: '1rem',
                backgroundColor: '#ffffff',
                border: '1px solid #cbd5e1',
                borderRadius: '6px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '0.75rem',
              }}
            >
              <div>
                <span style={{ fontSize: '0.8rem', color: '#64748b', display: 'block' }}>Registered User Found:</span>
                <strong style={{ fontSize: '1rem', color: '#0f172a' }}>{foundUser.name}</strong>{' '}
                <span style={{ fontSize: '0.85rem', color: '#475569' }}>({foundUser.email})</span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                <label htmlFor="new-emp-role" className="form-label" style={{ margin: 0, fontSize: '0.85rem' }}>
                  Assign Role:
                </label>
                <select
                  id="new-emp-role"
                  className="form-select"
                  style={{ width: 'auto', padding: '0.35rem 0.6rem', fontSize: '0.85rem' }}
                  value={foundUser.selectedRole}
                  onChange={(e) =>
                    setFoundUser({ ...foundUser, selectedRole: e.target.value })
                  }
                >
                  {EMPLOYEE_ROLE_OPTIONS.map((role) => (
                   <option key={role} value={role}>{ROLE_LABELS[role]}</option>
                     ))}
                </select>

                <button
                  type="button"
                  className="btn btn-sm btn-primary"
                  onClick={handleSaveNewEmployee}
                  disabled={savingNew}
                >
                  {savingNew ? 'Saving...' : 'Save'}
                </button>
                <button
                  type="button"
                  className="btn btn-sm btn-secondary"
                  onClick={() => {
                    setFoundUser(null);
                    setSearchEmail('');
                  }}
                  disabled={savingNew}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>

        <div className="data-table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Employee Name</th>
                <th>Email Address</th>
                <th>Current Role</th>
                <th style={{ textAlign: 'right' }}>Role Action</th>
              </tr>
            </thead>
            <tbody>
                {employeesLoading && (
                   <tr>
                     <td colSpan={4} style={{ textAlign: 'center', color: '#64748b' }}>
                     Loading employees...
                     </td>
                    </tr>
                )}

               {!employeesLoading && employeesError && (
                <tr>
                  <td colSpan={4} style={{ textAlign: 'center', color: '#b91c1c' }}>
                  ✕ Could not load employees: {employeesError}
                 </td>
                </tr>
   )}

   {!employeesLoading && !employeesError && employees.length === 0 && (
     <tr>
       <td colSpan={4} style={{ textAlign: 'center', color: '#64748b' }}>
         No employees yet.
       </td>
     </tr>
   )}
              {employees.map((emp) => (
                <tr key={emp.id}>
                  <td style={{ fontWeight: 600 }}>{emp.name}</td>
                  <td>{emp.email}</td>
                  <td>
                    <span className={`badge ${ROLE_BADGE[emp.role] ?? 'badge-neutral'}`}>
                   {ROLE_LABELS[emp.role] ?? emp.role}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', justifyContent: 'flex-end' }}>
                      <label htmlFor={`role-select-${emp.id}`} style={{ fontSize: '0.8rem', color: '#64748b' }}>
                        Change to:
                      </label>
                      <select
                        id={`role-select-${emp.id}`}
                        className="form-select"
                        style={{ width: 'auto', padding: '0.25rem 0.5rem', fontSize: '0.8rem' }}
                        value={emp.role}
                        disabled={updatingId==emp.id}
                        onChange={(e) => handleRoleChange(emp.id, e.target.value)}
                      >
                      {EMPLOYEE_ROLE_OPTIONS.map((role) => (
                         <option key={role} value={role}>{ROLE_LABELS[role]}</option>
                       ))}
                      <option value="client">Revoke to Client</option>
                      </select>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default AgencyDashboardPage;
