import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withMethods, withState, withHooks } from '@ngrx/signals';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';
import { tapResponse } from '@ngrx/operators';
import { ToastService } from '../services/toast.service';

type AuthState = {
    user: any | null;
    role: string | null;
    token: string | null;
    plan: 'FREE' | 'PREMIUM' | null;
    isLoading: boolean;
    error: string | null;
};

const initialState: AuthState = {
    user: null,
    token: localStorage.getItem('token'),
    role: localStorage.getItem('role'),
    plan: 'FREE', 
    isLoading: false,
    error: null
};

export const AuthStore = signalStore(
    { providedIn: 'root' },
    withState(initialState),
    
    withComputed(({ token, role, plan }) => ({
        isAuthenticated: computed(() => !!token()),
        isAdmin: computed(() => role() === 'ADMIN'),
        isCoach: computed(() => role() === 'COACH'),
        isPremium: computed(() => plan() === 'PREMIUM'),
    })),

    withMethods((store, authService = inject(AuthService), toastService = inject(ToastService), router = inject(Router)) => ({
        
        login: rxMethod<any>(
            pipe(
                tap(() => patchState(store, { isLoading: true, error: null })),
                switchMap((credentials) =>
                    authService.login(credentials).pipe(
                        tapResponse({
                            next: (res: any) => {
                                const actualToken = res.accessToken || res.token || res.access_token || res.jwt;

                                if (actualToken) {
                                    localStorage.clear();

                                    localStorage.setItem('token', actualToken);
                                    if (res.refreshToken) localStorage.setItem('refreshToken', res.refreshToken);
                                    if (res.role) localStorage.setItem('role', res.role);

                                    patchState(store, {
                                        token: actualToken,
                                        role: res.role,
                                        plan: res.plan || 'FREE',
                                        isLoading: false,
                                        error: null
                                    });

                                       toastService.success('Connexion réussie', 'Succès');
                                    setTimeout(() => {
                                        const target = res.role === 'ADMIN' ? '/admin/dashboard' : '/';
                                        router.navigate([target]);
                                    }, 100);
                                } else {
                                    patchState(store, { isLoading: false, error: "Token missing from server response" });
                                    toastService.error('Erreur de connexion: Token manquant', 'Erreur');
                                }
                            },
                            error: (err: any) => {
                                console.error("Login Error:", err);
                                let errorMessage = "Erreur serveur. Veuillez vérifier votre connexion.";
                                if (err.status === 403 || err.status === 401 || err.status === 0) {
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
                                toastService.error(errorMessage, 'Erreur');
                            },
                        })
                    )
                )
            )
        ),

        fetchProfileFromDb: () => {
            const token = store.token();
            if (!token) return; 

            authService.getProfile().subscribe({
                next: (realUser) => {
                    patchState(store, {
                        user: realUser,
                        role: realUser.role,
                        plan: realUser.plan 
                    });
                    console.log('✅ Plan vérifié depuis la DB:', realUser.plan);
                },
                error: () => {
                    console.error('❌ Erreur: Token invalide ou expiré');
                    localStorage.clear();
                    patchState(store, { user: null, token: null, role: null, plan: 'FREE' });
                    router.navigate(['/login']);
                    toastService.error('Erreur de session. Veuillez vous reconnecter.', 'Erreur');
                }
            });
        },

        logout: () => {
            console.log('🚪 Déconnexion...');
            localStorage.clear(); 
            patchState(store, { user: null, token: null, role: null, plan: 'FREE' });
            router.navigate(['/login']);
            toastService.success('Déconnexion réussie', 'Succès');
        },

        setUser: (user: any) => {
            patchState(store, { user, isLoading: false });
        },

        setPlan: (plan: 'FREE' | 'PREMIUM') => {
            patchState(store, { plan });
        }

    })),

    withHooks({
        onInit(store) {
            store.fetchProfileFromDb();
        }
    })
);