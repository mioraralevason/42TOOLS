import React, { useEffect, useState } from "react";
import API_BASE_URL from "../config.js";
import ErrorPopup from "./ErrorPopup.jsx";

const FreezeBegin = () => {
  const [userCursus, setUserCursus] = useState(null);
  const [locationStats, setLocationStats] = useState(null);
  const [freeze, setFreeze] = useState(null);
  const [listLogin, setListLogin] = useState([]);
  const [login, setLogin] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [nbDays, setNbDays] = useState(0);
  const [nbOpenDays, setNbOpenDays] = useState(0);
  const [totalHours, setTotalHours] = useState(0);

  const fetchFreezeData = async (loginParam) => {
    setLoading(true);
    try {
      let url = `${API_BASE_URL}/api/freeze`;
      if (loginParam) url += `?login=${encodeURIComponent(loginParam)}`;
      const response = await fetch(url, { credentials: "include" });
      const data = await response.json();

      if (data.error) throw new Error(data.error);

      setUserCursus(data.userCursus || null);
      setLocationStats(data.locationStats || null);
      setFreeze(data.freeze || 0);
      setListLogin(data.listLogin || []);
      setLogin(data.login || "");
      setError(null);
      setNbDays(data.nbDays || 0);
      setNbOpenDays(data.nbOpenDays || 0);
      setTotalHours(data.totalHours || 0);
      setIsAdmin(data.isAdmin);
    } catch (err) {
      console.error(err);
      setError(err.message || "Erreur inconnue");
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

  return (
    <div className="main-content">
      <ErrorPopup error={error} />

      <div className="dashboard-container">
        <div className="dashboard-header">
          {isAdmin && (
            <form onSubmit={handleSearch} className="search-form">
              <div className="group">
                <input
                  id="query"
                  className="input"
                  type="search"
                  placeholder="Rechercher un login..."
                  value={login}
                  onChange={(e) => setLogin(e.target.value)}
                  list="login-suggestions"
                  autoComplete="off"
                />
                <datalist id="login-suggestions">
                  {listLogin.map((lg, i) => (
                    <option key={i} value={lg} />
                  ))}
                </datalist>
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
          {!loading && !userCursus && !locationStats && (
            <p>Aucune donnée disponible pour cet utilisateur</p>
          )}

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

            {isAdmin && locationStats && (
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
