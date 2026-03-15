import { computed, inject } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, EMPTY, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Equipe } from '../models/equipe.model';
import { EquipeService } from '../services/equipe.service';
import { JoueurService } from '../services/joueur.service';
import { Joueur } from '../models/joueur.model';
import { tapResponse } from '@ngrx/operators';
import { ToastService } from '../services/toast.service';

interface EquipeState {
  equipe: Equipe | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: EquipeState = {
  equipe: null,
  isLoading: false,
  error: null
};

export const EquipeStore = signalStore(
  { providedIn: 'root' },

  withState(initialState),

  withComputed(({ equipe }) => ({
    joueurs: computed(() => equipe()?.joueurs || [])
  })),

  withMethods((store,toastService = inject(ToastService), equipeService = inject(EquipeService)) => ({
    loadMyTeam: rxMethod<void>(
      pipe(
        tap(() => patchState(store, { isLoading: true, error: null })),
        switchMap(() => {
          return equipeService.getMyTeam().pipe(
            tap((data) => patchState(store, { equipe: data, isLoading: false })),
            catchError((err) => {
              if (err?.status === 404 || err?.status === 500) {
                console.info('User has no team yet.');
                patchState(store, { equipe: null, error: null, isLoading: false });
                return of(null);
              }

              console.error('Erreur', err);
              patchState(store, { error: 'Erreur lors du chargement', isLoading: false });
              toastService.error('Erreur lors du chargement', 'Erreur');
              return EMPTY;
            })
          );
        })
      )
    )
  })),
  withMethods((store,toastService = inject(ToastService), equipeService = inject(EquipeService), joueurService = inject(JoueurService)) => ({

    removeJoueurLocally(joueurId: string) {
      const currentEquipe = store.equipe();
      if (!currentEquipe) return;

      patchState(store, {
        equipe: {
          ...currentEquipe,
          joueurs: currentEquipe.joueurs.filter(j => j.id !== joueurId)
        }
      });
    },



    importCsv: rxMethod<File>(
      pipe(
        tap(() => patchState(store, { isLoading: true, error: null })),
        switchMap((file) => {
          return equipeService.importJoueursCsv(file).pipe(
            tap((data) => patchState(store, { equipe: data, isLoading: false })),
            catchError((err) => {
              console.error('Erreur import CSV', err);
              patchState(store, { error: "Erreur lors de l'importation du fichier CSV.", isLoading: false });
              return EMPTY;
            })
          );
        })
      )
    ),

    addSingleJoueur: rxMethod<FormData>(
      pipe(
        tap(() => patchState(store, { isLoading: true, error: null })),
        switchMap((formData) =>
          equipeService.addJoueur(formData).pipe(
            tapResponse({
              next: (response) => {
                patchState(store, { isLoading: false });

                store.loadMyTeam();
              },
              error: (err) => {
                console.error('Erreur ajout joueur', err);
                patchState(store, { error: "Erreur lors de l'ajout", isLoading: false });
                toastService.error('Erreur lors de l\'ajout', 'Erreur');
              }
            })
          )
        )
      )
    ),

    deleteJoueur: rxMethod<string>(
      pipe(
        tap(() => patchState(store, { isLoading: true, error: null })),
        switchMap((joueurId) =>
          joueurService.deleteJoueur(joueurId).pipe(
            tapResponse({
              next: () => {
                const currentEquipe = store.equipe();
                if (currentEquipe) {
                  patchState(store, {
                    equipe: {
                      ...currentEquipe,
                      joueurs: currentEquipe.joueurs.filter(j => j.id !== joueurId)
                    },
                    isLoading: false
                  });
                }
              },
              error: (err) => {
                console.error('Erreur delete', err);
                patchState(store, { error: "Erreur suppression", isLoading: false });
                toastService.error('Erreur lors de la suppression', 'Erreur');
              }
            })
          )
        )
      )
    ),

    updateJoueur: rxMethod<{ id: string, dto: any }>(
      pipe(
        tap(() => patchState(store, { isLoading: false })),
        switchMap(({ id, dto }) =>
          joueurService.updateJoueur(id, dto).pipe(
            tap((updatedJoueur) => {
              const currentEquipe = store.equipe();
              if (currentEquipe) {
                const updatedJoueurs = currentEquipe.joueurs.map(j =>
                  j.id === id ? { ...j, ...updatedJoueur } : j
                );
                patchState(store, {
                  equipe: { ...currentEquipe, joueurs: updatedJoueurs },
                  isLoading: false
                });
              }
            }),
            catchError((err) => {
              console.error('Erreur update', err);
              patchState(store, { error: "Erreur modification", isLoading: false });
              toastService.error('Erreur lors de la modification', 'Erreur');
              return EMPTY;
            })
          )
        )
      )
    )

  }))
);