import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-create.html'
})
export class UserCreateComponent {
  private fb = inject(FormBuilder);
  private adminService = inject(AdminService);

  @Output() userCreated = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  createForm: FormGroup = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['COACH', Validators.required]
  });

  isSubmitting = false;
  errorMessage = '';

  onSubmit() {
    if (this.createForm.invalid) return;
    this.isSubmitting = true;
    this.errorMessage = '';

    this.adminService.createUser(this.createForm.value).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.userCreated.emit();
        this.createForm.reset({ role: 'COACH' });
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Erreur lors de la création.';
      }
    });
  }

  onCancel() {
    this.cancel.emit();
  }
}