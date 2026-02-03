import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Auth } from '../../../core/services/auth';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './app.login.html',
  styleUrl: './app.login.css',
})
export class Login {

  private fb = inject(FormBuilder);
  private authService = inject(Auth);
  private router = inject(Router);

  loginForm :FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  isLoading = false;
  errorMessage = '';

  get f() { return this.loginForm.controls; }

  loginWithGoogle() {
    console.log("Login Google...");
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const credentials = this.loginForm.value;
    this.authService.login(credentials).subscribe({
      next: (res :any) => {
        if(res.accessToken){
          localStorage.setItem('token', res.accessToken);
        }
        if (res.refreshToken) {
          localStorage.setItem('refreshToken', res.refreshToken);
        }
        if (res.role) {
          localStorage.setItem('role', res.role);
        }
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error("Login Error:", err);
        this.isLoading = false;
        if (err.status === 403 || err.status === 401) {
          this.errorMessage = "Email ou mot de passe incorrect.";
        } else {
          this.errorMessage = "Erreur serveur (Vérifiez la connexion Redis).";
        }
      }
    });
  }

}
