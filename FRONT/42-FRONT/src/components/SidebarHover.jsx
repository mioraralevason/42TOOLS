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
      {/* Zone de survol invisible */}
      <div
        className="hover-area"
        onMouseEnter={() => setSidebarVisible(true)}
      ></div>

      <aside
        className={`sidebar ${sidebarVisible ? "visible" : ""}`}
        onMouseLeave={() => setSidebarVisible(false)}
      >
        {/* Logo tout en haut */}
        <div className="sidebar-logo">
          <img src="/images/logo.png" alt="Logo" />
        </div>

        <ul>
          <li>
            <a href="/certificate">
              <FontAwesomeIcon icon={faHome} /> Certificat Scolarite
            </a>
          </li>
          <li>
            <a href="/freeze-begin">
              <FontAwesomeIcon icon={faSnowflake} /> Freeze
            </a>
          </li>
          <li className="has-submenu">
            <a href="#">
              <FontAwesomeIcon icon={faCheckSquare} /> Checking{" "}
              <FontAwesomeIcon icon={faCaretDown} />
            </a>
            <ul className="submenu">
              {userKind === "admin" && (
                <li>
                  <a href="/check">
                    <FontAwesomeIcon icon={faShieldAlt} /> Admin
                  </a>
                </li>
              )}
              <li>
                <a href="/checkUser">
                  <FontAwesomeIcon icon={faUser} /> User
                </a>
              </li>
            </ul>
          </li>
          {userKind === "admin" && (
            <li>
              <a href="/events">
                <FontAwesomeIcon icon={faCalendar} /> Events
              </a>
            </li>
          )}
        </ul>
      </aside>
    </>
  );
};

export default SidebarHover;
