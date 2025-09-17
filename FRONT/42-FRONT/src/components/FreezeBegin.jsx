import React, { useEffect, useState } from "react";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

const FreezeBegin = ({ user, kind, users }) => {
  const [userCursus, setUserCursus] = useState(null);
  const [locationStats, setLocationStats] = useState(null);
  const [freeze, setFreeze] = useState(null);
  const [login, setLogin] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [nbDays, setNbDays] = useState(0);
  const [nbOpenDays, setNbOpenDays] = useState(0);
  const [totalHours, setTotalHours] = useState(0);

  const fetchFreezeData = async (loginParam) => {
    setLoading(true);
    setError(null); // Reset error before fetching
    try {
      let url = `${API_BASE_URL}/api/freeze`;
      if (loginParam) url += `?login=${encodeURIComponent(loginParam)}`;
      const response = await fetch(url, { credentials: "include" });
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || "Failed to fetch freeze data");
      }
      const data = await response.json();

      if (!data.userCursus && !data.locationStats) {
        throw new Error("Aucune donnée disponible pour cet utilisateur");
      }

      setUserCursus(data.userCursus || null);
      setLocationStats(data.locationStats || null);
      setFreeze(data.freeze || 0);
      setLogin(data.login || "");
      setNbDays(data.nbDays || 0);
      setNbOpenDays(data.nbOpenDays || 0);
      setTotalHours(data.totalHours || 0);
    } catch (err) {
      console.error(err);
      setError(err.message || "Erreur inconnue");
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
    fetchFreezeData();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (!login.trim()) {
      setError("Veuillez entrer un login à rechercher");
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
        .slice(0, 5); // Limit to 5 suggestions
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
    fetchFreezeData(suggestion); // Fetch data for the selected login
  };

  return (
    <div className="main-content">
      <ErrorPopup error={error} />

      <div className="dashboard-container">
        <div className="dashboard-header">
          {kind === "admin" && (
            <form onSubmit={handleSearch} className="search-form">
              <div className="filter-box">
                <label htmlFor="query">Rechercher un login</label>
                <input
                  id="query"
                  className="input"
                  type="text"
                  placeholder="Rechercher un login..."
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
                        onMouseDown={(e) => e.preventDefault()} // Prevent blur on click
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
          {loading && <p>Chargement des données...</p>}
        </div>

        <div className="content-area">
          <div className="stats-grid">
            {userCursus && (
              <div className="milestone-section">
                <div className="milestone-content">
                  <div className="stat-header">
                    <span className="milestone-title">Milestone Actuel</span>
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
                  <span className="stat-label">Jours de freeze</span>
                  <span className="stat-value">{Math.floor(freeze)} jours</span>
                </div>
              </div>
            )}

            {kind === "admin" && locationStats && (
              <>
                <div className="stat-card">
                  <div className="stat-header">
                    <i className="fas fa-calendar-day stat-icon"></i>
                    <span className="stat-label">Jours total</span>
                    <span className="stat-value">{nbDays}</span>
                  </div>
                </div>

                <div className="stat-card">
                  <div className="stat-header">
                    <i className="fas fa-business-time stat-icon"></i>
                    <span className="stat-label">Jours ouvrables</span>
                    <span className="stat-value">{nbOpenDays}</span>
                  </div>
                </div>

                <div className="stat-card">
                  <div className="stat-header">
                    <i className="fas fa-clock stat-icon"></i>
                    <span className="stat-label">Total d'heures</span>
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