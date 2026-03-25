import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VideoStore } from '../../core/store/video.store';
import { SidebarComponent } from "../../core/layout/sidebar/app-sidebar";
import { environment } from '../../../environments/environment';
import { filter } from 'rxjs';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-video-library',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent,FormsModule],
  templateUrl: './video-library.html'
})
export class VideoLibraryComponent implements OnInit {
  readonly store = inject(VideoStore);
  private cacheBuster = new Date().getTime();
  searchTerm=signal("");


  ngOnInit() {
    this.store.loadUserVideos();
  }

  filterVideo = computed(() => {
    const trim = this.searchTerm().toLowerCase().trim();

    if (!trim) this.store.userVideos();
    return this.store.userVideos().filter(f => {
      return f.titre.toLowerCase().includes(trim);
    })

  })


  getVideoUrl(video: any): string {
    return `${environment.apiUrl}/videos/play/${video.id}`;

  }
}