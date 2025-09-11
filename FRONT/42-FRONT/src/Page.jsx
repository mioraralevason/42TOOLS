import React from "react";

const Page = ({ user }) => {
  return (
    <div className="page-content">
      <h1>Bienvenue, {user.first_name} {user.last_name} !</h1>
      <p>Votre login : {user.login}</p>
      <p>Email : {user.email}</p>
      <p>Type d'utilisateur : {user.kind}</p>

      <section style={{ marginTop: "30px" }}>
        <h2>Section principale</h2>
        <p>
          Ici vous pouvez afficher le contenu principal de votre application React. 
          Par exemple, des tableaux, des formulaires, ou des composants dynamiques.
        </p>
      </section>

      <section style={{ marginTop: "30px" }}>
        <h2>Actions rapides</h2>
        <ul>
          <li>Voir vos certificats</li>
          <li>Accéder au freeze</li>
          <li>Gérer le checking {user.kind === "admin" && "(admin)"}</li>
          {user.kind === "admin" && <li>Accéder aux événements</li>}
        </ul>
      </section>
    </div>
  );
};

export default Page;
