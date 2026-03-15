import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoStore } from '../../../../../app/core/store/video.store'

@Component({
  selector: 'app-video-controls',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './video-controls.component.html'
})
export class VideoControlsComponent {
  readonly store = inject(VideoStore);
  @Output() startAnalysisClick = new EventEmitter<void>();
  @Output() stopAnalysisClick = new EventEmitter<void>();
 
  updateDevice(device: string) {
    console.log("Device changed to:", device);
  }

  onStartAnalysis(): void {
    this.startAnalysisClick.emit();
  }

  onStopAnalysis(): void {
    this.stopAnalysisClick.emit();
  }
}