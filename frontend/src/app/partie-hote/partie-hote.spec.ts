import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { Subject } from 'rxjs';
import { AuthService } from '../auth-service';
import { Game, GameEvent } from '../game';
import { GameStreamService } from '../game-stream-service';
import { PartieHote } from './partie-hote';

describe('PartieHote', () => {
  let component: PartieHote;
  let fixture: ComponentFixture<PartieHote>;
  let httpTesting: HttpTestingController;
  let events: Subject<GameEvent>;
  let play: jasmine.Spy;
  const user = signal<{ id: number } | null>({ id: 1 });
  const game: Game = {
    code: 'ABC234',
    status: 'LOBBY',
    settings: {
      answerMode: 'QUICK_CHOICE',
      guessTarget: 'TITLE',
      roundCount: 3,
      roundSeconds: 20,
      difficulty: 'EASY',
      themeId: 'chart-world',
      quizId: null,
    },
    hostId: 1,
    hostName: 'Ana',
    players: [{ id: 10, nickname: 'Bob', score: 0, guest: true }],
    createdAt: '',
  };

  function text(): string {
    return fixture.nativeElement.textContent;
  }

  function emit(event: GameEvent) {
    events.next(event);
    fixture.detectChanges();
  }

  function roundStarted(): GameEvent {
    const now = new Date();
    return {
      type: 'ROUND_STARTED',
      data: {
        roundId: 4,
        roundNumber: 1,
        totalRounds: 3,
        startedAt: now.toISOString(),
        endsAt: new Date(now.getTime() + 20000).toISOString(),
        serverTime: now.toISOString(),
        choices: ['Tube 1', 'Tube 2', 'Tube 3', 'Tube 4'],
      },
    };
  }

  beforeEach(() => {
    events = new Subject<GameEvent>();
    user.set({ id: 1 });
    play = spyOn(HTMLMediaElement.prototype, 'play').and.resolveTo();
    spyOn(HTMLMediaElement.prototype, 'pause');
    TestBed.configureTestingModule({
      imports: [PartieHote],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ code: 'abc234' }) } },
        },
        { provide: AuthService, useValue: { currentUser: user } },
        { provide: GameStreamService, useValue: { events: () => events } },
      ],
    });
    fixture = TestBed.createComponent(PartieHote);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  function loadGame(value: Game = game) {
    httpTesting.expectOne('/api/games/ABC234').flush(value);
    fixture.detectChanges();
  }

  it('should show the lobby with the code and the players', () => {
    loadGame();
    expect(text()).toContain('ABC234');
    expect(text()).toContain('/rejoindre/ABC234');
    expect(text()).toContain('Bob');
    emit({
      type: 'PLAYER_JOINED',
      data: { playerId: 11, nickname: 'Zoé', guest: false, playerCount: 2 },
    });
    expect(text()).toContain('Zoé');
    expect(text()).toContain('Joueurs (2)');
  });

  it('should refuse a user who is not the host', () => {
    user.set({ id: 99 });
    loadGame();
    expect(text()).toContain("Seul l'animateur peut ouvrir cet écran");
    expect(text()).not.toContain('Démarrer la partie');
  });

  it('should start the game', () => {
    loadGame();
    fixture.nativeElement.querySelector('button.start').click();
    const request = httpTesting.expectOne('/api/games/ABC234/start');
    expect(request.request.method).toBe('POST');
    request.flush({});
    emit({ type: 'GAME_STARTED', data: { totalRounds: 3 } });
    expect(component.status()).toBe('PLAYING');
  });

  it('should show why the game cannot start', () => {
    loadGame();
    component.start();
    httpTesting
      .expectOne('/api/games/ABC234/start')
      .flush({ detail: 'Pas assez de morceaux' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(text()).toContain('Pas assez de morceaux');
  });

  it('should play the preview and show the choices of a round', () => {
    loadGame({ ...game, status: 'PLAYING' });
    httpTesting.expectOne('/api/games/ABC234/state').flush({ endsAt: null, serverTime: '' });
    emit(roundStarted());
    httpTesting
      .expectOne('/api/games/ABC234/rounds/current/preview')
      .flush({ roundId: 4, roundNumber: 1, previewUrl: 'https://cdn.example/1.mp3' });
    expect(play).toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('audio').src).toBe('https://cdn.example/1.mp3');
    expect(text()).toContain('Manche 1 / 3');
    expect(text()).toContain('Tube 3');
    emit({ type: 'ANSWER_COUNT', data: { roundId: 4, answered: 1, players: 2 } });
    expect(text()).toContain('1 / 2 réponses');
  });

  it('should reveal the track at the end of a round', () => {
    loadGame({ ...game, status: 'PLAYING' });
    httpTesting.expectOne('/api/games/ABC234/state').flush({ endsAt: null, serverTime: '' });
    emit({
      type: 'ROUND_ENDED',
      data: {
        roundId: 4,
        roundNumber: 1,
        totalRounds: 3,
        title: 'Get Lucky',
        artist: 'Daft Punk',
        coverUrl: null,
        fastest: 'Bob',
        leaderboard: [
          { playerId: 10, nickname: 'Bob', score: 900, answered: true, correct: true, points: 900 },
        ],
      },
    });
    expect(text()).toContain('Get Lucky');
    expect(text()).toContain('Daft Punk');
    expect(text()).toContain('Plus rapide : Bob');
    expect(text()).toContain('+900');
  });

  it('should show the podium at the end of the game', () => {
    loadGame({ ...game, status: 'PLAYING' });
    httpTesting.expectOne('/api/games/ABC234/state').flush({ endsAt: null, serverTime: '' });
    emit({
      type: 'GAME_FINISHED',
      data: {
        leaderboard: [
          { playerId: 10, nickname: 'Bob', score: 2000 },
          { playerId: 11, nickname: 'Zoé', score: 1500 },
        ],
      },
    });
    expect(text()).toContain('Partie terminée');
    const steps = fixture.nativeElement.querySelectorAll('.step');
    expect(steps.length).toBe(2);
    expect(steps[0].textContent).toContain('Bob');
  });
});
