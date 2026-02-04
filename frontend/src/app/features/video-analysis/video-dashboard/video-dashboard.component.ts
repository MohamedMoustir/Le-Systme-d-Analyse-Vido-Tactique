import { Component, computed, inject } from '@angular/core';
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
import { VideoStore } from '../../../core/store/video.store';
import { VideoPlayerComponent } from '../components/video-player/video-player.component';
import { VideoControlsComponent } from './video-controls/video-controls.component';
@Component({
  selector: 'app-video-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UploadZoneComponent, TimelineComponent, StatsPanelComponent,VideoPlayerComponent, 
    StatsPanelComponent,VideoControlsComponent],
  templateUrl: './video-dashboard.component.html'
})
export class VideoDashboardComponent {
  readonly store = inject(VideoStore);
  private sanitizer = inject(DomSanitizer);

  
  liveStreamSafeUrl = computed(() => {
    const rawUrl = this.store.streamRawUrl();
    return rawUrl ? this.sanitizer.bypassSecurityTrustUrl(rawUrl) : null;
  });

  constructor() { }
  onVideoSelected(file: File) {
   this.store.uploadVideo(file);
  }

  startAnalysis(): void {
    this?.store.startAnalysis();
  }
  
  stopAnalysis() {
    this.store.stopAnalysis()
  }

  resetUpload() {
    this.store.resetUpload();
  }
  updateDevice(event: Event) {
  const value = (event.target as HTMLSelectElement).value;
  // this.store.setDevice(value); 
  console.log("Device changed to:", value);
}
}