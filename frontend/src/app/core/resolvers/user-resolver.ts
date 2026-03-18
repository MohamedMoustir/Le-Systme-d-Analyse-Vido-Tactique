import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AuthStore } from '../store/auth.store';
import { catchError, of, tap } from 'rxjs';

export const userResolver: ResolveFn<boolean> = (route, state) => {
  const authService = inject(AuthService);
  const store = inject(AuthStore);
  if (store.user()) {
    return of(store.user());
  }

 return authService.getProfile().pipe(
    tap((user) => {
      store.setUser(user);
    }),
    catchError(() => {
      return of(null);
    })
  );
};