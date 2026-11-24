import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../auth-service';
import { Pagination } from '../pagination/pagination';
import { LeaderboardEntry, Page } from '../stats';
import { StatsService } from '../stats-service';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-classement',
  imports: [RouterLink, Pagination],
  templateUrl: './classement.html',
  styleUrl: './classement.scss',
})
export class Classement implements OnInit, OnDestroy {
  public leaderboard = signal<Page<LeaderboardEntry> | null>(null);
  public error = signal<boolean>(false);
  public podium = computed(() => {
    const leaderboard = this.leaderboard();
    return leaderboard !== null && leaderboard.page === 0 ? leaderboard.content.slice(0, 3) : [];
  });
  private subscription: Subscription | null = null;

  constructor(
    private statsService: StatsService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  public loadPage(page: number) {
    this.subscription?.unsubscribe();
    this.error.set(false);
    this.subscription = this.statsService.getLeaderboard(page, PAGE_SIZE).subscribe({
      next: (leaderboard) => this.leaderboard.set(leaderboard),
      error: () => this.error.set(true),
    });
  }

  public rank(leaderboard: Page<LeaderboardEntry>, index: number): number {
    return leaderboard.page * leaderboard.size + index + 1;
  }

  public isMe(entry: LeaderboardEntry): boolean {
    return this.authService.currentUser()?.id === entry.userId;
  }
}
