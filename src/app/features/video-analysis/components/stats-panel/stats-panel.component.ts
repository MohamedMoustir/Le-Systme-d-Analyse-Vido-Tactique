import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VideoStore } from '../../../../core/store/video.store'; // Assure-toi du chemin

@Component({
  selector: 'app-stats-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stats-panel.component.html',
  styleUrls: []
})
export class StatsPanelComponent {
  
  readonly store = inject(VideoStore);

  playersCount = computed(() => {
    const data = this.store.currentFrameData();
    if (!data) return '--';
    
    const total = data.players_count ?? 0;
    const t1 = data.team_1_count ?? 0;
    const t2 = data.team_2_count ?? 0;
    
    return `${total} (${t1} / ${t2})`;
  });

  possessionStats = computed(() => {
    const data = this.store.currentFrameData();
    if (!data) return { t1: '--%', t2: '--%' };

    
    return {
      t1: (data as any).possession_t1 ? `${(data as any).possession_t1}%` : '--%',
      t2: (data as any).possession_t2 ? `${(data as any).possession_t2}%` : '--%'
    };
  });

  ballStatus = computed(() => {
    const data = this.store.currentFrameData();
    if (!data) return { text: '--', color: 'text-gray-500', isDetected: false };

    const detected = data.ball_detected ?? false;
    return {
      text: detected ? 'DÉTECTÉ' : 'HORS CHAMP',
      color: detected ? 'text-emerald-400' : 'text-gray-500',
      isDetected: detected
    };
  });

  ballHolder = computed(() => {
    const data = this.store.currentFrameData();
    const holder = (data as any)?.ball_holder; 

    if (!holder) return null; 

    return {
      id: holder.id ?? '?',
      team: holder.team_id === 0 ? 'Equipe A' : 'Equipe B', 
      speed: holder.speed ? `${holder.speed.toFixed(1)} km/h` : '--',
      distance: holder.distance ? `${holder.distance.toFixed(1)} m` : '--',
      colorClass: holder.team_id === 0 ? 'text-blue-400' : 'text-red-400'
    };
  });

 
  topStats = computed(() => {
    const data = this.store.currentFrameData();
    return {
      fastest: (data as any)?.top_speed_player ?? '--',
      maxSpeed: (data as any)?.top_speed ? `${(data as any).top_speed} km/h` : '--',
      distancePlayer: (data as any)?.top_distance_player ?? '--',
      maxDistance: (data as any)?.top_distance ? `${(data as any).top_distance} m` : '--'
    };
  });

}