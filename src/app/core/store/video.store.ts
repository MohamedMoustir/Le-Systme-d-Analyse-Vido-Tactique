import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { computed, inject } from '@angular/core';
import { ApiService } from '../services/api.service';
import { tapResponse } from '@ngrx/operators';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, map, takeWhile, timer } from 'rxjs'; 
import { environment } from '../../../environments/environment';
import { VideoResponse, FrameAnalysis } from '../models/analysis.model';

type VideoState = {
  currentVideoId: string | null;
  currentVideoPath: string | null;
  originalVideoUrl: string | null;
  streamRawUrl: string | null;
  
  isUploading: boolean;
  isAnalyzing: boolean;
  isLiveStreaming: boolean;
  
  status: 'idle' | 'analyzing' | 'error';
  uploadMessage: string;
  progressPercent: number;
  device: string;
  currentFrameData: FrameAnalysis | null;
};

const initialState: VideoState = {
  currentVideoId: null,
  currentVideoPath: null,
  originalVideoUrl: null,
  streamRawUrl: null,
  isUploading: false,
  isAnalyzing: false,
  isLiveStreaming: false,
  status: 'idle',
  uploadMessage: '',
  progressPercent: 0,
  device: 'cpu',
  currentFrameData: null
};

export const VideoStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withComputed(({ status }) => ({
    statusText: computed(() => {
      switch (status()) {
        case 'idle': return 'Prêt';
        case 'analyzing': return 'Analyse en cours...';
        case 'error': return 'Erreur';
        default: return 'En attente';
      }
    }),
    canStartAnalysis: computed(() => status() === 'idle'),
  })),

  withMethods((store, apiService = inject(ApiService)) => {
    
    const startProgressSimulation = rxMethod<void>(
      pipe(
        switchMap(() => timer(0, 1000).pipe(
          map(i => (i + 1) * 5),
          takeWhile(val => val <= 100 && store.isAnalyzing()), 
          tap(val => patchState(store, { progressPercent: val }))
        ))
      )
    );

    return {
      uploadVideo: rxMethod<File>(
        pipe(
          tap(() => patchState(store, { isUploading: true, uploadMessage: 'Upload en cours...' })),
          switchMap((file) =>
            apiService.uploadVideo(file).pipe(
              tapResponse({
                next: (res: VideoResponse) => {
                  patchState(store, {
                    isUploading: false,
                    currentVideoId: res.id,
                    currentVideoPath: res.urlFichier,
                    originalVideoUrl: `${environment.uploadsUrl}/${res.urlFichier}`,
                    uploadMessage: 'Upload réussi',
                    status: 'idle'
                  });
                },
                error: (err) => {
                  console.error("Upload error", err);
                  patchState(store, { 
                    isUploading: false, 
                    status: 'error', 
                    uploadMessage: 'Erreur upload' 
                  });
                }
              })
            )
          )
        )
      ),

      startAnalysis: rxMethod<void>(
        pipe(
          tap(() => {
            if (!store.currentVideoId()) return;
            patchState(store, { 
              isAnalyzing: true, 
              status: 'analyzing', 
              uploadMessage: "Démarrage de l'analyse...", 
              progressPercent: 0 
            });
          }),
          switchMap(() => {
            const videoId = store.currentVideoId();
            if (!videoId) return [];

            return apiService.startAnalysis(videoId).pipe(
              tapResponse({
                next: (res) => {
                  console.log('Analysis started:', res);
                  
                  const timeTag = Date.now();
                  const rawUrl = `${environment.streamUrl}/mjpeg?videoPath=${store.currentVideoPath()}&videoId=${videoId}&t=${timeTag}`;

                  patchState(store, {
                    isLiveStreaming: true,
                    streamRawUrl: rawUrl
                  });

                  startProgressSimulation(); 
                },
                error: (err) => {
                  console.error('Analysis failed', err);
                  patchState(store, { status: 'error', isAnalyzing: false });
                }
              })
            );
          })
        )
      ),

      stopAnalysis: rxMethod<void>(
        pipe(
          switchMap(() => 
            apiService.stopAnalysis().pipe(
              tapResponse({
                next: () => {
                  patchState(store, { 
                    isAnalyzing: false, 
                    status: 'idle', 
                    isLiveStreaming: false,
                    progressPercent: 0
                  });
                },
                error: (err) => console.error(err)
              })
            )
          )
        )
      ),

      resetUpload: () => {
        patchState(store, {
          originalVideoUrl: null,
          currentVideoId: null,
          status: 'idle',
          progressPercent: 0,
          streamRawUrl: null,
          currentFrameData: null 
        });
      }
    };
  })
);