import React, { useState } from "react";

const mockUserLocationStats = [
  {
    userName: "alice",
    stats: [
      { date: "2025-09-01", durationStr: "5h 20m" },
      { date: "2025-09-05", durationStr: "6h 10m" },
    ],
  },
  {
    userName: "bob",
    stats: [
      { date: "2025-09-03", durationStr: "4h 45m" },
      { date: "2025-09-08", durationStr: "7h 00m" },
    ],
  },
  {
    userName: "charlie",
    stats: [],
  },
];

const CheckAdmin = () => {
  const [startDate, setStartDate] = useState("2025-09-01");
  const [endDate, setEndDate] = useState("2025-09-15");
  const [searchPerformed, setSearchPerformed] = useState(false);
  const [filteredStats, setFilteredStats] = useState([]);
  const [dayCount, setDayCount] = useState(0);

  const handleSubmit = (e) => {
    e.preventDefault();

    // Conversion en objets Date
    const start = new Date(startDate);
    const end = new Date(endDate);

    // Filtrage des stats par date
    const filtered = mockUserLocationStats.map((user) => {
      const statsInRange = user.stats.filter((s) => {
        const d = new Date(s.date);
        return d >= start && d <= end;
      });
      return { ...user, stats: statsInRange };
    });

    setFilteredStats(filtered);

    // Compter le total de présences
    let totalDays = 0;
    filtered.forEach((u) => (totalDays += u.stats.length));
    setDayCount(totalDays);

    setSearchPerformed(true);
  };

  return (
    <div className="checking-admin">
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

        <button type="submit">Check Attendance</button>
      </form>

      {searchPerformed && (
        <div className="results-section">
          <h3>Verification Results</h3>

          <div className="stats-display">
            <div className="stat-card">
              <div className="stat-number">{dayCount}</div>
              <div className="stat-label">Present Students</div>
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
                  <th>Student Name</th>
                  <th>Date</th>
                  <th>Duration</th>
                </tr>
              </thead>
              <tbody>
                {filteredStats.map((stat, i) => (
                  <tr key={i}>
                    <td>{stat.userName}</td>
                    <td>
                      {stat.stats.length > 0
                        ? stat.stats.map((s, j) => <div key={j}>{s.date}</div>)
                        : "-"}
                    </td>
                    <td>
                      {stat.stats.length > 0
                        ? stat.stats.map((s, j) => <div key={j}>{s.durationStr}</div>)
                        : "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p>No attendance found for the selected period.</p>
          )}
        </div>
      )}
    </div>
  );
};

export default CheckAdmin;
