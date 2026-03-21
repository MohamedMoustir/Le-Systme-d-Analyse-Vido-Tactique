import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { UserResponseDTO } from '../../../core/models/admin.model';
import { UserCreateComponent } from '../user-create/user-create';
import { ConfirmationAlertComponent } from '../../../shared/components/confirmation-alert/confirmation-alert.component';

type AdminUsersConfirmAction =
  | { type: 'toggle-status'; user: UserResponseDTO }
  | { type: 'change-role'; user: UserResponseDTO; newRole: 'ADMIN' | 'COACH' }
  | { type: 'delete-user'; userId: string; nom: string };

interface AdminUsersConfirmationConfig {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  action: AdminUsersConfirmAction;
}

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, UserCreateComponent, ConfirmationAlertComponent],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private adminService = inject(AdminService);

  users = signal<UserResponseDTO[]>([]);
  isLoading = signal<boolean>(true);
  isCreating = signal<boolean>(false);
  p: number = 1;
  itemsPerPage: number = 10;
  confirmationConfig = signal<AdminUsersConfirmationConfig | null>(null);
  
  searchQuery = signal<string>('');

  filteredUsers = computed(() => {
    const query = this.searchQuery().toLowerCase();
    return this.users().filter(u => 
      u.nom.toLowerCase().includes(query) || 
      u.email.toLowerCase().includes(query)
    );
  });

  paginatedUsers = computed(() => {
    const start = (this.p - 1) * this.itemsPerPage;
    return this.filteredUsers().slice(start, start + this.itemsPerPage);
  });

  totalPages = computed(() => {
    const total = Math.ceil(this.filteredUsers().length / this.itemsPerPage);
    return Math.max(1, total);
  });

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading.set(true);
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.p = 1;
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error("Erreur Users", err);
        this.isLoading.set(false);
      }
    });
  }

  onToggleStatus(user: UserResponseDTO) {
    this.confirmationConfig.set({
      title: 'Confirmation de statut',
      message: `Voulez-vous ${user.activated ? 'bloquer' : 'débloquer'} ${user.nom} ?`,
      confirmText: user.activated ? 'Bloquer' : 'Débloquer',
      cancelText: 'Annuler',
      action: { type: 'toggle-status', user }
    });
  }

  onChangeRole(user: UserResponseDTO) {
    const newRole = user.role === 'ADMIN' ? 'COACH' : 'ADMIN';
    this.confirmationConfig.set({
      title: 'Confirmation de rôle',
      message: `Passer ${user.nom} au rôle ${newRole} ?`,
      confirmText: 'Confirmer',
      cancelText: 'Annuler',
      action: { type: 'change-role', user, newRole }
    });
  }

  onDeleteUser(userId: string, nom: string) {
    this.confirmationConfig.set({
      title: 'Suppression définitive',
      message: `Supprimer ${nom} DÉFINITIVEMENT ?`,
      confirmText: 'Supprimer',
      cancelText: 'Annuler',
      action: { type: 'delete-user', userId, nom }
    });
  }

  onConfirmationDecision(confirmed: boolean) {
    const config = this.confirmationConfig();
    this.confirmationConfig.set(null);

    if (!confirmed || !config) {
      return;
    }

    switch (config.action.type) {
      case 'toggle-status': {
        const user = config.action.user;
        this.adminService.toggleUserStatus(user.id).subscribe({
          next: () => {
            this.users.update(list => list.map(u => u.id === user.id ? { ...u, activated: !u.activated } : u));
          },
          error: () => alert('Erreur de statut.')
        });
        break;
      }
      case 'change-role': {
        const { user, newRole } = config.action;
        this.adminService.changeUserRole(user.id, newRole).subscribe({
          next: () => {
            this.users.update(list => list.map(u => u.id === user.id ? { ...u, role: newRole } : u));
          },
          error: () => alert('Erreur de rôle.')
        });
        break;
      }
      case 'delete-user': {
        const { userId } = config.action;
        this.adminService.deleteUser(userId).subscribe({
          next: () => {
            this.users.update(list => list.filter(u => u.id !== userId));
            this.ensurePageBounds();
          },
          error: () => alert('Erreur de suppression.')
        });
        break;
      }
    }
  }

  onSearchChange(query: string) {
    this.searchQuery.set(query);
    this.p = 1;
  }

  previousPage() {
    if (this.p > 1) {
      this.p--;
    }
  }

  nextPage() {
    if (this.p < this.totalPages()) {
      this.p++;
    }
  }

  private ensurePageBounds() {
    if (this.p > this.totalPages()) {
      this.p = this.totalPages();
    }
    if (this.p < 1) {
      this.p = 1;
    }
  }
}