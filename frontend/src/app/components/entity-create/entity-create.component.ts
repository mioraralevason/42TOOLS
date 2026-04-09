import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { EntityService } from '../../services/entity.service';

@Component({
  selector: 'app-entity-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './entity-create.component.html',
  styleUrl: './entity-create.component.css'
})
export class EntityCreateComponent {
  private entityService = inject(EntityService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  name = '';
  loading = false;
  error: string | null = null;

  createEntity(): void {
    if (!this.name.trim()) {
      this.error = 'Le nom est obligatoire';
      this.cdr.detectChanges();
      return;
    }

    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.entityService.create({ name: this.name }).subscribe({
      next: () => {
        this.router.navigate(['/entities']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la création de l\'entité';
        this.loading = false;
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }
}
