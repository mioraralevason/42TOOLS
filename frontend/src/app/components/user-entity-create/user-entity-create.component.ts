import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UserEntityService } from '../../services/user-entity.service';
import { EntityService } from '../../services/entity.service';
import { UserService } from '../../services/user.service';
import { Entity } from '../../models/entity.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-entity-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-entity-create.component.html',
  styleUrl: './user-entity-create.component.css'
})
export class UserEntityCreateComponent implements OnInit {
  private userEntityService = inject(UserEntityService);
  private entityService = inject(EntityService);
  private userService = inject(UserService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  userId = 0;
  entityId = 0;
  users: User[] = [];
  entities: Entity[] = [];
  loading = false;
  loadingData = true;
  error: string | null = null;

  ngOnInit(): void {
    this.loadUsersAndEntities();
  }

  loadUsersAndEntities(): void {
    this.loadingData = true;
    Promise.all([
      this.userService.getAll().toPromise(),
      this.entityService.getAll().toPromise()
    ]).then(([users, entities]) => {
      this.users = users || [];
      this.entities = entities || [];
      this.loadingData = false;
      this.cdr.detectChanges();
    }).catch(err => {
      this.error = 'Erreur lors du chargement des données';
      this.loadingData = false;
      this.cdr.detectChanges();
      console.error(err);
    });
  }

  createUserEntity(): void {
    if (!this.userId || !this.entityId) {
      this.error = 'Veuillez sélectionner un utilisateur et une entité';
      this.cdr.detectChanges();
      return;
    }

    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.userEntityService.create({
      userId: this.userId,
      entityId: this.entityId
    }).subscribe({
      next: () => {
        this.router.navigate(['/user-entities']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la création de l\'association';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }
}
