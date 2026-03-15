import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {  RapportService } from '../../core/services/rapport.service';
import { RapportGlobalResponse } from '../../core/models/rapport.model';
import { SidebarComponent } from "../../core/layout/sidebar/app-sidebar";


@Component({
  selector: 'app-rapports',
  standalone: true,
  imports: [CommonModule, SidebarComponent],
  templateUrl: './rapports.component.html'
})
export class RapportsComponent implements OnInit {
  private rapportService = inject(RapportService);
  
  rapportData = signal<RapportGlobalResponse | null>(null);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.isLoading.set(true);
    this.rapportService.getMyStats().subscribe({
      next: (data) => {
        this.rapportData.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error("Erreur de chargement des stats", err);
        this.errorMessage.set("Impossible de charger les statistiques.");
        this.isLoading.set(false);
      }
    });
  }

  getWinRate(victoires: number, matchsJoues: number): number {
    if (matchsJoues === 0) return 0;
    return Math.round((victoires / matchsJoues) * 100);
  }
}