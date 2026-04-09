import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Entity } from '../../models/entity.model';
import { EntityService } from '../../services/entity.service';

@Component({
  selector: 'app-entity-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './entity-edit.component.html',
  styleUrl: './entity-edit.component.css'
})
export class EntityEditComponent implements OnInit {
  private entityService = inject(EntityService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  entity: Entity | null = null;
  name = '';
  loading = false;
  error: string | null = null;
  entityId = 0;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.entityId = +id;
      this.loadEntity();
    }
  }

  loadEntity(): void {
    this.loading = true;
    this.entityService.getById(this.entityId).subscribe({
      next: (data) => {
        this.entity = data;
        this.name = data.name;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement de l\'entité';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }

  updateEntity(): void {
    if (!this.name.trim()) {
      this.error = 'Le nom est obligatoire';
      return;
    }

    this.loading = true;
    this.error = null;

    this.entityService.update(this.entityId, { name: this.name }).subscribe({
      next: () => {
        this.router.navigate(['/entities']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la mise à jour de l\'entité';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }
}
