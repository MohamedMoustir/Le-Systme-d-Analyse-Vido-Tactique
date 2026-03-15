import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  
  const token = localStorage.getItem('token'); 

  let authReq = req;
  if (token && token !== 'undefined') {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // if (error.status === 401 || error.status === 403 || error.status === 0) {
      //   console.warn('Token expiré ou invalide. Redirection vers Login...');
        
      //   localStorage.clear(); 
        
      //   router.navigate(['/login']); 
      // }
      return throwError(() => error);
    })
  );
};