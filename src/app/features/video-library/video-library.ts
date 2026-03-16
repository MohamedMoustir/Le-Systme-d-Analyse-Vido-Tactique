import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VideoStore } from '../../core/store/video.store';
import { SidebarComponent } from "../../core/layout/sidebar/app-sidebar";

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
    let url = `https://savt-vision.live/api/videos/play/${video.id}`;
    
    return url;
  }
}