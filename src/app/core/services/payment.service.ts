import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export interface StorageUsage {
  usedMb: number;
  totalMb: number;
  percentage: number;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/payments`;

  createCheckoutSession(planName: string): Observable<{ checkoutUrl: string }> {
    return this.http.post<{ checkoutUrl: string }>(`${this.apiUrl}/create-checkout-session`, {
      planName,
      successUrl: `${window.location.origin}/?payment=success`,
      cancelUrl: `${window.location.origin}/subscription?payment=cancel`
    });
  }

  getStorageUsage(): Observable<StorageUsage> {
    return this.http.get<StorageUsage>(`${environment.apiUrl}/users/storage-usage`);
  }
}