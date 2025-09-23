import React, { useState } from 'react';
import API_BASE_URL from "../config";
import ErrorPopup from "./ErrorPopup";
import '../index.css';

// Custom error class for consistent error handling
class ResponseStatusException extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

const CertificateForm = ({ user, kind, users }) => {
  const [login, setLogin] = useState(kind === 'admin' ? '' : user.login);
  const [sousignerPar, setSousignerPar] = useState('none');
  const [signer, setSigner] = useState(false);
  const [lang, setLang] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [suggestions, setSuggestions] = useState([]);

  const handleLoginChange = (e) => {
    const value = e.target.value;
    setLogin(value);

    if (kind === 'admin' && value.length > 0) {
      const filteredSuggestions = users
        .map((u) => u.login)
        .filter((login) => login.toLowerCase().includes(value.toLowerCase()))
        .sort((a, b) => a.toLowerCase().localeCompare(b.toLowerCase()))
        .slice(0, 5);
      setSuggestions(filteredSuggestions);
    } else {
      setSuggestions([]);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    setLogin(suggestion);
    setSuggestions([]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/certificate-generator?login=${encodeURIComponent(login)}&sousigner_par=${encodeURIComponent(sousignerPar)}&signer=${signer}&lang=${encodeURIComponent(lang)}`,
        {
          method: 'GET',
          credentials: 'include',
        }
      );

      if (!response.ok) {
        if (response.status === 401) {
          setError('Authentication required. Redirecting to login...');
          setTimeout(() => {
            window.location.href = `${API_BASE_URL}/login`;
          }, 2000);
          return;
        }
        const errorData = await response.json().catch(() => ({}));
        throw new ResponseStatusException(response.status, errorData.error || `HTTP error! status: ${response.status}`);
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `school_certificate_${login}_${lang}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error:', error.message);
      setError(error.message || 'Failed to generate certificate. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="contact-card">
      <ErrorPopup error={error} setError={setError} />
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
              placeholder="Enter login"
              required
              autoComplete="off"
            />
            {suggestions.length > 0 && (
              <>
                <ul className="suggestions-list">
                  {suggestions.map((suggestion, index) => (
                    <li
                      key={index}
                      onClick={() => handleSuggestionClick(suggestion)}
                    >
                      {suggestion}
                    </li>
                  ))}
                </ul>
              </>
            )}
          </div>
        ) : (
          <input type="hidden" name="login" value={user.login} />
        )}

        <div className="sel sel--black-panther">
          <label htmlFor="sousigner_par">Signed by</label>
          <select
            name="sousigner_par"
            id="sousigner_par"
            value={sousignerPar}
            onChange={(e) => setSousignerPar(e.target.value)}
            required
          >
            <option value="none">None</option>
            <option value="DG">General Director</option>
            <option value="DP">Educational Director</option>
            <option value="AP">Educational Assistant</option>
          </select>
        </div>

        <div className="sel sel--black-panther">
          <label htmlFor="lang">Language</label>
          <select
            name="lang"
            id="lang"
            value={lang}
            onChange={(e) => setLang(e.target.value)}
            required
          >
            <option value="" disabled>
              -- Select Language --
            </option>
            <option value="fr">French</option>
            <option value="en">English</option>
          </select>
        </div>

        <div className="checkbox-container">
          <label htmlFor="signer">Signed</label>
          <input
            type="checkbox"
            id="signer"
            name="signer"
            checked={signer}
            onChange={(e) => setSigner(e.target.checked)}
          />
        </div>

        <button type="submit" className="codepen-button" disabled={loading}>
          <span>{loading ? 'Generating...' : 'Generate Certificate'}</span>
        </button>

        {loading && (
          <div id="loader" style={{ marginTop: '20px', textAlign: 'center' }}>
            <div className="spinner"></div>
            <div style={{ color: '#00ffcc', marginTop: '5px' }}>Generating...</div>
          </div>
        )}
      </form>
    </div>
  );
};

export default CertificateForm;