import React, { useState } from 'react';
import API_BASE_URL from "../config";
import '../index.css';

const CertificateForm = ({ user, kind, users }) => {
    const [login, setLogin] = useState(kind === 'admin' ? '' : user.login);
    const [signerPar, setSignerPar] = useState('aucune');
    const [lang, setLang] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);

    const handleLoginChange = (e) => {
        const value = e.target.value;
        setLogin(value);

        if (kind === 'admin' && value.length > 0) {
            const filteredSuggestions = users
                .filter(u => u.login?.toLowerCase().includes(value.toLowerCase()))
                .map(u => u.login)
                .slice(0, 5); // Limit to 5 suggestions
            setSuggestions(filteredSuggestions);
            setShowSuggestions(true);
        } else {
            setSuggestions([]);
            setShowSuggestions(false);
        }
    };

    const handleSuggestionClick = (suggestion) => {
        setLogin(suggestion);
        setSuggestions([]);
        setShowSuggestions(false);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(
                `${API_BASE_URL}/certificate-generator?login=${encodeURIComponent(login)}&signer_par=${encodeURIComponent(signerPar)}&lang=${encodeURIComponent(lang)}`,
                {
                    method: 'GET',
                    credentials: 'include',
                }
            );

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Erreur lors de la génération du certificat');
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `certificat_scolaire_${login}_${lang}.pdf`;
            a.click();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Erreur:', error.message);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="contact-card">
            {error && (
                <div className="popup-error">
                    <div className="popup-content">
                        <p>{error}</p>
                        <span className="close">&times;</span>
                    </div>
                </div>
            )}
            <form method="get" onSubmit={handleSubmit}>
                {kind === 'admin' ? (
                    <div className="filter-box">
                        <label htmlFor="login">Login</label>
                        <input
                            type="text"
                            className="admin-input"
                            id="login"
                            name="login"
                            value={login}
                            onChange={handleLoginChange}
                            onBlur={() => setTimeout(() => setShowSuggestions(false), 200)}
                            placeholder="Entrez le login"
                            required
                            autoComplete="off"
                        />
                        {showSuggestions && suggestions.length > 0 && (
                            <ul className="suggestions-list">
                                {suggestions.map((suggestion, index) => (
                                    <li
                                        key={index}
                                        onClick={() => handleSuggestionClick(suggestion)}
                                        onMouseDown={(e) => e.preventDefault()} // Prevent blur on click
                                    >
                                        {suggestion}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                ) : (
                    <input type="hidden" name="login" value={user.login} />
                )}

                <div className="sel sel--black-panther">
                    <select
                        name="signer_par"
                        id="signer_par"
                        value={signerPar}
                        onChange={(e) => setSignerPar(e.target.value)}
                        required
                    >
                        <option value="aucune">Aucune</option>
                        <option value="directeur">Directeur</option>
                        <option value="assistant">Assistant</option>
                    </select>
                </div>

                <div className="sel sel--black-panther">
                    <select
                        name="lang"
                        id="lang"
                        value={lang}
                        onChange={(e) => setLang(e.target.value)}
                        required
                    >
                        <option value="" disabled>
                            -- Choisir la langue --
                        </option>
                        <option value="fr">Français</option>
                        <option value="en">Anglais</option>
                    </select>
                </div>

                <button type="submit" className="codepen-button" disabled={loading}>
                    <span>{loading ? 'Génération...' : 'Générer le Certificat'}</span>
                </button>

                {loading && (
                    <div id="loader" style={{ marginTop: '20px', textAlign: 'center' }}>
                        <div className="spinner"></div>
                        <div style={{ color: '#00ffcc', marginTop: '5px' }}>Génération en cours...</div>
                    </div>
                )}
            </form>
        </div>
    );
};

export default CertificateForm;