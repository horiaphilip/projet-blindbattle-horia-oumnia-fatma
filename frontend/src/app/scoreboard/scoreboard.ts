import { Component, Input } from '@angular/core';
import { Standing } from '../game';

@Component({
  selector: 'app-scoreboard',
  imports: [],
  templateUrl: './scoreboard.html',
  styleUrl: './scoreboard.scss',
})
export class Scoreboard {
  @Input() standings: Standing[] = [];
  @Input() highlight: number | null = null;
  @Input() showPoints: boolean = false;
  @Input() limit: number = 0;

  public visible(): Standing[] {
    return this.limit > 0 ? this.standings.slice(0, this.limit) : this.standings;
  }
}
