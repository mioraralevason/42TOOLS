import React, { useState } from "react";
import API_BASE_URL from "../config";

const ServerError = () => {
  const [checking, setChecking] = useState(false);

  const handleReturnHome = async () => {
    setChecking(true);
    try {
      // Teste le serveur via /health
      const res = await fetch(`${API_BASE_URL}/health`);
      if (res.ok) {
        // Si le serveur répond, redirige vers la base
        window.location.href = `${API_BASE_URL}/`;
      } else {
        alert("Le serveur est toujours indisponible.");
      }
    } catch (err) {
      console.error("Server still unreachable:", err);
      alert("Le serveur est toujours indisponible.");
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className="server-error-container">
      <div className="face">
        <div className="band">
          <div className="red"></div>
          <div className="white"></div>
          <div className="blue"></div>
        </div>
        <div className="eyes"></div>
        <div className="dimples"></div>
        <div className="mouth"></div>
      </div>

      <h1>Oops! Something went wrong!</h1>
      <div className="btn" onClick={handleReturnHome}>
        {checking ? "Checking server..." : "Return to Home"}
      </div>
    </div>
  );
};

export default ServerError;
