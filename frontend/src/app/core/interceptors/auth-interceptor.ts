import { HttpErrorResponse, HttpInterceptorFn, HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service'; 
import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const http = inject(HttpClient);
  const toastService = inject(ToastService); 
  const apiUrl = environment.apiUrl; 
  
  const token = localStorage.getItem('token'); 

  let authReq = req;
  if (token && token !== 'undefined') {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      
      if (error.status === 401) {
        const refreshToken = localStorage.getItem('refreshToken');

        if (refreshToken) {
          return http.post<any>(`${apiUrl}/auth/refresh`, { refreshToken }).pipe(
            switchMap((res: any) => {
              
              const newToken = res.accessToken || res.token || res.jwt;
              
              localStorage.setItem('token', newToken);
              if (res.refreshToken) {
                localStorage.setItem('refreshToken', res.refreshToken);
              }

              const newAuthReq = req.clone({
                setHeaders: { Authorization: `Bearer ${newToken}` }
              });

              return next(newAuthReq);
            }),
            catchError((refreshError) => {
              localStorage.clear();
              router.navigate(['/login']);
              toastService.error('Session expirée. Veuillez vous reconnecter.', 'Erreur');
              return throwError(() => refreshError); 
            })
          );
        } else {
          localStorage.clear();
          router.navigate(['/login']);
          toastService.error('Session invalide. Veuillez vous reconnecter.', 'Erreur');
        }
      }

      return throwError(() => error);
    })
  );
};