import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VideoStore } from '../../core/store/video.store';
import { SidebarComponent } from "../../core/layout/sidebar/app-sidebar";
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-video-library',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent],
  templateUrl: './video-library.html'
})
export class VideoLibraryComponent implements OnInit {
  readonly store = inject(VideoStore);
  private cacheBuster = new Date().getTime();

  ngOnInit() {
    this.store.loadUserVideos();
  }

  getVideoUrl(video: any): string {
    return `${environment.apiUrl}/videos/play/${video.id}`;
  
  }
}