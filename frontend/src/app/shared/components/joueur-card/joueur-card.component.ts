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

  if (url.startsWith('http') && !url.includes('localhost')) {
    return url;
  }

  let cleanPath = url.replace('http://localhost:8080', '');
  
  if (!cleanPath.startsWith('/uploads/')) {
    cleanPath = '/uploads/' + (cleanPath.startsWith('/') ? cleanPath.substring(1) : cleanPath);
  }

  if (window.location.hostname === 'localhost') {
    return `http://localhost:8080/api${cleanPath}`;
  }

 
  return `/api${cleanPath}`;
}
}