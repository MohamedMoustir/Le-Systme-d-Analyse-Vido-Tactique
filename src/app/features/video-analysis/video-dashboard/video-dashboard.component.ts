import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { VideoResponse, FrameAnalysis } from '../../../core/models/analysis.model';
import { UploadZoneComponent } from '../components/upload-zone/upload-zone.component';
import { TimelineComponent } from '../components/timeline/timeline.component';
import { StatsPanelComponent } from '../components/stats-panel/stats-panel.component';
import { environment } from '../../../../environments/environment';
@Component({
  selector: 'app-video-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UploadZoneComponent,TimelineComponent, StatsPanelComponent],
  templateUrl: './video-dashboard.component.html'
})
export class VideoDashboardComponent {
  currentVideoId: string | null = null;
  currentVideoPath: string | null = null;
  originalVideoUrl: string | null = null;
  liveStreamUrl: string | null = null;
  status: 'idle' | 'analyzing' | 'error' = 'idle';
  device: string = 'cpu';
  isUploading: boolean = false;
  isAnalyzing: boolean = false;
  isLiveStreaming: boolean = false;
  
  progressPercent: number = 0;
  currentFrameData: FrameAnalysis | null = null;

  constructor(private apiService: ApiService) {}

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

  startAnalysis() {
    if (!this.currentVideoId) return;

    this.isAnalyzing = true;
    this.status = 'analyzing';
    
    this.apiService.startAnalysis(this.currentVideoId).subscribe({
      next: (res) => {
        console.log('Analysis started on server');
      },
      error: (err) => {
        this.status = 'error';
        this.isAnalyzing = false;
        console.error('Analysis failed to start', err);
      }
    });
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