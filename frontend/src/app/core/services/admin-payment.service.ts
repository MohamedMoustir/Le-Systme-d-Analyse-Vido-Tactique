import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentStats, UserPlan, UserPlanDTO } from '../models/admin.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminPaymentService {
  private http    = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/admin/payments`;
  private userUrl = `${environment.apiUrl}/admin/users`;

  getPaymentStats(): Observable<PaymentStats> {
    return this.http.get<PaymentStats>(`${this.baseUrl}/stats`);
  }

  getRecentTransactions(): Observable<UserPlanDTO[]> {
    return this.http.get<UserPlanDTO[]>(`${this.baseUrl}/transactions`);
  }

  updateUserPlan(userId: string, newPlan: UserPlan): Observable<any> {
    return this.http.put(
      `${this.userUrl}/${userId}/plan?newPlan=${newPlan}`, 
      {} 
    );
  }
}
