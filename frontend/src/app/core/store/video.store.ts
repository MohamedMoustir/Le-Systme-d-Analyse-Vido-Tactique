import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { computed, inject, NgZone } from '@angular/core';
import { ApiService } from '../services/api.service';
import { WebsocketService } from '../services/websocket.service';
import { tapResponse } from '@ngrx/operators';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap, finalize, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { VideoResponse, FrameAnalysis } from '../models/analysis.model';
import { ToastService } from '../services/toast.service';

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
  totalFrames: number;
  userVideos: VideoResponse[];
  isLoadingList: boolean;

  currentTime: number;
  matchEvents: any[];
  uploadErrorCode: number | null;
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
  currentFrameData: null,
  totalFrames: 0,
  userVideos: [],
  isLoadingList: false,
  currentTime: 0,
  matchEvents: [],
  uploadErrorCode: null
};

export const VideoStore = signalStore(
  { providedIn: 'root' },
  withState(initialState),

  withComputed(({ status, currentFrameData, totalFrames }) => ({
    statusText: computed(() => {
      switch (status()) {
        case 'idle': return 'Prêt';
        case 'analyzing': return 'Analyse en cours...';
        case 'error': return 'Erreur';
        default: return 'En attente';
      }
    }),
    canStartAnalysis: computed(() => status() === 'idle'),

    realProgress: computed(() => {
      const current = currentFrameData()?.frame_num || 0;
      const total = totalFrames() || 1;
      return Math.min(Math.round((current / total) * 100), 100);
    })
  })),

  withMethods((store, toastService = inject(ToastService),
    apiService = inject(ApiService),
    wsService = inject(WebsocketService),
    zone = inject(NgZone)) => {

    const formatTime = (seconds: number): string => {
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const connectToRealtimeData = rxMethod<string>(
      pipe(
        switchMap((videoId) =>
          wsService.watchVideoAnalysis(videoId).pipe(
            tap((data: any) => {
              zone.run(() => {
                if (data.type === 'match_event' || data.is_goal) {
                  const newEvent = {
                    timeSeconds: data.timestamp || (data.frame_num / 25),
                    time: data.time_str || formatTime(data.timestamp || (data.frame_num / 25)),
                    type: data.eventType?.toLowerCase() || 'info',
                    description: data.description || 'Événement de match',
                    team: data.team_id === 0 ? 'A' : (data.team_id === 1 ? 'B' : 'neutral')
                  };

                  patchState(store, (state) => ({
                    matchEvents: [...state.matchEvents, newEvent]
                  }));
                }
                else if (data.type === 'video_info') {
                  patchState(store, { totalFrames: data.total_frames });
                }
                else {
                  const frameNum = data.frameNum || data.frame_num || 0;
                  patchState(store, {
                    currentFrameData: { ...data },
                    currentTime: frameNum / 25
                  });
                }
              });
            }),
            finalize(() => console.log("Stream ended or disconnected"))
          )
        )
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
                    uploadErrorCode: null,
                    currentVideoId: res.id,
                    currentVideoPath: res.urlFichier,
                    originalVideoUrl: `${environment.uploadsUrl}/${res.urlFichier}`,
                    uploadMessage: 'Upload réussi',
                    status: 'idle'
                  });
                },
                error: (err: any) => {
                  const errorCode = err?.status ?? null;
                  patchState(store, { isUploading: false, status: 'error', uploadMessage: 'Erreur upload', uploadErrorCode: errorCode });
                  toastService.error('Erreur lors de l\'upload', 'Erreur');
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
              progressPercent: 0,
              currentFrameData: null,
              currentTime: 0
            });
          }),
          switchMap(() => {
            const videoId = store.currentVideoId();
            if (!videoId) return [];

            return apiService.startAnalysis(videoId).pipe(
              tapResponse({
                next: (res) => {
                  const timeTag = Date.now();
                  const rawUrl = `${environment.streamUrl}/mjpeg?videoPath=${store.currentVideoPath()}&videoId=${videoId}&t=${timeTag}`;

                  patchState(store, {
                    isLiveStreaming: true,
                    streamRawUrl: rawUrl
                  });

                  connectToRealtimeData(videoId);
                },
                error: (err) => {
                  patchState(store, { status: 'error', isAnalyzing: false });
                  toastService.error('Erreur lors de l\'analyse', 'Erreur');
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
                  const currentId = store.currentVideoId();
                  if (currentId) wsService.unsubscribeFromVideo(currentId);

                  patchState(store, {
                    isAnalyzing: false,
                    status: 'idle',
                    isLiveStreaming: false,
                    progressPercent: 0,
                    streamRawUrl: null
                  });
                },
                error: (err) => {
                  console.error(err);
                  toastService.error('Erreur lors de l\'arrêt de l\'analyse', 'Erreur');
                }
              })
            )
          )
        )
      ),
      loadUserVideos: rxMethod<void>(
        pipe(
          tap(() => patchState(store, { isLoadingList: true })),
          switchMap(() =>
            apiService.getMyVideos().pipe(
              map((videos) =>
                videos.map((video) => {
                  if (video.urlFichier && video.urlFichier.includes('/api/uploads/')) {
                    return {
                      ...video,
                      urlFichier: video.urlFichier.replace('/api/uploads/', '')
                    };
                  }
                  return video;
                })
              ),
              tapResponse({
                next: (cleanedVideos) => patchState(store, { userVideos: cleanedVideos, isLoadingList: false }),
                error: (err) => {
                  patchState(store, { isLoadingList: false });
                  toastService.error('Erreur lors du chargement des vidéos', 'Erreur');
                }
              })
            )
          )
        )
      ),

      clearUploadError: () => {
        patchState(store, { uploadErrorCode: null });
      },

      resetUpload: () => {
        const currentId = store.currentVideoId();
        if (currentId) wsService.unsubscribeFromVideo(currentId);
        patchState(store, initialState);
      }
    };
  })
);