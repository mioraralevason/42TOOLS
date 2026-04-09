import { Routes } from '@angular/router';
import { EntityListComponent } from './components/entity-list/entity-list.component';
import { EntityCreateComponent } from './components/entity-create/entity-create.component';
import { EntityEditComponent } from './components/entity-edit/entity-edit.component';
import { EntityDetailComponent } from './components/entity-detail/entity-detail.component';
import { UserListComponent } from './components/user-list/user-list.component';
import { UserCreateComponent } from './components/user-create/user-create.component';
import { UserEditComponent } from './components/user-edit/user-edit.component';
import { UserDetailComponent } from './components/user-detail/user-detail.component';
import { UserEntityListComponent } from './components/user-entity-list/user-entity-list.component';
import { UserEntityCreateComponent } from './components/user-entity-create/user-entity-create.component';
import { UserEntityEditComponent } from './components/user-entity-edit/user-entity-edit.component';

export const routes: Routes = [
  { path: '', redirectTo: '/entities', pathMatch: 'full' },
  
  // Entity routes
  { path: 'entities', component: EntityListComponent },
  { path: 'entities/create', component: EntityCreateComponent },
  { path: 'entities/:id', component: EntityDetailComponent },
  { path: 'entities/:id/edit', component: EntityEditComponent },
  
  // User routes
  { path: 'users', component: UserListComponent },
  { path: 'users/create', component: UserCreateComponent },
  { path: 'users/:id', component: UserDetailComponent },
  { path: 'users/:id/edit', component: UserEditComponent },
  
  // UserEntity routes
  { path: 'user-entities', component: UserEntityListComponent },
  { path: 'user-entities/create', component: UserEntityCreateComponent },
  { path: 'user-entities/:id/edit', component: UserEntityEditComponent },
  
  // Redirect unknown routes to home
  { path: '**', redirectTo: '/entities' }
];
