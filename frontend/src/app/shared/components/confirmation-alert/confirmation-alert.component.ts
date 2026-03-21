import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirmation-alert',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirmation-alert.component.html'
})
export class ConfirmationAlertComponent {
  @Input() title: string = 'Confirmation';
  @Input() message: string = 'Êtes-vous sûr de vouloir continuer ?';
  @Input() confirmText: string = 'Confirmer';
  @Input() cancelText: string = 'Annuler';

  @Output() decision = new EventEmitter<boolean>();

  onConfirm() {
    this.decision.emit(true);
  }

  onCancel() {
    this.decision.emit(false);
  }
}