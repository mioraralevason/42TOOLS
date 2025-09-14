import React, { useState } from 'react';
import '../styles/certificate-form.css';
import API_BASE_URL from "../config";

const CertificateForm = ({ user, kind }) => {
    const [login, setLogin] = useState(kind === 'admin' ? '' : user.login);
    const [signerPar, setSignerPar] = useState('aucune');
    const [lang, setLang] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

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
                    <input
                        type="text"
                        className="admin-input"
                        name="login"
                        value={login}
                        onChange={(e) => setLogin(e.target.value)}
                        placeholder="Entrez le login"
                        required
                    />
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
