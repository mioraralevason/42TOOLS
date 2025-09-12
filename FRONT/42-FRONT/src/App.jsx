import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import SidebarHover from './components/SidebarHover';
import CertificateForm from './components/CertificateForm';
import FreezeBegin from "./components/FreezeBegin";
import './styles/global.css';

const API_BASE_URL = 'http://localhost:9090';

function App() {
    const [user, setUser] = useState(null);
    const [sidebarVisible, setSidebarVisible] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch(`${API_BASE_URL}/api/user`, {
            method: 'GET',
            credentials: 'include',
        })
            .then(async (res) => {
                if (res.status === 401 || res.status === 204 || !res.ok) {
                    window.location.href = `${API_BASE_URL}/`;
                } else {
                    const data = await res.json();
                    setUser(data);
                }
            })
            .catch((err) => {
                console.error('Erreur API:', err);
                window.location.href = `${API_BASE_URL}/`;
            })
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <div>Chargement...</div>;
    if (!user) return <div>Redirection vers login...</div>;

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
                <main
                    style={{
                        marginLeft: sidebarVisible ? '250px' : '0px',
                        padding: '20px',
                        transition: 'margin-left 0.3s',
                    }}
                >
                    <Routes>
                        <Route
                            path="/certificate"
                            element={<CertificateForm user={user} kind={userKind} />}
                        />
                        <Route
                            path="/freeze-begin"
                            element={
                                <FreezeBegin
                                userCursus={{ formattedBeginAt: "2025-01-01", milestone: 4 }}
                                locationStats={{ nbDays: 120, nbOpenDays: 80, totalHours: 600 }}
                                freeze={5}
                                listLogin={["miora42", "admin42"]}
                                login="miora42"
                            />
                            }
                        />
                        <Route path="/check" element={<div>Page Vérification Admin (Placeholder)</div>} />
                        <Route path="/checkUser" element={<div>Page Vérification Utilisateur (Placeholder)</div>} />
                        <Route path="/events" element={<div>Page Événements (Placeholder)</div>} />
                        <Route path="/" element={<div>Accueil (Placeholder)</div>} />
                    </Routes>
                </main>
            </div>
        </BrowserRouter>
    );
}

export default App;