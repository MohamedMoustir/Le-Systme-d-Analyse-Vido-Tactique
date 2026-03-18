import { Component, effect, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-radar-tactique',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './radar-tactique.html'
})
export class RadarTactiqueComponent {
  players = input<any[]>([]);

  constructor() {
    effect(() => {
      console.log('⚽ RADAR COMPONENT: Received players input =', this.players().length);
    });
  }
}