import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EquipeService } from '../../../core/services/equipe.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-create-team',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-team.component.html',
  styleUrl: './create-team.component.css'
})
export class CreateTeamComponent implements OnInit {
  private fb = inject(FormBuilder);
  private equipeService = inject(EquipeService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  readonly createTeamForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    hexColor: ['#2563eb', [Validators.pattern(/^#([A-Fa-f0-9]{6})$/)]]
  });

  ngOnInit(): void {
    this.equipeService.getMyTeam().subscribe({
      next: () => this.router.navigate(['/analysis']),
      error: () => null
    });
  }

  submit(): void {
    if (this.createTeamForm.invalid || this.isSubmitting()) {
      this.createTeamForm.markAllAsTouched();
      return;
    }

    const name = this.createTeamForm.value.name?.trim();
    const hexColor = this.createTeamForm.value.hexColor?.trim();

    if (!name) {
      this.errorMessage.set('Le nom de l\'équipe est requis.');
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    this.equipeService.createTeam(name, hexColor || undefined).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success('Votre équipe a été créée.', 'Succès');
        this.router.navigate(['/analysis']);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Impossible de créer l\'équipe pour le moment.');
        this.toastService.error('Impossible de créer l\'équipe.', 'Erreur');
      }
    });
  }
}
