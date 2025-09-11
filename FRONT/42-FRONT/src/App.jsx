// src/App.jsx
import React from "react";
import Header from "./components/Header";
import SidebarHover from "./components/SidebarHover";
import "./styles/global.css";

function App() {
  const [sidebarVisible, setSidebarVisible] = React.useState(false);

  const user = {
    first_name: "Miora",
    last_name: "Ralevason",
    login: "miora42",
    email: "miora@example.com",
    image: { link: "/images/profil.jpeg" },
    kind: "admin",
  };

  return (
    <div className="App">
      <Header user={user} />
      <SidebarHover 
        userKind={user.kind} 
        sidebarVisible={sidebarVisible} 
        setSidebarVisible={setSidebarVisible} 
      />
      <main
        style={{
          marginLeft: sidebarVisible ? "250px" : "0px",
          padding: "20px",
          transition: "margin-left 0.3s",
        }}
      >
        <h1>Bienvenue, {user.first_name} !</h1>
        <p>Contenu principal ici...</p>
      </main>
    </div>
  );
}

export default App;
