// src/components/Header.jsx
import React from "react";
import "../styles/header.css";

const Header = ({ user }) => {
  return (
    <div className="profile-card">
      <div className="profile-info">
        <img
          src={user?.image?.link || "/images/default-profile.png"}
          alt={`${user?.first_name} ${user?.last_name}`}
          className="profile-pic"
        />
        <div className="name-status">
          <h1>{user ? `${user.first_name} ${user.last_name}` : "Nom Inconnu"}</h1>
          <span className="username">{user?.login}</span>
          <p>
            <i className="fa fa-envelope contact-icon" aria-hidden="true"></i>
            <span>{user?.email}</span>
          </p>
        </div>
      </div>

      <div className="logo-section">
        <img src="/images/logo-42.png" alt="Logo" className="logo-img" />
      </div>
    </div>
  );
};

export default Header;
