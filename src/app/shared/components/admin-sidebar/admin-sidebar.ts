import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AuthStore } from '../../../core/store/auth.store';
import { ToastService } from '../../../core/services/toast.service';
@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './admin-sidebar.html'
})
export class AdminSidebarComponent {
  private router = inject(Router);
  private authStore = inject(AuthStore);
  private toastService = inject(ToastService);

  onLogout() {
      this.authStore.logout();
      this.router.navigate(['/login']);
  }
}