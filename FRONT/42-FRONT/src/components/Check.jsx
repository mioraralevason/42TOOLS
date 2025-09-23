import React, { useState, useEffect } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

ChartJS.register(ArcElement, Tooltip, Legend);

// Custom error class for consistent error handling
class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

const ITEMS_PER_PAGE = 10;

const Check = ({ user, kind }) => {
  const [today, setToday] = useState(new Date());
  const [startDate, setStartDate] = useState(today.setMonth(today.getMonth() - 1) > new Date().getMonth() - 1
    ? new Date(new Date().setMonth(new Date().getMonth() - 1)).toISOString().split('T')[0]
    : new Date(new Date().setMonth(new Date().getMonth() - 2)).toISOString().split('T')[0]);
  const [endDate, setEndDate] = useState(today.toISOString().split('T')[0] > new Date().toISOString().split('T')[0]
    ? new Date().toISOString().split('T')[0]
    : new Date(new Date().setDate(new Date().getDate() + 1)).toISOString().split('T')[0]);
  const [userStats, setUserStats] = useState([]);
  const [globalRate, setGlobalRate] = useState(0);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [filterLogin, setFilterLogin] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);

  const fetchStats = async () => {
    setLoading(true);
    setError(null);
    try {
      const resGlobal = await fetch(
        `${API_BASE_URL}/stats/global?startDate=${startDate}&endDate=${endDate}`,
        { credentials: "include" }
      );
      if (!resGlobal.ok) {
        if (resGlobal.status === 401) {
          setError("Authentication required. Redirecting to login...");
          setTimeout(() => {
            window.location.href = `${API_BASE_URL}/login`;
          }, 2000);
          return;
        }
        const errorData = await resGlobal.json().catch(() => ({}));
        throw new ResponseStatusException(resGlobal.status, errorData.error || `HTTP error! status: ${resGlobal.status}`);
      }
      const global = await resGlobal.json();
      setGlobalRate(global || 0);

      if (kind === "admin") {
        const resUsers = await fetch(
          `${API_BASE_URL}/stats/users?startDate=${startDate}&endDate=${endDate}`,
          { credentials: "include" }
        );
        if (!resUsers.ok) {
          if (resUsers.status === 401) {
            setError("Authentication required. Redirecting to login...");
            setTimeout(() => {
              window.location.href = `${API_BASE_URL}/login`;
            }, 2000);
            return;
          }
          if (resUsers.status === 403) {
            setUserStats([]);
            return;
          }
          const errorData = await resUsers.json().catch(() => ({}));
          throw new ResponseStatusException(resUsers.status, errorData.error || `HTTP error! status: ${resUsers.status}`);
        }
        const users = await resUsers.json();
        setUserStats(users);
      } else {
        setUserStats([]);
      }
    } catch (err) {
      console.error("Error fetching stats:", err.message);
      setError(err.message || "Failed to fetch statistics. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, [startDate, endDate, kind]);

  useEffect(() => {
    if (filterLogin) {
      const matches = userStats
        .map((u) => u.login)
        .filter((login) =>
          login.toLowerCase().includes(filterLogin.toLowerCase())
        );
      setSuggestions(matches.slice(0, 5));
    } else {
      setSuggestions([]);
    }
    setCurrentPage(1);
  }, [filterLogin, userStats]);

  const filteredStats = filterLogin
    ? userStats.filter((u) =>
        u.login.toLowerCase().includes(filterLogin.toLowerCase())
      )
    : userStats;

  const totalPages = Math.ceil(filteredStats.length / ITEMS_PER_PAGE);
  const paginatedStats = filteredStats.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  );

  const doughnutData = {
    labels: ["Present", "Absent"],
    datasets: [
      {
        data: [globalRate, 100 - globalRate],
        backgroundColor: ["#00ffc0", "#333"],
        hoverBackgroundColor: ["#00ffd0", "#555"],
        borderWidth: 0,
      },
    ],
  };

  const doughnutOptions = {
    responsive: true,
    cutout: "70%",
    plugins: {
      legend: { display: false },
      tooltip: { enabled: false },
    },
  };

  return (
    <div className="checking-admin">
      <ErrorPopup error={error} setError={setError} />

      <form
        onSubmit={(e) => {
          e.preventDefault();
          fetchStats();
        }}
        className="checking-form"
      >
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
        <button type="submit" className="codepen-button" disabled={loading}>
          Fetch Stats
        </button>
      </form>

      {loading && <p>Loading data...</p>}

      {!loading && (
        <div className="results-section">
          {
            kind === "admin" && (
              <>
                <h3>Global Attendance</h3>
                <div style={{ position: "relative", width: "200px", margin: "0 auto" }}>
                  <Doughnut data={doughnutData} options={doughnutOptions} />
                  <div
                    style={{
                    position: "absolute",
                    top: "50%",
                    left: "50%",
                    transform: "translate(-50%, -50%)",
                    fontSize: "1.5rem",
                    fontWeight: "700",
                    color: "#00ffc0",
                  }}
                >
                  {globalRate}%
                </div>
              </div>
            </>
            )
          }

          {kind === "admin" && (
            <div className="filter-box">
              <label htmlFor="filterLogin">Filter by Login</label>
              <input
                type="text"
                id="filterLogin"
                placeholder="Enter login..."
                value={filterLogin}
                onChange={(e) => setFilterLogin(e.target.value)}
                autoComplete="off"
              />
              {suggestions.length > 0 && (
                <ul className="suggestions-list">
                  {suggestions.map((s, i) => (
                    <li key={i} onClick={() => setFilterLogin(s)}>
                      {s}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          {paginatedStats.length > 0 ? (
            <>
              <table className="results-table">
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Login</th>
                    <th>Days Present</th>
                    <th>Total Days</th>
                    <th>Presence Rate (%)</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedStats.map((u, i) => (
                    <tr key={i}>
                      <td>{u.firstName} {u.lastName?.toUpperCase()}</td>
                      <td>{u.login}</td>
                      <td>{u.joursPresent}</td>
                      <td>{u.joursTotaux}</td>
                      <td>{u.tauxPresence}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              <div className="pagination">
                <button
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage((p) => p - 1)}
                >
                  Prev
                </button>
                <span>
                  Page {currentPage} / {totalPages}
                </span>
                <button
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage((p) => p + 1)}
                >
                  Next
                </button>
              </div>
            </>
          ) : (
            <p>No attendance records found for this period.</p>
          )}
        </div>
      )}
    </div>
  );
};

export default Check;