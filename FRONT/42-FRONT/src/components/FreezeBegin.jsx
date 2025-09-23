import React, { useEffect, useState } from "react";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

// Custom error class for consistent error handling
class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

const FreezeBegin = ({ user, kind, users }) => {
  const [userCursus, setUserCursus] = useState(null);
  const [locationStats, setLocationStats] = useState(null);
  const [freeze, setFreeze] = useState(null);
  const [login, setLogin] = useState(kind === "admin" ? "" : user.login);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [nbDays, setNbDays] = useState(0);
  const [nbOpenDays, setNbOpenDays] = useState(0);
  const [totalHours, setTotalHours] = useState(0);

  const fetchFreezeData = async (loginParam) => {
    setLoading(true);
    setError(null);
    try {
      let url = `${API_BASE_URL}/api/freeze`;
      if (loginParam && kind === "admin") url += `?login=${encodeURIComponent(loginParam)}`;
      const response = await fetch(url, { credentials: "include" });
      if (!response.ok) {
        if (response.status === 401) {
          setError("Authentication required. Redirecting to login...");
          setTimeout(() => {
            window.location.href = `${API_BASE_URL}/login`;
          }, 2000);
          return;
        }
        const errorData = await response.json().catch(() => ({}));
        throw new ResponseStatusException(response.status, errorData.error || `HTTP error! status: ${response.status}`);
      }
      const data = await response.json();

      if (!data.userCursus && !data.locationStats) {
        throw new ResponseStatusException(404, "No data available for this user");
      }

      setUserCursus(data.userCursus || null);
      setLocationStats(data.locationStats || null);
      setFreeze(data.freeze || 0);
      setLogin(data.login || loginParam || user.login);
      setNbDays(data.nbDays || 0);
      setNbOpenDays(data.nbOpenDays || 0);
      setTotalHours(data.totalHours || 0);
    } catch (err) {
      console.error("Error fetching freeze data:", err.message);
      setError(err.message || "Failed to fetch freeze data. Please try again.");
      setUserCursus(null);
      setLocationStats(null);
      setFreeze(null);
      setNbDays(0);
      setNbOpenDays(0);
      setTotalHours(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFreezeData(kind === "admin" ? "" : user.login);
  }, [user.login, kind]);

  const handleSearch = (e) => {
    e.preventDefault();
    if (kind === "admin" && !login.trim()) {
      setError("Please enter a login to search");
      return;
    }
    fetchFreezeData(login);
  };

  const handleLoginChange = (e) => {
    const value = e.target.value;
    setLogin(value);

    if (kind === "admin" && value.length > 0) {
      const filteredSuggestions = users
        .filter((u) => u.login?.toLowerCase().includes(value.toLowerCase()))
        .map((u) => u.login)
        .slice(0, 5);
      setSuggestions(filteredSuggestions);
      setShowSuggestions(true);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    setLogin(suggestion);
    setSuggestions([]);
    setShowSuggestions(false);
    fetchFreezeData(suggestion);
  };

  return (
    <div className="main-content">
      <ErrorPopup error={error} setError={setError} />

      <div className="dashboard-container">
        <div className="dashboard-header">
          {kind === "admin" && (
            <form onSubmit={handleSearch} className="search-form">
              <div className="filter-box">
                <label htmlFor="query">Search Login</label>
                <input
                  id="query"
                  className="input"
                  type="text"
                  placeholder="Search login..."
                  value={login}
                  onChange={handleLoginChange}
                  onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                  autoComplete="off"
                  required
                />
                {showSuggestions && suggestions.length > 0 && (
                  <ul className="suggestions-list">
                    {suggestions.map((suggestion, index) => (
                      <li
                        key={index}
                        onClick={() => handleSuggestionClick(suggestion)}
                        onMouseDown={(e) => e.preventDefault()}
                      >
                        {suggestion}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </form>
          )}

          <h1 className="dashboard-title">
            Dashboard Cursus{" "}
            {userCursus?.user?.login && <span>{userCursus.user.login}</span>}
          </h1>
          {loading && <p>Loading data...</p>}
        </div>

        <div className="content-area">
          <div className="stats-grid">
            {userCursus && (
              <div className="milestone-section">
                <div className="milestone-content">
                  <div className="stat-header">
                    <span className="milestone-title">Current Milestone</span>
                    <span className="milestone-value">
                      Level {userCursus.milestone}
                    </span>
                  </div>
                </div>
              </div>
            )}

            {freeze != null && (
              <div className="stat-card freeze-card">
                <div className="stat-header">
                  <i className="fas fa-snowflake stat-icon"></i>
                  <span className="stat-label">Freeze Days</span>
                  <span className="stat-value">{Math.floor(freeze)} days</span>
                </div>
              </div>
            )}

            {kind === "admin" && locationStats && (
              <>
                <div className="stat-card">
                  <div className="stat-header">
                    <i className="fas fa-calendar-day stat-icon"></i>
                    <span className="stat-label">Total Days</span>
                    <span className="stat-value">{nbDays}</span>
                  </div>
                </div>

                <div className="stat-card">
                  <div className="stat-header">
                    <i className="fas fa-business-time stat-icon"></i>
                    <span className="stat-label">Open Days</span>
                    <span className="stat-value">{nbOpenDays}</span>
                  </div>
                </div>

                <div className="stat-card">
                  <div className="stat-header">
                    <i className="fas fa-clock stat-icon"></i>
                    <span className="stat-label">Total Hours</span>
                    <span className="stat-value">
                      {Math.floor(totalHours)}h
                    </span>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default FreezeBegin;