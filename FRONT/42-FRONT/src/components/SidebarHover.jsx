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
            <a href="/certificate">
              <FontAwesomeIcon icon={faHome} />
              <span>Certificat Scolarite</span>
            </a>
          </li>
          <li>
            <a href="/freeze-begin">
              <FontAwesomeIcon icon={faSnowflake} />
              <span>Freeze</span>
            </a>
          </li>
          <li className="has-submenu">
            <a href="#">
              <FontAwesomeIcon icon={faCheckSquare} />
              <span>Checking</span>
              <FontAwesomeIcon icon={faCaretDown} />
            </a>
            <ul className="submenu">
              {userKind === "admin" && (
                <li>
                  <a href="/check">
                    <FontAwesomeIcon icon={faShieldAlt} />
                    <span>Admin</span>
                  </a>
                </li>
              )}
              <li>
                <a href="/checkUser">
                  <FontAwesomeIcon icon={faUser} />
                  <span>User</span>
                </a>
              </li>
            </ul>
          </li>
          {userKind === "admin" && (
            <li>
              <a href="/events">
                <FontAwesomeIcon icon={faCalendar} />
                <span>Events</span>
              </a>
            </li>
          )}
        </ul>
      </aside>
    </>
  );
};

export default SidebarHover;
