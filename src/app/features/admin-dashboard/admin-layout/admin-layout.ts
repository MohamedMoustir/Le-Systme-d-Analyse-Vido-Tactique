import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminSidebarComponent } from '../../../shared/components/admin-sidebar/admin-sidebar';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, AdminSidebarComponent],
  template: `
    <div class="flex h-screen bg-[#0a0f1c] overflow-hidden">
      <app-admin-sidebar></app-admin-sidebar>
      
      <main class="flex-1 overflow-y-auto relative">
        <div class="absolute top-0 left-1/2 w-full h-[500px] bg-blue-600/5 blur-[120px] -translate-x-1/2 rounded-full pointer-events-none"></div>
        
        <div class="relative z-10">
          <router-outlet></router-outlet>
        </div>
      </main>
    </div>
  `
})
export class AdminLayoutComponent {}