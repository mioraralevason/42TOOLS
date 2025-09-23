import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import ServerError from "./ServerError";
import API_BASE_URL from "../config";

// Custom error class for consistent error handling
class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

function Login() {
  const [loading, setLoading] = useState(true);
  const [serverDown, setServerDown] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const navigate = useNavigate();
  const location = useLocation();

  const fetchWithRetry = async (url, options, retries = 3, delay = 1000) => {
    for (let i = 0; i < retries; i++) {
      try {
        const res = await fetch(url, { ...options, credentials: "include" });
        if (!res.ok) {
          if (res.status === 429) {
            await new Promise(resolve => setTimeout(resolve, delay * (i + 1)));
            continue;
          }
          const errorData = await res.json().catch(() => ({}));
          throw new ResponseStatusException(res.status, errorData.error || `HTTP error! status: ${res.status}`);
        }
        return res;
      } catch (err) {
        if (i === retries - 1) throw err;
        await new Promise(resolve => setTimeout(resolve, delay * (i + 1)));
      }
    }
  };

  useEffect(() => {
    const checkServerAndAuth = async () => {
      try {
        // Step 1: Ping the server
        await fetchWithRetry(`${API_BASE_URL}/api/ping`, {
          method: "GET",
        });

        // Step 2: Check for login_success or error query parameters
        const params = new URLSearchParams(location.search);
        if (params.get("login_success") === "true") {
          navigate("/app");
        } else if (params.get("error")) {
          setErrorMessage(`Authentication failed: ${params.get("error")}`);
          setServerDown(true);
        } else {
          window.location.href = `${API_BASE_URL}/`;
        }
      } catch (err) {
        console.error("Server error:", err.message);
        setErrorMessage(err.message || "Failed to connect to the server");
        setServerDown(true);
      } finally {
        setLoading(false);
      }
    };

    checkServerAndAuth();
  }, [navigate, location.search]);

  if (loading) return (
    <div className="loading">
      <div className="spinner"></div>
      <span>Loading...</span>
    </div>
  );
  if (serverDown) return <ServerError message={errorMessage} />;

  return <div>Redirecting...</div>;
}

export default Login;