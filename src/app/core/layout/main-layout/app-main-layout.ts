import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { SidebarComponent } from '../sidebar/app-sidebar';
import { AuthStore } from '../../store/auth.store';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, CommonModule],
  templateUrl: './app-main-layout.html',
  styleUrl: './app-main-layout.css',
})
export class MainLayout implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  readonly authStore = inject(AuthStore);

  showSuccessToast = signal(false);
  showCancelToast = signal(false);

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      const payment = params.get('payment');
      if (payment === 'success') {
        this.authStore.setPlan('PREMIUM');
        this.showSuccessToast.set(true);
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: {},
          replaceUrl: true
        });
        setTimeout(() => this.showSuccessToast.set(false), 6000);
      } else if (payment === 'cancel') {
        this.showCancelToast.set(true);
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: {},
          replaceUrl: true
        });
        setTimeout(() => this.showCancelToast.set(false), 5000);
      }
    });
  }
}
