import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ReglageDTO } from '../models/reglage.model';

@Injectable({
  providedIn: 'root'
})
export class ReglageService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reglages`;

  getReglages(): Observable<ReglageDTO> {
    return this.http.get<ReglageDTO>(this.apiUrl);
  }

  updateReglages(dto: ReglageDTO): Observable<ReglageDTO> {
    return this.http.put<ReglageDTO>(this.apiUrl, dto);
  }
}