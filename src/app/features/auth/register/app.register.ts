import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Auth } from '../../../core/services/auth';
@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './app.register.html',
  styleUrl: './app.register.css',
})
export class Register {

  private fb = inject(FormBuilder);
  private authService = inject(Auth);
  private router = inject(Router);

  registerForm: FormGroup = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['COACH', [Validators.required]]
  });
  isLoading = false;
  errorMessage = '';
  get f() {return this.registerForm.controls;}

  onSubmit() {
    if(this.registerForm.invalid){
  this.registerForm.markAllAsTouched();
  return;
    }
    this.isLoading = true;
    this.errorMessage = '';
    const userData = this.registerForm.value;

    this.authService.register(userData).subscribe({
      next: ()=>{
        this.isLoading = false;
        this.router.navigate(['/login']);

      },
      error: (err)=>{
        this.isLoading = false;
        this.errorMessage = err?.error?.message || 'Erreur lors de l\'inscription. Veuillez réessayer.';
      }
    })

  }
  loginWithGoogle() {
  console.log("Tentative de connexion avec Google...");
}
}
