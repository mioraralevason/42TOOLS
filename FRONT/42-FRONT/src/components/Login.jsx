import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import ServerError from "./ServerError";
import API_BASE_URL from "../config";

function Login() {
  const [loading, setLoading] = useState(true);
  const [serverDown, setServerDown] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const checkServerAndAuth = async () => {
      try {
        // Step 1: Ping the server
        const pingResponse = await fetch(`${API_BASE_URL}/api/ping`, {
          method: "GET",
          credentials: "include",
        });

        if (!pingResponse.ok) {
          console.error("Ping failed with status:", pingResponse.status);
          setServerDown(true);
          return;
        }

        // Step 2: Check for login_success or error query parameters
        const params = new URLSearchParams(location.search);
        if (params.get("login_success") === "true") {
          // Authentication successful, navigate to App
          navigate("/app");
        } else if (params.get("error")) {
          // Handle OAuth errors (e.g., token_failed, user_failed)
          console.error("Authentication error:", params.get("error"));
          setServerDown(true); // Or display a specific error message
        } else {
          // No login_success or error, initiate OAuth flow
          window.location.href = `${API_BASE_URL}/`;
        }
      } catch (err) {
        console.error("Server unreachable:", err);
        setServerDown(true);
      } finally {
        setLoading(false);
      }
    };

    checkServerAndAuth();
  }, [navigate, location.search]);

  if (loading) return <div>Loading...</div>;
  if (serverDown) return <ServerError />;

  return <div>Redirecting...</div>;
}

export default Login;