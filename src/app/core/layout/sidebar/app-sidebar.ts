import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../store/auth.store';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './app-sidebar.html'
})
export class SidebarComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private authStore = inject(AuthStore);
  private toastService = inject(ToastService);

  isMenuOpen = signal(false);
  userProfile = signal<any>(null);

  ngOnInit() {
    this.authService.getProfile().subscribe({
      next: (data) => this.userProfile.set(data),
      error: (err) => console.error('Erreur chargement profil:', err)
    });
  }

  toggleMenu() {
    this.isMenuOpen.update(v => !v);
  }

  onLogout() {
      this.authStore.logout();
      this.router.navigate(['/login']);
  }
}