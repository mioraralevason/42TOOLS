import React, { useState, useEffect } from "react";
import { Routes, Route } from "react-router-dom";
import Header from './components/Header';
import SidebarHover from './components/SidebarHover';
import CertificateForm from './components/CertificateForm';
import FreezeBegin from "./components/FreezeBegin";
import ServerError from "./components/ServerError";
import Login from "./components/Login";
import './index.css';
import API_BASE_URL from "./config";
import Check from "./components/Check";
import Calendar from "./components/Calendar"; 
import { BrowserRouter } from "react-router-dom";

// Custom error class for consistent error handling
class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

function App() {
  const [user, setUser] = useState(null);
  const [users, setUsers] = useState([]);
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [serverDown, setServerDown] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const fetchWithRetry = async (url, options, retries = 3, delay = 1000) => {
    for (let i = 0; i < retries; i++) {
      try {
        const res = await fetch(url, { ...options, credentials: "include" });
        if (!res.ok) {
          if (res.status === 429) {
            await new Promise(resolve => setTimeout(resolve, delay * (i + 1)));
            continue;
          }
          if (res.status === 401) {
            setUser(null);
            window.location.href = `${API_BASE_URL}/login`;
            return null;
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
    const fetchUser = async () => {
      try {
        const res = await fetchWithRetry(`${API_BASE_URL}/api/user`, {
          method: "GET",
        });
        if (res) {
          const data = await res.json();
          setUser(data);
        }
      } catch (err) {
        console.error("Server error:", err.message);
        setErrorMessage(err.message || "Failed to connect to the server");
        setServerDown(true);
      } finally {
        setLoading(false);
      }
    };

    const fetchAllUsers = async () => {
      try {
        const res = await fetchWithRetry(`${API_BASE_URL}/users`, {
          method: "GET",
        });
        if (res) {
          const data = await res.json();
          setUsers(data);
        }
      } catch (err) {
        console.error("Error fetching all users:", err.message);
        if (err.status !== 403) {
          setErrorMessage(err.message || "Failed to fetch users");
        }
        setUsers([]);
      }
    };

    fetchUser();
    fetchAllUsers();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (serverDown) return <ServerError message={errorMessage} />;
  if (!user) return <div>Redirecting to login...</div>;

  const userKind = user.kind || (user.login && ['admin', 'root', 'supervisor'].some(admin => user.login.toLowerCase().includes(admin)) ? 'admin' : 'student');

  return (
    <div className="App">
      <Header user={user} />
      <SidebarHover
        userKind={userKind}
        sidebarVisible={sidebarVisible}
        setSidebarVisible={setSidebarVisible}
      />
      <main style={{ marginLeft: sidebarVisible ? '250px' : '0px', padding: '20px', transition: 'margin-left 0.3s' }}>
        <Routes>
          <Route path="/certificate" element={<CertificateForm user={user} kind={userKind} users={userKind === 'admin' ? users : []} />} />
          <Route path="/freeze-begin" element={<FreezeBegin user={user} kind={userKind} users={userKind === 'admin' ? users : []} />} />
          <Route path="/check" element={<Check user={user} kind={userKind} />} />
          <Route path="/events" element={<div>Events Page (Placeholder)</div>} />
          <Route path="/" element={<CertificateForm user={user} kind={userKind} users={userKind === 'admin' ? users : []} />} />
          <Route path="/calendar" element={<Calendar userResponse={user} kind={userKind} />} />
        </Routes>
      </main>
    </div>
  );
}

function Root() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/app/*" element={<App />} />
      </Routes>
    </BrowserRouter>
  );
}

export default Root;
