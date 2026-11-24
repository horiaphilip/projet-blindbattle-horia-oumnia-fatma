import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AnswerPad, PadAnswer } from '../answer-pad/answer-pad';
import { Countdown } from '../countdown/countdown';
import {
  GameEvent,
  GameSettings,
  GameStatus,
  JoinResponse,
  RoundEnded,
  RoundStarted,
  Standing,
} from '../game';
import { GameService } from '../game-service';
import { GameStreamService } from '../game-stream-service';
import { Scoreboard } from '../scoreboard/scoreboard';

@Component({
  selector: 'app-jeu',
  imports: [RouterLink, AnswerPad, Countdown, Scoreboard],
  templateUrl: './jeu.html',
  styleUrl: './jeu.scss',
})
export class Jeu implements OnInit, OnDestroy {
  public code: string = '';
  public player: JoinResponse | null = null;
  public settings = signal<GameSettings | null>(null);
  public status = signal<GameStatus>('LOBBY');
  public playerCount = signal<number>(0);
  public round = signal<RoundStarted | null>(null);
  public endsAt = signal<number | null>(null);
  public sent = signal<boolean>(false);
  public reveal = signal<RoundEnded | null>(null);
  public standings = signal<Standing[]>([]);
  public errorMessage = signal<string>('');
  private streamSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private gameService: GameService,
    private streamService: GameStreamService,
  ) {}

  ngOnInit(): void {
    this.code = (this.route.snapshot.paramMap.get('code') ?? '').toUpperCase();
    this.player = this.gameService.player(this.code);
    if (this.player === null) {
      this.router.navigate(['/rejoindre', this.code]);
      return;
    }
    this.gameService.getGame(this.code).subscribe({
      next: (game) => {
        this.settings.set(game.settings);
        this.status.set(game.status);
        this.playerCount.set(game.players.length);
        this.streamSubscription = this.streamService
          .events(this.code)
          .subscribe((event) => this.onEvent(event));
      },
      error: (error: HttpErrorResponse) =>
        this.errorMessage.set(error.error?.detail ?? 'Partie introuvable'),
    });
  }

  ngOnDestroy(): void {
    this.streamSubscription?.unsubscribe();
  }

  public me(): Standing | null {
    return this.standings().find((standing) => standing.playerId === this.player?.playerId) ?? null;
  }

  public rank(): number {
    return (
      this.standings().findIndex((standing) => standing.playerId === this.player?.playerId) + 1
    );
  }

  public answer(answer: PadAnswer) {
    if (this.player === null || this.sent()) {
      return;
    }
    this.sent.set(true);
    this.errorMessage.set('');
    this.gameService
      .answer(this.code, { playerToken: this.player.playerToken, ...answer })
      .subscribe({
        error: (error: HttpErrorResponse) => {
          this.sent.set(false);
          this.errorMessage.set(error.error?.detail ?? 'Réponse non envoyée, réessaie');
        },
      });
  }

  private onEvent(event: GameEvent) {
    switch (event.type) {
      case 'PLAYER_JOINED':
        this.playerCount.set(event.data.playerCount);
        break;
      case 'GAME_STARTED':
        this.status.set('PLAYING');
        break;
      case 'ROUND_STARTED':
        this.status.set('PLAYING');
        this.round.set(event.data);
        this.reveal.set(null);
        this.sent.set(false);
        this.errorMessage.set('');
        this.endsAt.set(
          Date.parse(event.data.endsAt) - (Date.parse(event.data.serverTime) - Date.now()),
        );
        break;
      case 'ROUND_ENDED':
        this.endsAt.set(null);
        this.reveal.set(event.data);
        this.standings.set(event.data.leaderboard);
        break;
      case 'GAME_FINISHED':
        this.status.set('FINISHED');
        this.standings.set(event.data.leaderboard);
        this.streamSubscription?.unsubscribe();
        break;
    }
  }
}
