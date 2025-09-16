import React, { useState } from "react";
// <-- utilise le CSS que tu as demandé

// Données statiques (simulation d’un backend)
const mockUserLocationStats = [
  {
    userId: "alice",
    stats: [
      { date: "2025-09-01", durationStr: "5h 20m" },
      { date: "2025-09-05", durationStr: "6h 10m" },
    ],
  },
  {
    userId: "bob",
    stats: [
      { date: "2025-09-03", durationStr: "4h 45m" },
      { date: "2025-09-08", durationStr: "7h 00m" },
    ],
  },
  {
    userId: "charlie",
    stats: [],
  },
];

const CheckUser = () => {
  const [login, setLogin] = useState("");
  const [startDate, setStartDate] = useState("2025-09-01");
  const [endDate, setEndDate] = useState("2025-09-15");
  const [searchPerformed, setSearchPerformed] = useState(false);
  const [filteredStats, setFilteredStats] = useState([]);
  const [dayCount, setDayCount] = useState(0);
  const [hourCount, setHourCount] = useState(0);

  const handleSubmit = (e) => {
    e.preventDefault();

    const start = new Date(startDate);
    const end = new Date(endDate);

    // Filtrage des données par login + date
    const filtered = mockUserLocationStats
      .filter((user) => login === "" || user.userId === login)
      .map((user) => {
        const statsInRange = user.stats.filter((s) => {
          const d = new Date(s.date);
          return d >= start && d <= end;
        });
        return { ...user, stats: statsInRange };
      });

    setFilteredStats(filtered);

    // Calcul des jours et heures totales
    let totalDays = 0;
    let totalHours = 0;
    filtered.forEach((u) => {
      totalDays += u.stats.length;
      u.stats.forEach((s) => {
        if (s.durationStr) {
          const parts = s.durationStr.split(" ");
          let h = 0,
            m = 0;
          parts.forEach((p) => {
            if (p.includes("h")) h = parseInt(p.replace("h", ""));
            if (p.includes("m")) m = parseInt(p.replace("m", ""));
          });
          totalHours += h + m / 60;
        }
      });
    });

    setDayCount(totalDays);
    setHourCount(totalHours.toFixed(2));
    setSearchPerformed(true);
  };

  return (
    <div className="checking-admin">
      {/* ===== Formulaire ===== */}
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

      {/* ===== Résultats ===== */}
      {searchPerformed && (
        <div className="results-section">
          <h3>
            <i className="fa fa-bar-chart"></i> Verification Results
          </h3>

          <div className="stats-display">
            <div className="stat-card">
              <div className="stat-number">{dayCount}</div>
              <div className="stat-label">Day(s) of Attendance</div>
            </div>
            <div className="stat-card">
              <div className="stat-number">{hourCount}</div>
              <div className="stat-label">Hour(s) of Attendance</div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                {startDate} → {endDate}
              </div>
              <div className="stat-label">Date Range</div>
            </div>
          </div>

          {filteredStats.some((u) => u.stats.length > 0) ? (
            <table className="results-table">
              <thead>
                <tr>
                  <th>
                    <i className="fa fa-user"></i> Student ID
                  </th>
                  <th>
                    <i className="fa fa-calendar"></i> Date
                  </th>
                  <th>
                    <i className="fa fa-clock-o"></i> Duration
                  </th>
                </tr>
              </thead>
              <tbody>
                {filteredStats.map((stat, i) => (
                  <tr key={i}>
                    <td>{stat.userId}</td>
                    <td>
                      {stat.stats.length > 0
                        ? stat.stats.map((s, j) => <div key={j}>{s.date}</div>)
                        : "-"}
                    </td>
                    <td>
                      {stat.stats.length > 0
                        ? stat.stats.map((s, j) => (
                            <div key={j}>{s.durationStr}</div>
                          ))
                        : "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p>No attendance records found for the selected period.</p>
          )}
        </div>
      )}
    </div>
  );
};

export default CheckUser;
