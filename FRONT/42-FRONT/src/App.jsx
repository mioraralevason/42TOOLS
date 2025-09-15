import React, { useState, useEffect } from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from './components/Header';
import SidebarHover from './components/SidebarHover';
import CertificateForm from './components/CertificateForm';
import FreezeBegin from "./components/FreezeBegin";
import ServerError from "./components/ServerError";
import './index.css';
import API_BASE_URL from "./config";

function App() {
  const [user, setUser] = useState(null);
  const [sidebarVisible, setSidebarVisible] = useState(false);
  const [loading, setLoading] = useState(true);
  const [serverDown, setServerDown] = useState(false);

  useEffect(() => {
    const checkServer = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/api/user`, { credentials: 'include' });
        if (!res.ok || res.status === 401 || res.status === 204) {
          setUser(null);
        } else {
          const data = await res.json();
          setUser(data);
        }
      } catch (err) {
        console.error("Server unreachable:", err);
        setServerDown(true); // server is offline
      } finally {
        setLoading(false);
      }
    };

    checkServer();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (serverDown) return <ServerError />; // only show Oops if server offline
  if (!user) return <div>Redirecting to login...</div>;

  const userKind = user.kind || (user.login && ['admin', 'root', 'supervisor'].some(admin => user.login.toLowerCase().includes(admin)) ? 'admin' : 'student');

  return (
    <BrowserRouter>
      <div className="App">
        <Header user={user} />
        <SidebarHover
          userKind={userKind}
          sidebarVisible={sidebarVisible}
          setSidebarVisible={setSidebarVisible}
        />
        <main style={{ marginLeft: sidebarVisible ? '250px' : '0px', padding: '20px', transition: 'margin-left 0.3s' }}>
          <Routes>
            <Route path="/certificate" element={<CertificateForm user={user} kind={userKind} />} />
            <Route path="/freeze-begin" element={<FreezeBegin />} />
            <Route path="/check" element={<div>Admin Check Page (Placeholder)</div>} />
            <Route path="/checkUser" element={<div>User Check Page (Placeholder)</div>} />
            <Route path="/events" element={<div>Events Page (Placeholder)</div>} />
            <Route path="/" element={<CertificateForm user={user} kind={userKind} />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
