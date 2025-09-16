import React, { useState, useEffect } from "react";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

const CheckUser = () => {
  const [login, setLogin] = useState("");
  const [startDate, setStartDate] = useState("2025-09-01");
  const [endDate, setEndDate] = useState("2025-09-15");
  const [userStats, setUserStats] = useState([]);
  const [dayCount, setDayCount] = useState(0);
  const [hourCount, setHourCount] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchPerformed, setSearchPerformed] = useState(false);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);

    try {
      // Simulation ou appel API
      const url = login
        ? `${API_BASE_URL}/stats/users?startDate=${startDate}&endDate=${endDate}&login=${encodeURIComponent(login)}`
        : `${API_BASE_URL}/stats/users?startDate=${startDate}&endDate=${endDate}`;

      const res = await fetch(url, { credentials: "include" });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const users = await res.json();

      let totalDays = 0;
      let totalHours = 0;
      users.forEach((u) => {
        totalDays += u.joursPresent || 0;
        totalHours += ((u.joursTotaux || 0) * (u.tauxPresence || 0) * 8) / 100;
      });

      setUserStats(users);
      setDayCount(totalDays);
      setHourCount(totalHours.toFixed(2));
      setSearchPerformed(true);
    } catch (err) {
      console.error(err);
      setError(err.message || "Unknown error");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    fetchStats();
  };

  return (
    <div className="main-content">
      <ErrorPopup error={error} />

      <div className="dashboard-container">
        <div className="dashboard-header">
          <form onSubmit={handleSubmit} className="checking-form">
            <div className="date-group">
              <div className="date-input-wrapper">
                <label htmlFor="userId">Student Login</label>
                <input
                  type="text"
                  id="userId"
                  value={login}
                  onChange={(e) => setLogin(e.target.value)}
                  placeholder="Enter login (optional)"
                />
              </div>

              <div className="date-input-wrapper">
                <label htmlFor="startDate">Start Date</label>
                <input
                  type="date"
                  id="startDate"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                />
              </div>

              <div className="date-input-wrapper">
                <label htmlFor="endDate">End Date</label>
                <input
                  type="date"
                  id="endDate"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                />
              </div>
            </div>

            <button type="submit">
              <i className="fa fa-search"></i> Check Attendance
            </button>
          </form>
        </div>

        {loading && <p>Loading data...</p>}

        {searchPerformed && !loading && (
          <div className="content-area">
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-header">
                  <span className="stat-label">Day(s) of Attendance</span>
                  <span className="stat-value">{dayCount}</span>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-header">
                  <span className="stat-label">Hour(s) of Attendance</span>
                  <span className="stat-value">{hourCount}</span>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-header">
                  <span className="stat-label">Date Range</span>
                  <span className="stat-value">
                    {startDate} → {endDate}
                  </span>
                </div>
              </div>
            </div>

            {userStats.length > 0 ? (
              <table className="results-table">
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Days Present</th>
                    <th>Total Days</th>
                    <th>Presence Rate (%)</th>
                  </tr>
                </thead>
                <tbody>
                  {userStats.map((u, i) => (
                    <tr key={i}>
                      <td>{u.displayname}</td>
                      <td>{u.joursPresent}</td>
                      <td>{u.joursTotaux}</td>
                      <td>{u.tauxPresence}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p>No attendance records found for this period.</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default CheckUser;
