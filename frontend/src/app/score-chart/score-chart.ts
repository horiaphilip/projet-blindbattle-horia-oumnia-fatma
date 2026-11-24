import { formatDate } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { ChartConfiguration, ChartData } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { HistoryEntry } from '../stats';

@Component({
  selector: 'app-score-chart',
  imports: [BaseChartDirective],
  templateUrl: './score-chart.html',
  styleUrl: './score-chart.scss',
})
export class ScoreChart implements OnChanges {
  @Input() entries: HistoryEntry[] = [];
  public data: ChartData<'line', number[], string> = { labels: [], datasets: [] };
  public options: ChartConfiguration<'line', number[], string>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: {
        ticks: { color: this.themeColor('--muted'), maxRotation: 0, autoSkipPadding: 16 },
        grid: { color: this.themeColor('--surface-light') },
      },
      y: {
        beginAtZero: true,
        ticks: { color: this.themeColor('--muted'), precision: 0 },
        grid: { color: this.themeColor('--surface-light') },
      },
    },
  };

  ngOnChanges(): void {
    const oldestFirst = [...this.entries].reverse();
    this.data = {
      labels: oldestFirst.map((entry) => formatDate(entry.playedAt, 'dd/MM HH:mm', 'en-US')),
      datasets: [
        {
          label: 'Score',
          data: oldestFirst.map((entry) => entry.finalScore),
          borderColor: this.themeColor('--primary'),
          pointBackgroundColor: this.themeColor('--secondary'),
          pointBorderColor: this.themeColor('--secondary'),
          pointRadius: 4,
          tension: 0.3,
        },
      ],
    };
  }

  private themeColor(name: string): string {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  }
}
