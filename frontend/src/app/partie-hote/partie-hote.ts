import { Component, ElementRef, OnDestroy, OnInit, ViewChild, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import QRCode from 'qrcode';
import { AuthService } from '../auth-service';
import { Countdown } from '../countdown/countdown';
import {
  AnswerCount,
  Game,
  GameEvent,
  GameStatus,
  PlayerJoined,
  RoundEnded,
  RoundStarted,
  Standing,
} from '../game';
import { GameService } from '../game-service';
import { GameStreamService } from '../game-stream-service';
import { Scoreboard } from '../scoreboard/scoreboard';

@Component({
  selector: 'app-partie-hote',
  imports: [RouterLink, Countdown, Scoreboard],
  templateUrl: './partie-hote.html',
  styleUrl: './partie-hote.scss',
})
export class PartieHote implements OnInit, OnDestroy {
  @ViewChild('audio') audio?: ElementRef<HTMLAudioElement>;
  public code: string = '';
  public game = signal<Game | null>(null);
  public status = signal<GameStatus>('LOBBY');
  public players = signal<PlayerJoined[]>([]);
  public round = signal<RoundStarted | null>(null);
  public endsAt = signal<number | null>(null);
  public answers = signal<AnswerCount | null>(null);
  public reveal = signal<RoundEnded | null>(null);
  public standings = signal<Standing[]>([]);
  public qrCode = signal<string>('');
  public starting = signal<boolean>(false);
  public audioBlocked = signal<boolean>(false);
  public errorMessage = signal<string>('');
  private streamSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private gameService: GameService,
    private streamService: GameStreamService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.code = (this.route.snapshot.paramMap.get('code') ?? '').toUpperCase();
    this.gameService.getGame(this.code).subscribe({
      next: (game) => this.load(game),
      error: (error: HttpErrorResponse) =>
        this.errorMessage.set(error.error?.detail ?? 'Partie introuvable'),
    });
  }

  ngOnDestroy(): void {
    this.streamSubscription?.unsubscribe();
    this.audio?.nativeElement.pause();
  }

  public joinUrl(): string {
    return location.origin + '/rejoindre/' + this.code;
  }

  public start() {
    this.starting.set(true);
    this.errorMessage.set('');
    this.gameService.start(this.code).subscribe({
      error: (error: HttpErrorResponse) => {
        this.starting.set(false);
        this.errorMessage.set(error.error?.detail ?? 'Impossible de démarrer la partie');
      },
    });
  }

  public playPreview() {
    this.gameService.preview(this.code).subscribe({
      next: (preview) => this.play(preview.previewUrl),
      error: () => this.errorMessage.set("L'extrait de cette manche est indisponible"),
    });
  }

  private load(game: Game) {
    const user = this.authService.currentUser();
    if (user === null || user.id !== game.hostId) {
      this.errorMessage.set("Seul l'animateur peut ouvrir cet écran.");
      return;
    }
    this.game.set(game);
    this.status.set(game.status);
    this.players.set(
      game.players.map((player) => ({
        playerId: player.id,
        nickname: player.nickname,
        guest: player.guest,
        playerCount: game.players.length,
      })),
    );
    this.standings.set(
      game.players
        .map((player) => ({ playerId: player.id, nickname: player.nickname, score: player.score }))
        .sort((a, b) => b.score - a.score),
    );
    QRCode.toDataURL(this.joinUrl(), { margin: 1, width: 240 })
      .then((url) => this.qrCode.set(url))
      .catch(() => this.qrCode.set(''));
    this.streamSubscription = this.streamService
      .events(this.code)
      .subscribe((event) => this.onEvent(event));
    if (game.status === 'PLAYING') {
      this.resume();
    }
  }

  private resume() {
    this.gameService.getState(this.code).subscribe((state) => {
      if (state.endsAt !== null) {
        this.endsAt.set(Date.parse(state.endsAt) - this.offset(state.serverTime));
        this.playPreview();
      }
    });
  }

  private onEvent(event: GameEvent) {
    switch (event.type) {
      case 'PLAYER_JOINED':
        this.players.update((players) => [...players, event.data]);
        break;
      case 'GAME_STARTED':
        this.status.set('PLAYING');
        this.starting.set(false);
        break;
      case 'ROUND_STARTED':
        this.round.set(event.data);
        this.reveal.set(null);
        this.answers.set(null);
        this.endsAt.set(Date.parse(event.data.endsAt) - this.offset(event.data.serverTime));
        this.playPreview();
        break;
      case 'ANSWER_COUNT':
        this.answers.set(event.data);
        break;
      case 'ROUND_ENDED':
        this.stopAudio();
        this.endsAt.set(null);
        this.reveal.set(event.data);
        this.standings.set(event.data.leaderboard);
        break;
      case 'GAME_FINISHED':
        this.stopAudio();
        this.status.set('FINISHED');
        this.standings.set(event.data.leaderboard);
        this.streamSubscription?.unsubscribe();
        break;
    }
  }

  private play(url: string) {
    const audio = this.audio?.nativeElement;
    if (audio === undefined) {
      return;
    }
    audio.src = url;
    audio
      .play()
      .then(() => this.audioBlocked.set(false))
      .catch(() => this.audioBlocked.set(true));
  }

  private stopAudio() {
    this.audio?.nativeElement.pause();
    this.audioBlocked.set(false);
  }

  private offset(serverTime: string): number {
    return Date.parse(serverTime) - Date.now();
  }
}
