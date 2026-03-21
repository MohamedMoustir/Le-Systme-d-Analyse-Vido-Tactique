import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminPaymentService } from '../../core/services/admin-payment.service';
import { PaymentStats, UserPlan, UserPlanDTO } from '../../core/models/admin.model';
import { ConfirmationAlertComponent } from '../../shared/components/confirmation-alert/confirmation-alert.component';

interface PaymentConfirmationConfig {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  action: {
    type: 'change-plan';
    user: UserPlanDTO;
    newPlan: UserPlan;
  };
}

@Component({
  selector: 'app-payment-management',
  standalone: true,
  imports: [CommonModule, ConfirmationAlertComponent],
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
  p = signal<number>(1);
  readonly itemsPerPage = 10;
  confirmationConfig = signal<PaymentConfirmationConfig | null>(null);

  paginatedUserPlans = computed(() => {
    const start = (this.p() - 1) * this.itemsPerPage;
    return this.userPlans().slice(start, start + this.itemsPerPage);
  });

  totalPages = computed(() => {
    const total = Math.ceil(this.userPlans().length / this.itemsPerPage);
    return Math.max(1, total);
  });

  ngOnInit() { this.loadData(); }

  loadData() {
    this.isLoading.set(true);

    this.paymentService.getPaymentStats().subscribe({
      next: (data) => this.stats.set(data),
      error: (err)  => console.error('Stats error', err)
    });

    this.paymentService.getRecentTransactions().subscribe({
      next: (data) => {
        this.userPlans.set(data);
        this.p.set(1);
        this.isLoading.set(false);
      },
      error: (err)  => { console.error('Transactions error', err); this.isLoading.set(false); }
    });
  }

  toggleDropdown(userId: string) {
    this.openDropdownId.set(this.openDropdownId() === userId ? null : userId);
  }

  onChangePlan(user: UserPlanDTO, newPlan: UserPlan) {
    this.confirmationConfig.set({
      title: 'Confirmation de paiement',
      message: `Changer le plan de ${user.nom} vers ${newPlan} ?`,
      confirmText: 'Confirmer',
      cancelText: 'Annuler',
      action: {
        type: 'change-plan',
        user,
        newPlan
      }
    });
  }

  onConfirmationDecision(confirmed: boolean) {
    const config = this.confirmationConfig();
    this.confirmationConfig.set(null);

    if (!confirmed || !config) {
      return;
    }

    const { user, newPlan } = config.action;
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

  previousPage() {
    if (this.p() > 1) {
      this.p.update(current => current - 1);
    }
  }

  nextPage() {
    if (this.p() < this.totalPages()) {
      this.p.update(current => current + 1);
    }
  }
}
