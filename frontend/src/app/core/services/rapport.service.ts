import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RapportGlobalResponse } from '../models/rapport.model';

@Injectable({
  providedIn: 'root'
})
export class RapportService {
  private http = inject(HttpClient);
 private apiUrl = `${environment.apiUrl}/rapports`;

  getMyStats(): Observable<RapportGlobalResponse> {
    return this.http.get<RapportGlobalResponse>(`${this.apiUrl}/my-stats`);
  }
}