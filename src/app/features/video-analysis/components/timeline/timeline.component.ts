import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-timeline',
  imports: [CommonModule],
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.css'
})
export class TimelineComponent {
events = [
  { time: '12:30', type: 'goal', description: 'But! Joueur #10', team: 'A' },
  { time: '24:15', type: 'card', description: 'Carton Jaune #4', team: 'B' },
  { time: '45:00', type: 'whistle', description: 'Mi-temps', team: 'neutral' },
];
}
