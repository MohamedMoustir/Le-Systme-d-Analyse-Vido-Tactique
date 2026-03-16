import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-shared-joueur-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './joueur-card.component.html'
})
export class JoueurCardComponent {
  @Input({ required: true }) joueur: any;
  @Output() delete = new EventEmitter<number>();
  @Output() edit = new EventEmitter<any>();

  onDelete() { this.delete.emit(this.joueur.id); }
  onEdit() { this.edit.emit(this.joueur); }

getCorrectImageUrl(url: string | null | undefined): string | null {
  if (!url) return null;
  
  if (url.includes('localhost:8080')) {
    return url.replace('http://localhost:8080', 'https://savt-vision.live/api');
  }
  
  if (!url.startsWith('http')) {
    return `https://savt-vision.live/api/uploads/${url}`; 
  }

  return url;
}
}