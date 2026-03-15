import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-shared-joueur-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './joueur-form.component.html'
})
export class JoueurFormComponent {
  @Input({ required: true }) formGroup!: FormGroup;
  @Input() photoPreview: string | null = null;
  @Input() isEditMode: boolean = false; 
  @Output() close = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<void>();
  @Output() uploadPhoto = new EventEmitter<void>();

  onClose() { this.close.emit(); }
  onSubmit() { this.submitForm.emit(); }
  onPhotoClick() { this.uploadPhoto.emit(); }
}