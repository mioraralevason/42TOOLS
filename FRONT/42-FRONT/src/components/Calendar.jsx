import React, { useState } from "react";
import '../index.css';



const Calendar = () => {
  const [view, setView] = useState("month"); // "month" | "quarter" | "semester" | "year"

  // Données statiques d’exemple
  const events = {
    presence: ["2025-09-05", "2025-09-06", "2025-09-09", "2025-09-15"],
    freeze: ["2025-09-10", "2025-09-11"],
    bonus: ["2025-09-18", "2025-09-19"],
    milestones: [
      { date: "2025-09-20", label: "Milestone Piscine" },
      { date: "2025-09-25", label: "Deadline Projet Cursus" },
    ],
  };

  // Génère les jours du mois en cours
  const generateMonthDays = () => {
    const today = new Date("2025-09-01"); // statique pour l’exemple
    const year = today.getFullYear();
    const month = today.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const days = [];

    for (let i = 1; i <= lastDay.getDate(); i++) {
      const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(
        i
      ).padStart(2, "0")}`;

      let type = null;
      if (events.presence.includes(dateStr)) type = "presence";
      if (events.freeze.includes(dateStr)) type = "freeze";
      if (events.bonus.includes(dateStr)) type = "bonus";

      const milestone = events.milestones.find((m) => m.date === dateStr);

      days.push({ day: i, type, milestone });
    }

    return days;
  };

  const renderCalendar = () => {
    const days = generateMonthDays();

    return (
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
    );
  };

  return (
    <div className="checking-admin">
      <div className="checking-form">
        <h2 style={{ color: "var(--accent-cyan)", marginBottom: "20px" }}>
          Student Calendar
        </h2>
        <div className="date-group">
          <label htmlFor="view">View</label>
          <select
            id="view"
            value={view}
            onChange={(e) => setView(e.target.value)}
          >
            <option value="month">Month</option>
            <option value="quarter">Quarter</option>
            <option value="semester">Semester</option>
            <option value="year">Year</option>
          </select>
        </div>
      </div>

      <div className="results-section">
        <h3>📅 {view.charAt(0).toUpperCase() + view.slice(1)} View</h3>
        {renderCalendar()}
      </div>
    </div>
  );
};

export default Calendar;
