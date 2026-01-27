import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FrameAnalysis } from '../../../../core/models/analysis.model';

@Component({
  selector: 'app-stats-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './stats-panel.component.html',
  styleUrls: []
})
export class StatsPanelComponent implements OnChanges {
  @Input() frameData: FrameAnalysis | null = null;
  playersCount = '--';
  ballStatus = '--';
  ballStatusColor = 'var(--text-secondary)';
  team1Possession = '--%';
  team2Possession = '--%';

  showBallHolder = false;
  ballHolderNumber = '--';
  ballHolderTeam = '--';
  ballHolderSpeed = '-- km/h';
  ballHolderDistance = '-- m';
  ballHolderClass = '';

  fastestPlayer: string | null = null;
  fastestSpeed: string | null = null;
  topDistancePlayer: string | null = null;
  topDistance: string | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['frameData'] && this.frameData) {
      this.updateStats(this.frameData);
    }
  }

  private updateStats(data: FrameAnalysis): void {
    const totalPlayers = data.players_count ?? data.players_count ?? 0;
    const team1Count = data.team_1_count ?? data.team_1_count ?? 0;
    const team2Count = data.team_2_count ?? data.team_2_count ?? 0;
    this.playersCount = `${totalPlayers} (${team1Count}/${team2Count})`;

    const ballDetected = data.ball_detected ?? data.ball_detected ?? false;
  }
}