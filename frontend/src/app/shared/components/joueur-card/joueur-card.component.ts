import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlayerFlagPipe } from '../../pipes/player-flag-pipe';
import { environment } from '../../../../environments/environment';

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

    if (url.startsWith('http') && !url.includes('localhost') && !url.includes('savt-vision.live')) {
      return url;
    }

    const fileName = url.split('/').pop();
    if (!fileName) return null;

    return `${environment.apiUrl}/uploads/${fileName}`;
  }
}