import { Component, OnInit, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ReglageStore } from '../../core/store/reglage.store';
import { ReglageDTO } from '../../core/models/reglage.model';
import { SidebarComponent } from "../../core/layout/sidebar/app-sidebar";

@Component({
  selector: 'app-reglages',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent],
  templateUrl: './reglages.component.html'
})
export class ReglagesComponent implements OnInit {
  
  readonly store = inject(ReglageStore);
  
  formData = signal<ReglageDTO>({
    nomComplet: '', email: '', nomClub: '', couleurClub: '#EF4444', notificationsActives: false, langue: 'fr'
  });

  constructor() {
    effect(() => {
      const serverData = this.store.settings();
      if (serverData) {
        console.log('=========>',serverData)
        this.formData.set({ ...serverData });
      }
    });
  }

  ngOnInit() {
    
    this.store.loadSettings();
  }

  onSave() {
    this.store.updateSettings(this.formData());
  }
}