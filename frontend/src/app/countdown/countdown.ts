import { Component, Input, OnChanges, OnDestroy, signal } from '@angular/core';

@Component({
  selector: 'app-countdown',
  imports: [],
  templateUrl: './countdown.html',
  styleUrl: './countdown.scss',
})
export class Countdown implements OnChanges, OnDestroy {
  @Input() endsAt: number | null = null;
  @Input() totalSeconds: number = 0;
  public remaining = signal<number>(0);
  private timer: ReturnType<typeof setInterval> | null = null;

  ngOnChanges(): void {
    this.stop();
    this.tick();
    if (this.endsAt !== null) {
      this.timer = setInterval(() => this.tick(), 250);
    }
  }

  ngOnDestroy(): void {
    this.stop();
  }

  public percent(): number {
    if (this.totalSeconds <= 0) {
      return 0;
    }
    return Math.min(100, (this.remaining() / this.totalSeconds) * 100);
  }

  private tick() {
    const left =
      this.endsAt === null ? 0 : Math.max(0, Math.ceil((this.endsAt - Date.now()) / 1000));
    this.remaining.set(left);
    if (left === 0) {
      this.stop();
    }
  }

  private stop() {
    if (this.timer !== null) {
      clearInterval(this.timer);
      this.timer = null;
    }
  }
}
