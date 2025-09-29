import React, { useState, useEffect } from "react";
import "../index.css";

const Calendar = ({ userResponse, kind, suggestions = [] }) => {
  const [view, setView] = useState("month");
  const [page, setPage] = useState(0);
  const [calendarData, setCalendarData] = useState(null);
  const [login, setLogin] = useState(kind === "admin" ? "" : userResponse?.login || "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filteredSuggestions, setFilteredSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const year = new Date().getFullYear();
  const month = new Date().getMonth();

  const fetchCalendar = async (loginValue = "") => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams({ year: year.toString() });
      if (view === "month") params.append("month", (month + 1).toString());

      if (kind === "admin" && loginValue.trim() !== "") {
        params.append("login", loginValue.trim());
      } else if (kind !== "admin" && userResponse?.login) {
        params.append("login", userResponse.login);
      }

      const res = await fetch(`http://localhost:9090/calendar?${params}`, { credentials: "include" });
      const data = await res.json();
      if (data.error) throw new Error(data.error);
      console.log("Calendar data fetched:", data); // Debug log
      setCalendarData(data);
    } catch (err) {
      console.error("Erreur chargement calendrier :", err);
      setError(err.message || "Erreur lors du chargement du calendrier");
    } finally {
      setLoading(false);
    }
  };

  // Initial fetch for non-admin users
  useEffect(() => {
    if (kind !== "admin" && userResponse?.login) {
      fetchCalendar(userResponse.login);
    }
  }, [view, userResponse, kind]);

  // Suggestions autocomplete login
  const handleLoginChange = (e) => {
    const value = e.target.value;
    setLogin(value);

    if (kind === "admin" && value.trim() !== "") {
      const matches = suggestions
        .filter((s) => s.toLowerCase().includes(value.toLowerCase()))
        .slice(0, 5);
      setFilteredSuggestions(matches);
      setShowSuggestions(matches.length > 0);
    } else {
      setFilteredSuggestions([]);
      setShowSuggestions(false);
    }
  };

  const handleSuggestionClick = (s) => {
    setLogin(s);
    setFilteredSuggestions([]);
    setShowSuggestions(false);
    fetchCalendar(s);
  };

  const generateMonthDays = (year, month) => {
    const firstDayOfMonth = new Date(year, month, 1).getDay();
    const lastDay = new Date(year, month + 1, 0).getDate();
    const days = [];
    const offset = firstDayOfMonth === 0 ? 6 : firstDayOfMonth - 1; // Adjust for Monday start

    // Check if blackholed date is within 3 weeks
    let isBlackholedNear = false;
    if (calendarData?.blackholed_at) {
      try {
        const today = new Date();
        const blackholedDate = new Date(calendarData.blackholed_at);
        const diffTime = blackholedDate - today;
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
        isBlackholedNear = diffDays > 0 && diffDays <= 21;
        console.log(
          `Blackholed date: ${calendarData.blackholed_at}, Today: ${today.toISOString().split('T')[0]}, Diff: ${diffDays} days, isBlackholedNear: ${isBlackholedNear}`
        ); // Debug log
      } catch (e) {
        console.error("Error parsing blackholed_at date:", e);
      }
    } else {
      console.log("No blackholed_at date available");
    }

    // Add empty cells for offset
    for (let i = 0; i < offset; i++) {
      days.push({ day: null, isPresent: false, milestone: null, isDeadline: false, isBlackholedNear });
    }

    // Add actual days
    for (let i = 1; i <= lastDay; i++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(i).padStart(2, "0")}`;
      const isPresent = calendarData?.presence?.includes(dateStr);

      // Check for milestone
      const milestone = calendarData?.milestones?.find((m) => {
        if (!m.date) return false;
        return m.date === dateStr;
      });

      // Check for deadline (blackholed_at)
      const isDeadline = calendarData?.blackholed_at
        ? calendarData.blackholed_at === dateStr
        : false;

      days.push({ day: i, isPresent, milestone, isDeadline, isBlackholedNear });
    }

    return days;
  };

  const renderMonth = (year, month) => {
    const days = generateMonthDays(year, month);
    const monthName = new Date(year, month).toLocaleString("fr-FR", { month: "long" });
    const weekdays = ["Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"];

    return (
      <div className="calendar-block" key={`${year}-${month}`}>
        <h4>{monthName} {year}</h4>
        <div className="calendar-weekdays">
          {weekdays.map((d, idx) => (
            <span key={idx}>{d}</span>
          ))}
        </div>
        <div className="calendar-grid">
          {days.map((d, idx) => (
            <div
              key={idx}
              className={`calendar-day ${d.day ? "" : "empty"} ${d.isPresent ? "presence" : ""} ${
                d.milestone ? "milestone" : ""
              } ${d.isDeadline ? "deadline" : ""} ${d.isDeadline && d.isBlackholedNear ? "blink" : ""}`}
              title={
                d.day
                  ? d.milestone
                    ? `Milestone: Niveau ${d.milestone.level} (${d.milestone.date})`
                    : d.isDeadline
                    ? "Date limite (Blackholed)"
                    : ""
                  : ""
              }
            >
              {d.day && <span className="day-number">{d.day}</span>}
              {d.milestone && <span className="milestone-marker">🏆</span>}
              {d.isDeadline && <span className="deadline-marker">⚠️</span>}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderCalendar = () => {
    const baseYear = year;
    const baseMonth = month;
    if (view === "month") return renderMonth(baseYear, baseMonth);
    if (view === "quarter") {
      const startMonth = page * 3;
      return (
        <div className="calendar-multi">
          {Array.from({ length: 3 }, (_, i) => renderMonth(baseYear, startMonth + i))}
        </div>
      );
    }
    if (view === "semester") {
      const startMonth = page * 6;
      return (
        <div className="calendar-multi">
          {Array.from({ length: 6 }, (_, i) => renderMonth(baseYear, startMonth + i))}
        </div>
      );
    }
    if (view === "year") {
      return (
        <div className="calendar-multi">
          {Array.from({ length: 12 }, (_, i) => renderMonth(baseYear, i))}
        </div>
      );
    }
    return null;
  };

  const renderBlackholedAlert = () => {
    if (!calendarData?.blackholed_at) return null;

    const today = new Date();
    const blackholedDate = new Date(calendarData.blackholed_at);
    const diffDays = 21;

    if (diffDays > 0 && diffDays <= 21) {
      return (
        <div className="alert-warning">
          ⚠️ Attention : La date blackholed est dans {diffDays} jour{diffDays > 1 ? "s" : ""} !
        </div>
      );
    }
    return null;
  };

  const renderImportantDates = () => {
    return (
      <div className="important-dates">
        <h4>Dates Importantes</h4>
        {calendarData?.milestoneDates && calendarData.milestoneDates.length > 0 ? (
          <div className="milestone-dates">
            <h5>Milestones</h5>
            <ul>
              {calendarData.milestoneDates.map((m, index) => (
                <li key={index}>Niveau {m.level}: {m.date}</li>
              ))}
            </ul>
          </div>
        ) : (
          <p>Aucune date de milestone disponible.</p>
        )}
        {calendarData?.blackholed_at ? (
          <div className="blackholed-date">
            <h5>Date Blackholed</h5>
            <p>{calendarData.blackholed_at}</p>
          </div>
        ) : (
          <p>Aucune date blackholed disponible.</p>
        )}
      </div>
    );
  };

  const handlePrev = () => setPage((p) => Math.max(0, p - 1));
  const handleNext = () => {
    const maxPage = view === "quarter" ? 3 : view === "semester" ? 1 : 0;
    setPage((p) => Math.min(maxPage, p + 1));
  };

  return (
    <div className="checking-admin">
      <div className="checking-form">
        <h2>Calendrier Étudiant</h2>

        {kind === "admin" && (
          <div className="filter-box">
            <label htmlFor="login">Login Étudiant</label>
            <div className="input-container">
              <input
                type="text"
                id="login"
                placeholder="Entrer le login..."
                value={login}
                onChange={handleLoginChange}
                onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                autoComplete="off"
              />
              {showSuggestions && filteredSuggestions.length > 0 && (
                <ul className="suggestions-list">
                  {filteredSuggestions.map((s, i) => (
                    <li key={i} onMouseDown={() => handleSuggestionClick(s)}>
                      {s}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <button type="button" onClick={() => fetchCalendar(login)}>
              Charger le Calendrier
            </button>
          </div>
        )}

        <div className="date-group">
          <label htmlFor="view">Vue</label>
          <select
            id="view"
            value={view}
            onChange={(e) => {
              setView(e.target.value);
              setPage(0);
              if (kind !== "admin" && userResponse?.login) {
                fetchCalendar(userResponse.login);
              }
            }}
          >
            <option value="month">Mois</option>
            <option value="quarter">Trimestre</option>
            <option value="semester">Semestre</option>
            <option value="year">Année</option>
          </select>
        </div>
      </div>

      <div className="results-section">
        {loading ? (
          <p className="loading">⏳ Chargement du calendrier...</p>
        ) : error ? (
          <p className="error">{error}</p>
        ) : calendarData ? (
          <>
            <div className="calendar-pagination">
              <button onClick={handlePrev} disabled={page === 0}>
                ◀ Précédent
              </button>
              <span>Page {page + 1}</span>
              <button onClick={handleNext}>Suivant ▶</button>
            </div>
            {renderBlackholedAlert()}
            {renderCalendar()}
            {renderImportantDates()}
          </>
        ) : (
          <p className="no-data">Aucune donnée disponible. Veuillez charger un calendrier.</p>
        )}
      </div>
    </div>
  );
};

export default Calendar;