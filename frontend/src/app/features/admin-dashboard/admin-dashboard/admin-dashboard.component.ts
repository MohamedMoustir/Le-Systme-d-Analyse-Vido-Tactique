import { Component, OnInit, inject, signal, effect, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { DashboardStats, UserResponseDTO } from '../../../core/models/admin.model';
import { Chart, registerables } from 'chart.js'; 
Chart.register(...registerables); 

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  private platformId = inject(PLATFORM_ID); 
  stats = signal<DashboardStats | null>(null);
  users = signal<UserResponseDTO[]>([]);
  isLoading = signal<boolean>(true);

  chartRoles: Chart | null = null;
  chartStatus: Chart | null = null;

  constructor() {
    effect(() => {
      const userList = this.users();
      
      if (userList.length > 0 && isPlatformBrowser(this.platformId)) {
        setTimeout(() => this.buildCharts(userList), 0);
      }
    });
  }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading.set(true);
    
    this.adminService.getGlobalStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error("Erreur stats", err)
    });

    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error("Erreur users", err);
        this.isLoading.set(false);
      }
    });
  }

  buildCharts(userList: UserResponseDTO[]) {
    const adminsCount = userList.filter(u => u.role === 'ADMIN').length;
    const coachesCount = userList.filter(u => u.role === 'COACH').length;
    const autresCount = userList.length - (adminsCount + coachesCount); 

    const actifsCount = userList.filter(u => u.activated === true).length;
    const bloquesCount = userList.filter(u => u.activated === false).length;

    const ctxRoles = document.getElementById('rolesChart') as HTMLCanvasElement;
    if (ctxRoles) {
      if (this.chartRoles) this.chartRoles.destroy(); 
      
      this.chartRoles = new Chart(ctxRoles, {
        type: 'doughnut',
        data: {
          labels: ['Admins', 'Coachs', 'Autres'],
          datasets: [{
            data: [adminsCount, coachesCount, autresCount],
            backgroundColor: ['#a855f7', '#06b6d4', '#64748b'], 
            borderWidth: 0,
            hoverOffset: 4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '70%',
          plugins: {
            legend: { position: 'bottom', labels: { color: '#94a3b8', padding: 20 } }
          }
        }
      });
    }

    const ctxStatus = document.getElementById('statusChart') as HTMLCanvasElement;
    if (ctxStatus) {
      if (this.chartStatus) this.chartStatus.destroy(); 
      
      this.chartStatus = new Chart(ctxStatus, {
        type: 'bar',
        data: {
          labels: ['Comptes Actifs', 'Comptes Bloqués'],
          datasets: [{
            label: 'Nombre d\'utilisateurs',
            data: [actifsCount, bloquesCount],
            backgroundColor: ['#10b981', '#ef4444'], 
            borderRadius: 8,
            borderWidth: 0,
            barThickness: 50 
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } }, 
          scales: {
            y: { 
              beginAtZero: true,
              ticks: { stepSize: 1, color: '#94a3b8' }, 
              grid: { color: 'rgba(255, 255, 255, 0.05)' }, 
              border: { display: false } 
            },
            x: { 
              ticks: { color: '#94a3b8' },
              grid: { display: false }, 
              border: { display: false } 
            }
          }
        }
      });
    }
  }

  // onToggleStatus(user: UserResponseDTO) { /* ... */ }
  // onChangeRole(user: UserResponseDTO) { /* ... */ }
  // onDeleteUser(userId: string) { /* ... */ }
}