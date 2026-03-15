import { CommonModule } from '@angular/common';
import { Component, computed, inject, effect } from '@angular/core';
import { VideoStore } from '../../../../core/store/video.store';

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.css'
})
export class TimelineComponent {
  
  readonly store = inject(VideoStore);

  constructor() {
    effect(() => {
      console.log("⏱️ Current Video Time:", this.store.currentTime());
      console.log("📦 All Events in Store:", this.store.matchEvents());
    });
  }

  events = computed(() => {
    const currentVideoTime = this.store.currentTime();
    const realEvents = this.store.matchEvents();

    const visibleEvents = realEvents.filter(event => (event.timeSeconds || 0) <= currentVideoTime);
    
    if (realEvents.length > 0) {
      console.log("🎯 Visible Events after filter:", visibleEvents);
    }

    return visibleEvents;
  });
}