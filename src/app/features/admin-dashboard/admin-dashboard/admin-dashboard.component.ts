import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin.service';
import { DashboardStats, UserResponseDTO } from '../../../core/models/admin.model';


@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);

  stats = signal<DashboardStats | null>(null);
  users = signal<UserResponseDTO[]>([]);
  isLoading = signal<boolean>(true);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading.set(true);
    
    this.adminService.getGlobalStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err) => console.error("Erreur de chargement des statistiques", err)
    });

    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error("Erreur de chargement des utilisateurs", err);
        this.isLoading.set(false);
      }
    });
  }

  onToggleStatus(user: UserResponseDTO) {
    if(confirm(`Voulez-vous ${user.activated ? 'bloquer' : 'débloquer'} ${user.nom} ?`)) {
      this.adminService.toggleUserStatus(user.id).subscribe({
        next: () => {
          this.users.update(list => list.map(u => u.id === user.id ? { ...u, activated: !u.activated } : u));
        },
        error: () => alert("Erreur de modification du statut.")
      });
    }
  }

  onChangeRole(user: UserResponseDTO) {
    const newRole = user.role === 'ADMIN' ? 'COACH' : 'ADMIN';
    if(confirm(`Passer ${user.nom} au rôle ${newRole} ?`)) {
      this.adminService.changeUserRole(user.id, newRole).subscribe({
        next: () => {
          this.users.update(list => list.map(u => u.id === user.id ? { ...u, role: newRole } : u));
        },
        error: () => alert("Erreur de modification du rôle.")
      });
    }
  }

  onDeleteUser(userId: string) {
    if(confirm("Êtes-vous sûr de vouloir supprimer cet utilisateur définitivement ?")) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          this.users.update(list => list.filter(u => u.id !== userId));
        },
        error: () => alert("Erreur lors de la suppression.")
      });
    }
  }
}