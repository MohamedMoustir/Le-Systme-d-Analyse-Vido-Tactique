import { Component, inject, signal, OnInit } from '@angular/core';
import { DecimalPipe, NgClass } from '@angular/common';
import { PaymentService, StorageUsage } from '../../core/services/payment.service';
import { AuthStore } from '../../core/store/auth.store';

@Component({
  selector: 'app-subscription',
  standalone: true,
  imports: [DecimalPipe, NgClass],
  templateUrl: './subscription.component.html',
})
export class SubscriptionComponent implements OnInit {
  private paymentService = inject(PaymentService);
  readonly authStore = inject(AuthStore);

  storage = signal<StorageUsage | null>(null);
  isLoadingCheckout = signal(false);
  checkoutError = signal<string | null>(null);

  ngOnInit() {
    this.paymentService.getStorageUsage().subscribe({
      next: (data) => this.storage.set(data),
      error: () => this.storage.set({ usedMb: 0, totalMb: 500, percentage: 0 })
    });
  }

  upgradeNow() {
    this.isLoadingCheckout.set(true);
    this.checkoutError.set(null);
    this.paymentService.createCheckoutSession('PREMIUM').subscribe({
      next: (res) => {
        window.location.href = res.checkoutUrl;
      },
      error: (err) => {
        this.isLoadingCheckout.set(false);
        this.checkoutError.set('Impossible de démarrer le paiement. Veuillez réessayer.');
        console.error('Checkout error:', err);
      }
    });
  }

  get storageBarWidth(): number {
    const s = this.storage();
    return s ? Math.min(s.percentage, 100) : 20;
  }

  get storageBarColor(): string {
    const pct = this.storageBarWidth;
    if (pct >= 90) return 'from-red-500 to-red-400';
    if (pct >= 70) return 'from-amber-500 to-yellow-400';
    return 'from-blue-500 to-cyan-400';
  }
}
