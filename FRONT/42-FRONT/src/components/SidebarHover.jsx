import React from "react";
import "../styles/sidebar.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { 
  faHome, 
  faSnowflake, 
  faCheckSquare, 
  faCaretDown, 
  faCalendar, 
  faUser, 
  faShieldAlt 
} from "@fortawesome/free-solid-svg-icons";

const SidebarHover = ({ userKind, sidebarVisible, setSidebarVisible }) => {
  return (
    <>
      {/* Button toggle mobile */}
      <button 
        className="sidebar-toggle-btn" 
        onClick={() => setSidebarVisible(!sidebarVisible)}
      >
        ☰
      </button>

      <aside className={`sidebar ${sidebarVisible ? "visible" : ""}`}>
        <div className="sidebar-logo">
          <img src="/images/logo.png" alt="Logo" />
        </div>

        <ul>
          <li>
            <a href="/app/certificate">
              <FontAwesomeIcon icon={faHome} />
              <span>Certificat Scolarite</span>
            </a>
          </li>
          <li>
            <a href="/app/freeze-begin">
              <FontAwesomeIcon icon={faSnowflake} />
              <span>Freeze</span>
            </a>
          </li>
          <li className="has-submenu">
            <a href="/app/check">
              <FontAwesomeIcon icon={faCheckSquare} />
              <span>Checking</span>
            </a>
          </li>
        </ul>
      </aside>
    </>
  );
};

export default SidebarHover;
