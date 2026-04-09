import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { UserEntity } from '../../models/user-entity.model';
import { UserEntityService } from '../../services/user-entity.service';
import { UserService } from '../../services/user.service';
import { EntityService } from '../../services/entity.service';
import { User } from '../../models/user.model';
import { Entity } from '../../models/entity.model';

@Component({
  selector: 'app-user-entity-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-entity-list.component.html',
  styleUrl: './user-entity-list.component.css'
})
export class UserEntityListComponent implements OnInit {
  private userEntityService = inject(UserEntityService);
  private userService = inject(UserService);
  private entityService = inject(EntityService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  userEntities: UserEntity[] = [];
  users: User[] = [];
  entities: Entity[] = [];
  error: string | null = null;

  ngOnInit(): void {
    this.loadUserEntities();

    // Recharger les données après chaque navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.loadUserEntities();
    });
  }

  loadUserEntities(): void {
    Promise.all([
      this.userEntityService.getAll().toPromise(),
      this.userService.getAll().toPromise(),
      this.entityService.getAll().toPromise()
    ]).then(([userEntities, users, entities]) => {
      // Mapper les données pour convertir snake_case vers camelCase
      this.userEntities = (userEntities || []).map(ue => ({
        ...ue,
        userId: ue.user_id || 0,
        entityId: ue.entity_id || 0
      }));
      this.users = users || [];
      this.entities = entities || [];
      this.cdr.detectChanges();
    }).catch(err => {
      this.error = 'Erreur lors du chargement des associations';
      this.cdr.detectChanges();
      console.error(err);
    });
  }

  getUserName(userId: number): string {
    const user = this.users.find(u => u.id === userId);
    return user ? user.username : 'Utilisateur inconnu';
  }

  getEntityName(entityId: number): string {
    const entity = this.entities.find(e => e.id === entityId);
    return entity ? entity.name : 'Entité inconnue';
  }

  deleteUserEntity(id: number): void {
    this.userEntityService.delete(id).subscribe({
      next: () => {
        this.userEntities = this.userEntities.filter(ue => ue.id !== id);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        alert('Erreur lors de la suppression');
      }
    });
  }
}
