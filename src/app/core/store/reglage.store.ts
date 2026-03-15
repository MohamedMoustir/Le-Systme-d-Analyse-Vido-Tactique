import { inject } from '@angular/core';
import { signalStore, withState, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, catchError, EMPTY } from 'rxjs';
import { tapResponse } from '@ngrx/operators';
import { ReglageDTO } from '../models/reglage.model';
import { ReglageService } from '../services/reglage.service'; // 🔴 جبنا السيرفيس
import { ToastService } from '../services/toast.service';

interface ReglageState {
  settings: ReglageDTO | null;
  isLoading: boolean;
  isSaving: boolean;
  saveSuccess: boolean;
  error: string | null;
}

const initialState: ReglageState = {
  settings: null,
  isLoading: false,
  isSaving: false,
  saveSuccess: false,
  error: null
};

export const ReglageStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withMethods((store,toastService = inject(ToastService), reglageService = inject(ReglageService)) => ({
    
    loadSettings: rxMethod<void>(
      pipe(
        tap(() => patchState(store, { isLoading: true, error: null })),
        switchMap(() => 
          reglageService.getReglages().pipe( 
            tapResponse({
              next: (data) => patchState(store, { settings: data, isLoading: false }),
              error: (err) => {
                patchState(store, { error: 'Erreur lors du chargement des paramètres.', isLoading: false });
                toastService.error('Erreur lors du chargement', 'Erreur');
              }
            })
          )
        )
      )
    ),

    updateSettings: rxMethod<ReglageDTO>(
      pipe(
        tap(() => patchState(store, { isSaving: true, saveSuccess: false, error: null })),
        switchMap((dto) => 
          reglageService.updateReglages(dto).pipe( 
            tapResponse({
              next: (updatedData) => {
                patchState(store, { settings: updatedData, isSaving: false, saveSuccess: true });
                setTimeout(() => patchState(store, { saveSuccess: false }), 3000);
              },
              error: (err) => {
                console.error('Erreur sauvegarde', err);
                patchState(store, { error: 'Erreur lors de la sauvegarde.', isSaving: false });
                toastService.error('Erreur lors de la sauvegarde', 'Erreur');
              }
            })
          )
        )
      )
    )

  }))
);