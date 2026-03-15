import { Component, effect, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DragDropModule, CdkDragDrop, moveItemInArray, transferArrayItem, CdkDragEnd } from '@angular/cdk/drag-drop';
import { EquipeStore } from '../../core/store/equipe.store';
import { Joueur } from '../../core/models/joueur.model';

export interface JoueurSurTerrain extends Joueur {
  x?: number,
  y?: number
}
@Component({
  selector: 'app-tableau-tactique',
  standalone: true,
  imports: [CommonModule, DragDropModule],
  templateUrl: './tableau-tactique.component.html'
})
export class TableauTactiqueComponent implements OnInit {
  readonly store = inject(EquipeStore);

  bancDeTouche: Joueur[] = [];
  terrainJoueurs: JoueurSurTerrain[] = [];
  @ViewChild('pitchContainer') pitchContainer!: ElementRef
  constructor() {
    effect(() => {
      const joueurs = this.store.joueurs();
      if (joueurs.length > 0 && this.bancDeTouche.length === 0 && this.terrainJoueurs.length === 0) {
        this.bancDeTouche = [...joueurs];
      }
    });
  }

  ngOnInit() {
    this.store.loadMyTeam();
  }

  drop(event: CdkDragDrop<Joueur[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const joueurDropped = event.previousContainer.data[event.previousIndex];

      const pitchRect = this.pitchContainer.nativeElement.getBoundingClientRect();
      
      const dropPointX = event.dropPoint.x - pitchRect.left;
      const dropPointY = event.dropPoint.y - pitchRect.top;

      const percentX = (dropPointX / pitchRect.width) * 100;
      const percentY = (dropPointY / pitchRect.height) * 100;

      const joueurAvecPosition: JoueurSurTerrain = {
        ...joueurDropped,
        x: percentX,
        y: percentY
      };

      event.previousContainer.data.splice(event.previousIndex, 1);
      this.terrainJoueurs.push(joueurAvecPosition);
    }
    
  }
  onDragEnded(event: CdkDragEnd, joueur: JoueurSurTerrain) {
    const pitchRect = this.pitchContainer.nativeElement.getBoundingClientRect();
    const elementRect = event.source.element.nativeElement.getBoundingClientRect();

    const newX = elementRect.left - pitchRect.left + (elementRect.width / 2);
    const newY = elementRect.top - pitchRect.top + (elementRect.height / 2);

    joueur.x = (newX / pitchRect.width) * 100;
    joueur.y = (newY / pitchRect.height) * 100;

    event.source._dragRef.reset();
  }
}