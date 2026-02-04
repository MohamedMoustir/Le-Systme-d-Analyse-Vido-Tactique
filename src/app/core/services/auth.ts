import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Auth {

  private ApiUrl = `${environment.apiUrl}/auth`;
  private http = inject(HttpClient);

  register(userData: any): Observable<any> {
    return this.http.post(`${this.ApiUrl}/register`, userData);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.ApiUrl}/login`, credentials);
  }

  getProfile(): Observable<any> {
   return this.http.get(`${this.ApiUrl}/me`);
  }
}
