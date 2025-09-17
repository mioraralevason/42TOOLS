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
import { BrowserRouter } from "react-router-dom";

function App() {
  const [user, setUser] = useState(null);
  const [users, setUsers] = useState([]); // State to store all users
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [serverDown, setServerDown] = useState(false);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/api/user`, {
          credentials: "include",
        });
        if (!res.ok || res.status === 401 || res.status === 204) {
          setUser(null);
          window.location.href = `${API_BASE_URL}/login`; // Redirect to OAuth login
        } else {
          const data = await res.json();
          setUser(data);
        }
      } catch (err) {
        console.error("Server unreachable:", err);
        setServerDown(true);
      } finally {
        setLoading(false);
      }
    };

    const fetchAllUsers = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/users`, {
          credentials: "include",
        });
        if (!res.ok) {
          if (res.status === 403) {
            console.warn("Access denied: Only admins can fetch all users");
            setUsers([]); // Set empty array if not authorized
          } else {
            throw new Error(`HTTP error! status: ${res.status}`);
          }
        } else {
          const data = await res.json();
          setUsers(data);
        }
      } catch (err) {
        console.error("Error fetching all users:", err);
        setUsers([]); // Set empty array on error
      }
    };

    fetchUser();
  }, []);

  // Fetch users only if user is an admin
  useEffect(() => {
    if (user && (user.kind?.toLowerCase() === 'admin' || ['admin', 'root', 'supervisor'].some(admin => user.login?.toLowerCase().includes(admin)))) {
      const fetchAllUsers = async () => {
        try {
          const res = await fetch(`${API_BASE_URL}/users`, {
            credentials: "include",
          });
          if (!res.ok) {
            if (res.status === 403) {
              console.warn("Access denied: Only admins can fetch all users");
              setUsers([]);
            } else {
              throw new Error(`HTTP error! status: ${res.status}`);
            }
          } else {
            const data = await res.json();
            setUsers(data);
          }
        } catch (err) {
          console.error("Error fetching all users:", err);
          setUsers([]);
        }
      };
      fetchAllUsers();
    }
  }, [user]);

  if (loading) return <div>Loading...</div>;
  if (serverDown) return <ServerError />;
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
          <Route path="/check" element={<Check />} />
          <Route path="/events" element={<div>Events Page (Placeholder)</div>} />
          <Route path="/" element={<CertificateForm user={user} kind={userKind} users={userKind === 'admin' ? users : []} />} />
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