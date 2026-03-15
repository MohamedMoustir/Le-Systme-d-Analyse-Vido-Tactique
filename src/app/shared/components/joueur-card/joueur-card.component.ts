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
}