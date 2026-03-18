import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stats-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stats-panel.component.html' 
})
export class StatsPanelComponent {
  data = input<any>(null);

  playersCount = computed(() => {
    const frame = this.data();
    if (!frame?.players) return '0 (0/0)';
    
    const players = frame.players;
    const t1 = players.filter((p: any) => p.team === 1).length;
    const t2 = players.filter((p: any) => p.team === 2).length;
    return `${players.length} (${t1}/${t2})`;
  });

  ballStatus = computed(() => {
    const frame = this.data();
    const detected = !!frame?.ball_detected;
    return {
      text: detected ? 'DÉTECTÉ' : 'HORS CHAMP',
      isDetected: detected
    };
  });

  topStats = computed(() => {
    const frame = this.data();
    const players = frame?.players || [];
    
    if (players.length === 0) {
      return { fastest: '--', maxSpeed: '0.0 km/h', distancePlayer: '--', maxDistance: '0.0 m' };
    }

    const fastest = players.reduce((prev: any, curr: any) => 
      ((prev.speed_kmh || 0) > (curr.speed_kmh || 0)) ? prev : curr
    );

    const furthest = players.reduce((prev: any, curr: any) => 
      ((prev.distance_m || 0) > (curr.distance_m || 0)) ? prev : curr
    );

    return {
      fastest: fastest.id ? `Joueur #${fastest.id}` : '--',
      maxSpeed: `${(fastest.speed_kmh || 0).toFixed(1)} km/h`,
      distancePlayer: furthest.id ? `Joueur #${furthest.id}` : '--',
      maxDistance: `${(furthest.distance_m || 0).toFixed(1)} m`
    };
  });
}