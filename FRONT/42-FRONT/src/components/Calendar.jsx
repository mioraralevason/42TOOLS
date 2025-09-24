import React, { useState, useEffect } from "react";
import "../index.css";

const Calendar = ({ userResponse, kind }) => {
  const [view, setView] = useState("month"); // "month" | "quarter" | "semester" | "year"
  const [page, setPage] = useState(0);
  const [calendarData, setCalendarData] = useState(null);
  const [login, setLogin] = useState(""); // pour admin
  const [loading, setLoading] = useState(false);

  const year = new Date().getFullYear();
  const month = new Date().getMonth(); // 0 = janvier

  // 🔹 Charger calendrier depuis backend
  const fetchCalendar = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        year,
        month: month + 1, // backend attend 1-12
      });
      if (kind === "admin" && login.trim() !== "") {
        params.append("login", login.trim());
      }

      const res = await fetch(`http://localhost:9090/calendar?${params}`, {
        credentials: "include", // important pour session Spring
      });
      if (!res.ok) throw new Error("Erreur API");
      const data = await res.json();
      setCalendarData(data);
    } catch (err) {
      console.error("Erreur chargement calendrier :", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCalendar();
  }, [kind, login]);

  // Génère les jours d’un mois spécifique
  const generateMonthDays = (year, month) => {
    const lastDay = new Date(year, month + 1, 0);
    const days = [];

    for (let i = 1; i <= lastDay.getDate(); i++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(
        i
      ).padStart(2, "0")}`;

      let type = null;
      if (calendarData?.presence?.includes(dateStr)) type = "presence";
      if (calendarData?.freezeDates?.includes(dateStr)) type = "freeze";
      if (calendarData?.bonus?.includes(dateStr)) type = "bonus";

      const milestone = calendarData?.milestones?.find(
        (m) => m.date === dateStr
      );

      days.push({ day: i, type, milestone });
    }

    return days;
  };

  // Rendu d’un mois
  const renderMonth = (year, month) => {
    const days = generateMonthDays(year, month);
    const monthName = new Date(year, month).toLocaleString("default", {
      month: "long",
    });

    return (
      <div className="calendar-block" key={`${year}-${month}`}>
        <h4 style={{ textAlign: "center", marginBottom: "10px" }}>
          {monthName} {year}
        </h4>
        <div className="calendar-grid">
          {days.map((d, idx) => (
            <div key={idx} className={`calendar-day ${d.type || ""}`}>
              <span className="day-number">{d.day}</span>
              {d.milestone && (
                <div className="milestone" title={d.milestone.label}>
                  📍
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    );
  };

  // Rendu selon la vue
  const renderCalendar = () => {
    const baseYear = year;
    const baseMonth = month;

    if (view === "month") {
      return renderMonth(baseYear, baseMonth);
    }

    if (view === "quarter") {
      const startMonth = page * 3;
      return (
        <div className="calendar-multi">
          {Array.from({ length: 3 }, (_, i) =>
            renderMonth(baseYear, startMonth + i)
          )}
        </div>
      );
    }

    if (view === "semester") {
      const startMonth = page * 6;
      return (
        <div className="calendar-multi">
          {Array.from({ length: 6 }, (_, i) =>
            renderMonth(baseYear, startMonth + i)
          )}
        </div>
      );
    }

    if (view === "year") {
      return (
        <div className="calendar-multi">
          {Array.from({ length: 12 }, (_, i) =>
            renderMonth(baseYear, i)
          )}
        </div>
      );
    }

    return null;
  };

  const handlePrev = () => {
    setPage((p) => Math.max(0, p - 1));
  };

  const handleNext = () => {
    const maxPage =
      view === "quarter" ? 3 : view === "semester" ? 1 : view === "year" ? 0 : 0;
    setPage((p) => Math.min(maxPage, p + 1));
  };

  return (
    <div className="checking-admin">
      <div className="checking-form">
        <h2 style={{ color: "var(--accent-cyan)", marginBottom: "20px" }}>
          Student Calendar
        </h2>

        {kind === "admin" && (
          <div className="date-group">
            <label htmlFor="login">Student Login</label>
            <input
              type="text"
              id="login"
              value={login}
              onChange={(e) => setLogin(e.target.value)}
              placeholder="Enter login..."
            />
          </div>
        )}

        <div className="date-group">
          <label htmlFor="view">View</label>
          <select
            id="view"
            value={view}
            onChange={(e) => {
              setView(e.target.value);
              setPage(0);
            }}
          >
            <option value="month">Month</option>
            <option value="quarter">Quarter</option>
            <option value="semester">Semester</option>
            <option value="year">Year</option>
          </select>
        </div>
      </div>

      <div className="results-section">
        {loading ? (
          <p>⏳ Loading calendar...</p>
        ) : (
          <>
            <h3>📅 {view.charAt(0).toUpperCase() + view.slice(1)} View</h3>
            <div className="calendar-pagination">
              <button onClick={handlePrev} disabled={page === 0}>
                ◀ Prev
              </button>
              <span style={{ margin: "0 10px" }}>Page {page + 1}</span>
              <button onClick={handleNext}>Next ▶</button>
            </div>
            {calendarData ? renderCalendar() : <p>No data</p>}
          </>
        )}
      </div>
    </div>
  );
};

export default Calendar;
