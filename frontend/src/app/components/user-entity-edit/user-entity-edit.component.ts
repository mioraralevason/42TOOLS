import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UserEntity } from '../../models/user-entity.model';
import { UserEntityService } from '../../services/user-entity.service';
import { EntityService } from '../../services/entity.service';
import { UserService } from '../../services/user.service';
import { Entity } from '../../models/entity.model';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-entity-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './user-entity-edit.component.html',
  styleUrl: './user-entity-edit.component.css'
})
export class UserEntityEditComponent implements OnInit {
  private userEntityService = inject(UserEntityService);
  private entityService = inject(EntityService);
  private userService = inject(UserService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  userEntity: UserEntity | null = null;
  userId = 0;
  entityId = 0;
  users: User[] = [];
  entities: Entity[] = [];
  loading = false;
  loadingData = true;
  error: string | null = null;
  userEntityId = 0;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.userEntityId = +id;
      this.loadData();
    }
  }

  loadData(): void {
    this.loadingData = true;
    Promise.all([
      this.userEntityService.getById(this.userEntityId).toPromise(),
      this.userService.getAll().toPromise(),
      this.entityService.getAll().toPromise()
    ]).then(([userEntity, users, entities]) => {
      // Mapper snake_case vers camelCase
      const mappedUserEntity = userEntity ? {
        ...userEntity,
        userId: userEntity.user_id || 0,
        entityId: userEntity.entity_id || 0
      } : null;

      this.userEntity = mappedUserEntity;
      this.users = users || [];
      this.entities = entities || [];
      if (this.userEntity) {
        this.userId = this.userEntity.userId;
        this.entityId = this.userEntity.entityId;
      }
      this.loadingData = false;
      this.cdr.detectChanges();
    }).catch(err => {
      this.error = 'Erreur lors du chargement des données';
      this.loadingData = false;
      this.cdr.detectChanges();
      console.error(err);
    });
  }

  updateUserEntity(): void {
    if (!this.userId || !this.entityId) {
      this.error = 'Veuillez sélectionner un utilisateur et une entité';
      return;
    }

    this.loading = true;
    this.error = null;

    this.userEntityService.update(this.userEntityId, {
      userId: this.userId,
      entityId: this.entityId
    }).subscribe({
      next: () => {
        this.router.navigate(['/user-entities']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la mise à jour de l\'association';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }
}
