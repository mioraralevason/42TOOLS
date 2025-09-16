import React, { useState, useEffect } from "react";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

const CheckAdmin = () => {
  const [startDate, setStartDate] = useState("2025-09-01");
  const [endDate, setEndDate] = useState("2025-09-15");
  const [userStats, setUserStats] = useState([]);
  const [globalRate, setGlobalRate] = useState(0);
  const [totalDays, setTotalDays] = useState(0);
  const [totalHours, setTotalHours] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);

    try {
      // 1️⃣ Call global presence rate
      const resGlobal = await fetch(
        `${API_BASE_URL}/stats/global?startDate=${startDate}&endDate=${endDate}`,
        { credentials: "include" }
      );
      if (!resGlobal.ok) throw new Error(`HTTP ${resGlobal.status}`);
      const global = await resGlobal.json();
      setGlobalRate(global || 0);

      // 2️⃣ Call all users stats
      const resUsers = await fetch(
        `${API_BASE_URL}/stats/users?startDate=${startDate}&endDate=${endDate}`,
        { credentials: "include" }
      );
      if (!resUsers.ok) throw new Error(`HTTP ${resUsers.status}`);
      const users = await resUsers.json();
      setUserStats(users);

      // Calculate total days and total hours
      let days = 0;
      let hours = 0;
      users.forEach((u) => {
        days += u.joursPresent || 0;
        // hours = approx total: 1 day = 8h * tauxPresence%
        hours += ((u.joursTotaux || 0) * (u.tauxPresence || 0) * 8) / 100;
      });
      setTotalDays(days);
      setTotalHours(hours.toFixed(2));
    } catch (err) {
      console.error(err);
      setError(err.message || "Unknown error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, [startDate, endDate]);

  const handleSubmit = (e) => {
    e.preventDefault();
    fetchStats();
  };

  return (
    <div className="checking-admin">
      <ErrorPopup error={error} />

      <form onSubmit={handleSubmit} className="checking-form">
        <div className="date-group">
          <div>
            <label htmlFor="startDate">Start Date</label>
            <input
              type="date"
              id="startDate"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              required
            />
          </div>
          <div>
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
      </form>

      {loading && <p>Loading data...</p>}

      {!loading && (
        <div className="results-section">
          <h3>Global Attendance</h3>
          <p>Global Presence Rate: {globalRate}%</p>

          <h3>Per User Attendance</h3>
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
  );
};

export default CheckAdmin;
