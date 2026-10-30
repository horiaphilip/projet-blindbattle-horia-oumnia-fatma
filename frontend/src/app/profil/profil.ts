import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, catchError, of, switchMap, takeWhile, timer } from 'rxjs';
import { BadgeLabelPipe } from '../badge-label-pipe';
import { Stats, User } from '../user';
import { UserService } from '../user-service';

@Component({
  selector: 'app-profil',
  imports: [DatePipe, RouterLink, BadgeLabelPipe],
  templateUrl: './profil.html',
  styleUrl: './profil.scss',
})
export class Profil implements OnInit, OnDestroy {
  public user = signal<User | null>(null);
  public stats = signal<Stats | null>(null);
  public notFound = signal<boolean>(false);
  private statsSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.userService.getUser(id).subscribe({
      next: (user) => {
        this.user.set(user);
        this.waitForStats(id);
      },
      error: () => this.notFound.set(true),
    });
  }

  ngOnDestroy(): void {
    this.statsSubscription?.unsubscribe();
  }

  private waitForStats(id: number) {
    this.statsSubscription = timer(0, 1000)
      .pipe(
        switchMap(() => this.userService.getStats(id).pipe(catchError(() => of(null)))),
        takeWhile((stats) => stats === null, true),
      )
      .subscribe((stats) => this.stats.set(stats));
  }
}
