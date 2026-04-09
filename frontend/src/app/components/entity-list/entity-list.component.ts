import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { Entity } from '../../models/entity.model';
import { EntityService } from '../../services/entity.service';

@Component({
  selector: 'app-entity-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './entity-list.component.html',
  styleUrl: './entity-list.component.css'
})
export class EntityListComponent implements OnInit {
  private entityService = inject(EntityService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  entities: Entity[] = [];
  error: string | null = null;

  ngOnInit(): void {
    this.loadEntities();

    // Recharger les données après chaque navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.loadEntities();
    });
  }

  loadEntities(): void {
    this.entityService.getAll().subscribe({
      next: (data) => {
        this.entities = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des entités';
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }

  deleteEntity(id: number): void {
    this.entityService.delete(id).subscribe({
      next: () => {
        this.entities = this.entities.filter(e => e.id !== id);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors de la suppression:', err);
        alert('Erreur lors de la suppression');
      }
    });
  }
}
