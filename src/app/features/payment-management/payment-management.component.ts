import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminPaymentService } from '../../core/services/admin-payment.service';
import { PaymentStats, UserPlan, UserPlanDTO } from '../../core/models/admin.model';

@Component({
  selector: 'app-payment-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-management.component.html',
  styleUrl: './payment-management.component.scss'
})
export class PaymentManagementComponent implements OnInit {
  private paymentService = inject(AdminPaymentService);

  stats          = signal<PaymentStats | null>(null);
  userPlans      = signal<UserPlanDTO[]>([]);
  isLoading      = signal<boolean>(true);
  openDropdownId = signal<string | null>(null);
  loadingPlanId  = signal<string | null>(null);

  ngOnInit() { this.loadData(); }

  loadData() {
    this.isLoading.set(true);

    this.paymentService.getPaymentStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err)  => console.error('Stats error', err)
    });

    this.paymentService.getRecentTransactions().subscribe({
      next: (data) => { this.userPlans.set(data); this.isLoading.set(false); },
      error: (err)  => { console.error('Transactions error', err); this.isLoading.set(false); }
    });
  }

  toggleDropdown(userId: string) {
    this.openDropdownId.set(this.openDropdownId() === userId ? null : userId);
  }

  onChangePlan(user: UserPlanDTO, newPlan: UserPlan) {
    if (!confirm(`Changer le plan de ${user.nom} vers ${newPlan} ?`)) return;

    this.openDropdownId.set(null);
    this.loadingPlanId.set(user.userId);

    this.paymentService.updateUserPlan(user.userId, newPlan).subscribe({
      next: () => {
        this.userPlans.update(list =>
          list.map(u => u.userId === user.userId ? { ...u, plan: newPlan } : u)
        );
        this.loadingPlanId.set(null);
      },
      error: () => {
        alert('Erreur lors du changement de plan.');
        this.loadingPlanId.set(null);
      }
    });
  }
}
