import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { UserResponseDTO } from '../../../core/models/admin.model';
import { UserCreateComponent } from '../user-create/user-create';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, UserCreateComponent],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private adminService = inject(AdminService);

  users = signal<UserResponseDTO[]>([]);
  isLoading = signal<boolean>(true);
  isCreating = signal<boolean>(false);
  
  searchQuery = signal<string>('');

  filteredUsers = computed(() => {
    const query = this.searchQuery().toLowerCase();
    return this.users().filter(u => 
      u.nom.toLowerCase().includes(query) || 
      u.email.toLowerCase().includes(query)
    );
  });

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading.set(true);
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error("Erreur Users", err);
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
        error: () => alert("Erreur de statut.")
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
        error: () => alert("Erreur de rôle.")
      });
    }
  }

  onDeleteUser(userId: string, nom: string) {
    if(confirm(`Supprimer ${nom} DÉFINITIVEMENT ?`)) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          this.users.update(list => list.filter(u => u.id !== userId));
        },
        error: () => alert("Erreur de suppression.")
      });
    }
  }
}