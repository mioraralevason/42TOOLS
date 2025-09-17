import React, { useState, useEffect } from "react";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

ChartJS.register(ArcElement, Tooltip, Legend);

const ITEMS_PER_PAGE = 10;

const Check = () => {
  const [startDate, setStartDate] = useState("2025-09-01");
  const [endDate, setEndDate] = useState("2025-09-15");
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
      if (!resGlobal.ok) throw new Error(`HTTP ${resGlobal.status}`);
      const global = await resGlobal.json();
      setGlobalRate(global || 0);

      const resUsers = await fetch(
        `${API_BASE_URL}/stats/users?startDate=${startDate}&endDate=${endDate}`,
        { credentials: "include" }
      );
      if (!resUsers.ok) throw new Error(`HTTP ${resUsers.status}`);
      const users = await resUsers.json();
      setUserStats(users);

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

  // Doughnut data & options
  const doughnutData = {
    labels: ["Présent", "Absent"],
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
    cutout: "70%", // plus petit camembert
    plugins: {
      legend: { display: false },
      tooltip: { enabled: false },
      // Taux au centre
      beforeDraw: (chart) => {},
    },
  };

  return (
    <div className="checking-admin">
      <ErrorPopup error={error} />

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
      </form>

      {loading && <p>Loading data...</p>}

      {!loading && (
        <div className="results-section">
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

          {/* Filter / autocomplete */}
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
                      <td>{u.displayname}</td>
                      <td>{u.login}</td>
                      <td>{u.joursPresent}</td>
                      <td>{u.joursTotaux}</td>
                      <td>{u.tauxPresence}</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {/* Pagination */}
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
