import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EquipeService } from '../../../../core/services/equipe.service';
import { Equipe } from '../../../../core/models/equipe.model';

@Component({
  selector: 'app-team-creation-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './team-creation-modal.component.html'
})
export class TeamCreationModalComponent {
  private fb = inject(FormBuilder);
  private equipeService = inject(EquipeService);

  @Output() closed = new EventEmitter<void>();
  @Output() created = new EventEmitter<Equipe>();

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  readonly form = this.fb.group({
    nomEquipe: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    couleurHex: ['#2563eb', [Validators.pattern(/^#([A-Fa-f0-9]{6})$/)]]
  });

  close(): void {
    if (!this.isSubmitting()) {
      this.closed.emit();
    }
  }

  submit(): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    const nomEquipe = this.form.value.nomEquipe?.trim();
    const couleurHex = this.form.value.couleurHex?.trim();

    if (!nomEquipe) {
      this.errorMessage.set('Le nom de l\'équipe est requis.');
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    this.equipeService.createTeam(nomEquipe, couleurHex || undefined).subscribe({
      next: (team) => {
        this.isSubmitting.set(false);
        this.created.emit(team);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Impossible de créer l\'équipe pour le moment.');
      }
    });
  }
}
