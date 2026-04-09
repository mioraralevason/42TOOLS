# 42TOOLS Frontend - Angular Application

Application Angular pour la gestion des entités, utilisateurs et leurs associations.

## Structure du projet

```
frontend/
├── src/
│   ├── app/
│   │   ├── models/              # Interfaces TypeScript
│   │   │   ├── entity.model.ts
│   │   │   ├── user.model.ts
│   │   │   └── user-entity.model.ts
│   │   ├── services/            # Services pour les appels API
│   │   │   ├── entity.service.ts
│   │   │   ├── user.service.ts
│   │   │   └── user-entity.service.ts
│   │   ├── components/          # Components Angular
│   │   │   ├── navbar/
│   │   │   ├── entity-list/
│   │   │   ├── entity-create/
│   │   │   ├── entity-edit/
│   │   │   ├── entity-detail/
│   │   │   ├── user-list/
│   │   │   ├── user-create/
│   │   │   ├── user-edit/
│   │   │   ├── user-detail/
│   │   │   ├── user-entity-list/
│   │   │   ├── user-entity-create/
│   │   │   └── user-entity-edit/
│   │   ├── app.routes.ts        # Configuration des routes
│   │   ├── app.config.ts        # Configuration de l'application
│   │   └── app.ts               # Component principal
│   └── styles.css               # Styles globaux
```

## Fonctionnalités

### 1. CRUD pour les Entités
- **Lister** toutes les entités : `GET /entities`
- **Créer** une entité : `POST /entities`
- **Voir** une entité : `GET /entities/:id`
- **Modifier** une entité : `PUT /entities/:id`
- **Supprimer** une entité : `DELETE /entities/:id`

### 2. CRUD pour les Utilisateurs
- **Lister** tous les utilisateurs : `GET /users`
- **Créer** un utilisateur : `POST /users`
- **Voir** un utilisateur : `GET /users/:id`
- **Modifier** un utilisateur : `PUT /users/:id`
- **Supprimer** un utilisateur : `DELETE /users/:id`

### 3. CRUD pour les Associations User-Entity
- **Lister** toutes les associations : `GET /user-entities`
- **Créer** une association : `POST /user-entities`
- **Modifier** une association : `PUT /user-entities/:id`
- **Supprimer** une association : `DELETE /user-entities/:id`

## Installation et démarrage

### Prérequis
- Node.js (v18+)
- npm ou yarn
- Backend 42TOOLS démarré sur `http://localhost:5000`

### Commands

```bash
# Installer les dépendances
npm install

# Démarrer en mode développement
ng serve

# Construire pour la production
ng build

# Démarrer le serveur de développement
npm start
```

L'application sera accessible sur `http://localhost:4200`

## Configuration

Le fichier `app.config.ts` configure :
- Le routing avec `provideRouter`
- HttpClient avec `provideHttpClient`
- Le SSR avec `provideClientHydration`

## Architecture

### Services
Chaque service utilise `HttpClient` pour communiquer avec le backend :
- `EntityService` : Gestion des entités
- `UserService` : Gestion des utilisateurs
- `UserEntityService` : Gestion des associations

### Components
Tous les components sont **standalone** (Angular 14+) et utilisent :
- `CommonModule` pour les directives communes
- `RouterModule` pour la navigation
- `FormsModule` pour les formulaires

### Routing
Le routing est configuré dans `app.routes.ts` avec :
- Route par défaut vers `/entities`
- Routes CRUD pour chaque entité
- Gestion des routes inconnues

## Notes

- L'URL du backend est configurée dans chaque service (`http://localhost:5000`)
- Les components utilisent des formulaires template-driven avec `ngModel`
- La navigation se fait via `RouterModule` et `routerLink`
