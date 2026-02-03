import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { VideoResponse, FrameAnalysis } from '../../../core/models/analysis.model';
import { UploadZoneComponent } from '../components/upload-zone/upload-zone.component';
import { TimelineComponent } from '../components/timeline/timeline.component';
import { StatsPanelComponent } from '../components/stats-panel/stats-panel.component';
import { environment } from '../../../../environments/environment';
import { timestamp } from 'rxjs';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
@Component({
  selector: 'app-video-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UploadZoneComponent, TimelineComponent, StatsPanelComponent],
  templateUrl: './video-dashboard.component.html'
})
export class VideoDashboardComponent {
  currentVideoId: string | null = null;
  currentVideoPath: string | null = null;
  originalVideoUrl: string | null = null;
  liveStreamUrl: SafeUrl | null = null;
  isUploading = false;
  isAnalyzing = false;
  uploadMessage = '';
  status: 'idle' | 'analyzing' | 'error' = 'idle';
  device: string = 'cpu';
  isLiveStreaming = false;
  progressPercent: number = 0;
  currentFrameData: FrameAnalysis | null = null;
  streamUrl = `${environment.streamUrl}?t=${new Date().getTime()}`;
  constructor(private apiService: ApiService,private sanitizer: DomSanitizer) { }

  onVideoSelected(file: File) {
    this.isUploading = true;
    this.apiService.uploadVideo(file).subscribe({
      next: (res) => {
        this.isUploading = false;
        this.currentVideoId = res.id;
        this.currentVideoPath = res.urlFichier;
        this.originalVideoUrl = `${environment.uploadsUrl}/${res.urlFichier}`;
      },
      error: (err) => {
        this.isUploading = false;
        console.error("Upload error", err);
      }
    });
  }

  startAnalysis(): void {
    if (!this.currentVideoId) return;

    this.isAnalyzing = true;
    this.status = 'analyzing';
    this.uploadMessage = "Démarrage de l'analyse...";
    this.progressPercent = 0;

    this.apiService.startAnalysis(this.currentVideoId).subscribe({
      next: (res) => {
        console.log('Analysis started:', res);
        this.isAnalyzing = true;
        this.isLiveStreaming = true;
        const timeTag = Date.now();
        const rawUrl = `${environment.streamUrl}/mjpeg?videoPath=${this.currentVideoPath}&videoId=${this.currentVideoId}&t=${timeTag}`;
        this.liveStreamUrl = this.sanitizer.bypassSecurityTrustUrl(rawUrl);
        this.simulateProgress();
      },
      error: (err) => {
        this.status = 'error';
        this.isAnalyzing = false;
        console.error('Analysis failed to start', err);
      }
    });
  }
  simulateProgress() {
    const intrval = setInterval(() => {
      if (this.progressPercent >= 100) {
        clearInterval(intrval);
      } else {
        this.progressPercent += 5;
      }
    }, 1000);
  }

  stopAnalysis() {
    this.apiService.stopAnalysis().subscribe(() => {
      this.isAnalyzing = false;
      this.status = 'idle';
      this.isLiveStreaming = false;
    });
  }

  getStatusText(): string {
    switch (this.status) {
      case 'idle': return 'Prêt';
      case 'analyzing': return 'Analyse en cours...';
      case 'error': return 'Erreur';
      default: return 'En attente';
    }
  }

  resetUpload() {
    this.originalVideoUrl = null;
    this.currentVideoId = null;
    this.status = 'idle';
    this.progressPercent = 0;
  }
}