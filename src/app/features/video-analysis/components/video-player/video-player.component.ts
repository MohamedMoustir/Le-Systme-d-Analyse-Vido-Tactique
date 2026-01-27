import { Component, Input, OnChanges, SimpleChanges, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-player.component.html',
  styleUrls: []
})
export class VideoPlayerComponent implements OnChanges {
  @Input() videoUrl: string | null = null;
  @Input() currentVideoPath: string | null = null;
  @Input() currentVideoId: number | null = null;
  @Input() device = 'cpu';
  @Input() isAnalyzing = false;

  @ViewChild('videoPlayer') videoPlayer!: ElementRef<HTMLVideoElement>;
  @ViewChild('liveStreamImg') liveStreamImg!: ElementRef<HTMLImageElement>;

  activeTab: 'original' | 'live' = 'original';
  isLiveStreamEnabled = false;
  isLiveStreaming = false;
  showStreamLoading = true;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isAnalyzing'] && this.isAnalyzing) {
      this.isLiveStreamEnabled = true;
    }
    
    if (changes['videoUrl'] && this.videoUrl) {
      this.resetTabs();
    }
  }

  showOriginalVideo(): void {
    if (this.isAnalyzing && this.isLiveStreaming) {
      if (!confirm("Arreter l'analyse en cours et revenir a la video originale ?")) {
        return;
      }
      this.stopLiveStream();
    }
    
    this.activeTab = 'original';
  }

  showLiveStream(): void {
    if (!this.currentVideoPath) return;
    
    if (!this.isLiveStreaming) {
      this.startLiveStream();
    }
    
    this.activeTab = 'live';
  }

  private startLiveStream(): void {
    if (!this.currentVideoPath || !this.currentVideoId) return;
    if (this.isLiveStreaming) return;

    this.isLiveStreaming = true;
    this.showStreamLoading = true;

    const streamUrl = `${environment.streamUrl}/mjpeg?videoPath=${encodeURIComponent(this.currentVideoPath)}&videoId=${this.currentVideoId}&device=${this.device}&t=${Date.now()}`;
    
    if (this.liveStreamImg) {
      this.liveStreamImg.nativeElement.onload = () => {
        this.showStreamLoading = false;
        this.liveStreamImg.nativeElement.onload = null;
      };
      
      this.liveStreamImg.nativeElement.onerror = () => {
        console.warn('Erreur de stream - le modele se charge peut-etre encore...');
      };
      
      this.liveStreamImg.nativeElement.src = streamUrl;
    }
  }

  private stopLiveStream(): void {
    this.isLiveStreaming = false;
    this.showStreamLoading = true;
    
    if (this.liveStreamImg) {
      this.liveStreamImg.nativeElement.onload = null;
      this.liveStreamImg.nativeElement.onerror = null;
      this.liveStreamImg.nativeElement.src = '';
    }
    
    this.activeTab = 'original';
    
    fetch('/stream/stop', { method: 'POST' }).catch(() => {});
  }

  private resetTabs(): void {
    this.activeTab = 'original';
    this.isLiveStreamEnabled = false;
    this.stopLiveStream();
  }
}