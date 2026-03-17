import { Component, Input, OnChanges, SimpleChanges, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../../environments/environment';
import { VideoStore } from '../../../../core/store/video.store';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-player.component.html',
  styleUrls: []
})
export class VideoPlayerComponent {
  readonly store = inject(VideoStore);
  private sanitizer = inject(DomSanitizer);
  @Input() src: string | SafeUrl | null = null;

  activeTab = signal<'original' | 'live'>('original');
  isStreamImageLoading = signal(true);
  // safeStreamUrl = computed(() => {
  //   const rawUrl = this.store.streamRawUrl();
  //   return rawUrl ? this.sanitizer.bypassSecurityTrustUrl(rawUrl) : null;
  // });

  constructor() {
    effect(() => {
      if (this.store.isLiveStreaming()) {
        this.activeTab.set('live');
        this.isStreamImageLoading.set(true);
      }
      else if (!this.store.currentVideoId()) {
        this.activeTab.set('original');
      }
    });
  }

  showOriginalVideo(): void {
    if (this.store.isAnalyzing() && this.store.isLiveStreaming()) {
      if (!confirm("Arreter l'analyse en cours et revenir a la video originale ?")) {
        return;
      }
      this.store.stopAnalysis();
    }

    this.activeTab.set('original');
  }
  safeStreamUrl() {
    return this.src;
  }
  showLiveStream(): void {
    if (!this.store.currentVideoPath()) return;

    if (!this.store.isAnalyzing()) {
      this.activeTab.set('live');
    }

  }
  onStreamLoaded() {
    this.isStreamImageLoading.set(false);
  }
  onStreamError() {
    console.warn("En attente de diffusion ou connexion interrompue...");
  }





}