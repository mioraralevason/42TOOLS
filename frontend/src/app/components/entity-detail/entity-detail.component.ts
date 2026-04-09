import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Entity } from '../../models/entity.model';
import { EntityService } from '../../services/entity.service';

@Component({
  selector: 'app-entity-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './entity-detail.component.html',
  styleUrl: './entity-detail.component.css'
})
export class EntityDetailComponent implements OnInit {
  private entityService = inject(EntityService);
  private route = inject(ActivatedRoute);
  private cdr = inject(ChangeDetectorRef);

  entity: Entity | null = null;
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
}
