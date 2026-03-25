import { Component, computed, effect, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BehaviorSubject, Observable, Subscription } from 'rxjs'; // 🔴 1. جبنا Subscription باش نتحكمو فـ WebSocket
import { ApiService } from '../../../core/services/api.service';
import { VideoResponse, FrameAnalysis, AnalysisMessage } from '../../../core/models/analysis.model';
import { UploadZoneComponent } from '../components/upload-zone/upload-zone.component';
import { TimelineComponent } from '../components/timeline/timeline.component';
import { StatsPanelComponent } from '../components/stats-panel/stats-panel.component';
import { environment } from '../../../../environments/environment';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { VideoStore } from '../../../core/store/video.store';
import { VideoPlayerComponent } from '../components/video-player/video-player.component';
import { VideoControlsComponent } from './video-controls/video-controls.component';
import { RouterLink } from '@angular/router';
import { RadarTactiqueComponent } from '../../../shared/components/radar-tactique/radar-tactique';
import { WebsocketService } from '../../../core/services/websocket.service';
import { EquipeService } from '../../../core/services/equipe.service';
import { ToastService } from '../../../core/services/toast.service';
import { HttpErrorResponse } from '@angular/common/http';
import { TeamCreationModalComponent } from '../../team/components/team-creation-modal/team-creation-modal.component';
import { Equipe } from '../../../core/models/equipe.model';

@Component({
  selector: 'app-video-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RadarTactiqueComponent,
    UploadZoneComponent,
    TimelineComponent,
    StatsPanelComponent,
    VideoPlayerComponent,
    VideoControlsComponent,
    RouterLink,
    TeamCreationModalComponent
  ],
  templateUrl: './video-dashboard.component.html'
})
export class VideoDashboardComponent implements OnInit, OnDestroy {
  readonly store = inject(VideoStore);
  private sanitizer = inject(DomSanitizer);
  private wsService = inject(WebsocketService);
  private equipeService = inject(EquipeService);
  private toastService = inject(ToastService);
  private wsSubscription?: Subscription;
  

  livePlayers = signal<any[]>([]);
  liveFrameData = signal<any>(null);

  showLimitModal = signal(false);
  showTeamCreationModal = signal(false);
  pendingAnalysisStart = signal(false);

  readonly isLimitError = computed(() => {
    const code = this.store.uploadErrorCode();
    return code === 401 || code === 402;
  });

  constructor() {
    effect(() => {
      const players = this.livePlayers();
    });

    effect(() => {
      const videoId = this.store.currentVideoId();
      const isAnalyzing = this.store.isAnalyzing();

      if (videoId && isAnalyzing) {
        this.connectToAnalysisStream(videoId);
      } else {
        this.livePlayers.set([]);
        this.liveFrameData.set(null);
        if (this.wsSubscription) {
          this.wsSubscription.unsubscribe();
          this.wsSubscription = undefined;
        }
      }
    });

    effect(() => {
      if (this.isLimitError()) {
        this.showLimitModal.set(true);
      }
    });
  }

  ngOnInit() {
    this.store.loadUserVideos();
  }

  connectToAnalysisStream(videoId: string) {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }

    this.wsSubscription = this.wsService.watchVideoAnalysis(videoId).subscribe((data: any) => {
      this.liveFrameData.set(data);
      if (data && data.players) {
        const mapped = data.players
          .filter((p: any) => p.position_field !== null)
          .map((p: any) => {
            const MAX_X_FROM_PYTHON = 25;
            const MAX_Y_FROM_PYTHON = 40;

            let calcX = (p.position_field[0] / MAX_X_FROM_PYTHON) * 100;
            let calcY = (p.position_field[1] / MAX_Y_FROM_PYTHON) * 100;

            calcX = Math.max(0, Math.min(100, calcX));
            calcY = Math.max(0, Math.min(100, calcY));

            return {
              id: p.id,
              team: p.team === 1 ? 'AWAY' : 'HOME',
              FieldX: calcX,
              FieldY: calcY,
              hasBall: p.has_ball || false,
              speed: p.speed_kmh || 0,
              number: p.jersey_number || p.id
            };
          });

        this.livePlayers.set(mapped);
      }
    });
  }

  
  liveStreamSafeUrl = computed(() => {
    const rawUrl = this.store.originalVideoUrl();
    if (!rawUrl) return null;

    const cleanUrl = this.getFullVideoUrl(rawUrl);

    return cleanUrl ? this.sanitizer.bypassSecurityTrustUrl(cleanUrl) : null;
  });

  onVideoSelected(file: File) {
    this.showLimitModal.set(false);
    this.store.uploadVideo(file);
  }

  dismissLimitModal() {
    this.showLimitModal.set(false);
  }

  startAnalysis(): void {
    if (!this.store.currentVideoId() || this.store.isAnalyzing()) {
      return;
    }

    this.equipeService.getMyTeam().subscribe({
      next: () => {
        this.pendingAnalysisStart.set(false);
        this.store.startAnalysis();
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 400 || error.status === 404 || error.status === 500) {
          this.pendingAnalysisStart.set(true);
          this.showTeamCreationModal.set(true);
          return;
        }
        this.toastService.error('Impossible de vérifier votre équipe.', 'Erreur');
      }
    });
  }

  stopAnalysis() {
    this.store.stopAnalysis();
  }

  onTeamCreationModalClose(): void {
    this.showTeamCreationModal.set(false);
    this.pendingAnalysisStart.set(false);
  }

  onTeamCreated(_team: Equipe): void {
    this.showTeamCreationModal.set(false);
    this.toastService.success('Équipe créée avec succès.', 'Succès');

    if (this.pendingAnalysisStart()) {
      this.pendingAnalysisStart.set(false);
      this.store.startAnalysis();
    }
  }

  resetUpload() {
    this.store.resetUpload();
  }

  updateDevice(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    console.log("Device changed to:", value);
  }

  ngOnDestroy() {
    const videoId = this.store.currentVideoId();
    if (videoId) {
      this.wsService.unsubscribeFromVideo(videoId);
    }
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  private getFullVideoUrl(path: string | null | undefined): string | null {
    if (!path) return null;

    const fileName = path.split('/').pop();
    if (!fileName) return null;
   
    return `${environment.apiUrl}/uploads/${fileName}`;
  }


  

}