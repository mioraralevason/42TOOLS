import React from "react";
import "../styles/server-error.css";
import API_BASE_URL from "../config";


const ServerError = () => {
  const handleReturnHome = () => {
    window.location.href = `${API_BASE_URL}/`;
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
      <div className="btn" onClick={handleReturnHome}>Return to Home</div>
    </div>
  );
};

export default ServerError;
