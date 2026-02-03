import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { Auth } from '../services/auth';
import { Router } from '@angular/router';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';
import { tapResponse } from '@ngrx/operators';
type AuthState = {
    user: any | null;
    role: string | null;
    token: string | null;
    isLoading: boolean;
    error: string | null;
};

const initialState: AuthState = {
    user: null,
    token: localStorage.getItem('token'),
    role: localStorage.getItem('token'),
    isLoading: false,
    error: null
}

export const AuthStore = signalStore(
    { providedIn: 'root' },
    withState(initialState),
    withComputed(({ token, role }) => ({
        isAuthenticated: computed(() => !!token()),
        isAdmin: computed(() => role() == 'ADMIN'),
        isCoach: computed(() => role() == 'COACH'),
    })),

    withMethods((store, authService = inject(Auth), router = inject(Router)) => ({
        login: rxMethod<any>(
            pipe(
                tap(() => patchState(store, { isLoading: true, error: null })),
                switchMap((credentials) =>
                    authService.login(credentials).pipe(
                        tapResponse({
                            next: (res: any) => {
                                if (res.accessToken) localStorage.setItem('token', res.accessToken);
                                if (res.refreshToken) localStorage.setItem('refreshToken', res.refreshToken);
                                if (res.role) localStorage.setItem('role', res.role);
                                patchState(store, {
                                    token: res.accessToken,
                                    role: res.role,
                                    isLoading: false,
                                    error: null
                                });
                                router.navigate(['/']);
                            },
                            error: (err: any) => {
                                console.error("Login Error:", err);
                                let errorMessage = "Erreur serveur (Vérifiez la connexion Redis).";
                                if (err.status === 403 || err.status === 401) {
                                    errorMessage = "Email ou mot de passe incorrect.";
                                }

                                patchState(store, { isLoading: false, error: errorMessage });
                            },
                        })
                    )
                )
            )
        ),
        register: rxMethod<any>(
      pipe(
        tap(() => patchState(store, { isLoading: true, error: null })),
        switchMap((userData) =>
          authService.register(userData).pipe(
            tapResponse({
              next: () => {
                patchState(store, { isLoading: false, error: null });
                router.navigate(['/login']);
              },
              error: (err: any) => {
                const errorMessage = err?.error?.message || 'Erreur lors de l\'inscription. Veuillez réessayer.';
                patchState(store, { isLoading: false, error: errorMessage });
              },
            })
          )
        )
      )
    ),
        logout: () => {
            localStorage.removeItem('token');
            patchState(store, { user: null, token: null ,role:null});
            router.navigate(['/login']);
        }
    })
    ))