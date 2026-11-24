import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { Game, GameEvent, Standing } from '../game';
import { GameService } from '../game-service';
import { GameStreamService } from '../game-stream-service';
import { Jeu } from './jeu';

describe('Jeu', () => {
  let component: Jeu;
  let fixture: ComponentFixture<Jeu>;
  let httpTesting: HttpTestingController;
  let events: Subject<GameEvent>;
  let navigate: jasmine.Spy;
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
    players: [
      { id: 10, nickname: 'Bob', score: 0, guest: true },
      { id: 11, nickname: 'Zoé', score: 0, guest: true },
    ],
    createdAt: '',
  };

  function setUp(withPlayer: boolean) {
    sessionStorage.clear();
    events = new Subject<GameEvent>();
    TestBed.configureTestingModule({
      imports: [Jeu],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ code: 'ABC234' }) } },
        },
        { provide: GameStreamService, useValue: { events: () => events } },
      ],
    });
    if (withPlayer) {
      TestBed.inject(GameService).savePlayer('ABC234', {
        playerId: 10,
        nickname: 'Bob',
        playerToken: 'jeton',
      });
    }
    navigate = spyOn(TestBed.inject(Router), 'navigate');
    fixture = TestBed.createComponent(Jeu);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  function loadGame() {
    httpTesting.expectOne('/api/games/ABC234').flush(game);
    fixture.detectChanges();
  }

  function emit(event: GameEvent) {
    events.next(event);
    fixture.detectChanges();
  }

  function text(): string {
    return fixture.nativeElement.textContent;
  }

  function roundStarted(): GameEvent {
    const now = new Date().toISOString();
    return {
      type: 'ROUND_STARTED',
      data: {
        roundId: 4,
        roundNumber: 1,
        totalRounds: 3,
        startedAt: now,
        endsAt: new Date(Date.now() + 20000).toISOString(),
        serverTime: now,
        choices: ['Tube 1', 'Tube 2', 'Tube 3', 'Tube 4'],
      },
    };
  }

  function roundEnded(leaderboard: Standing[]): GameEvent {
    return {
      type: 'ROUND_ENDED',
      data: {
        roundId: 4,
        roundNumber: 1,
        totalRounds: 3,
        title: 'Get Lucky',
        artist: 'Daft Punk',
        coverUrl: null,
        fastest: 'Bob',
        leaderboard: leaderboard,
      },
    };
  }

  afterEach(() => {
    httpTesting.verify();
  });

  it('should send a player without token back to the join page', () => {
    setUp(false);
    expect(navigate).toHaveBeenCalledWith(['/rejoindre', 'ABC234']);
  });

  it('should wait in the lobby', () => {
    setUp(true);
    loadGame();
    expect(text()).toContain('Tu es dans la partie');
    expect(text()).toContain('2 joueurs');
    emit({
      type: 'PLAYER_JOINED',
      data: { playerId: 12, nickname: 'Léa', guest: true, playerCount: 3 },
    });
    expect(text()).toContain('3 joueurs');
  });

  it('should answer a round with the player token', () => {
    setUp(true);
    loadGame();
    emit(roundStarted());
    expect(text()).toContain('Manche 1 / 3');
    const buttons = fixture.nativeElement.querySelectorAll('button.choice');
    expect(buttons.length).toBe(4);
    buttons[1].click();
    const request = httpTesting.expectOne('/api/games/ABC234/answers');
    expect(request.request.body).toEqual({ playerToken: 'jeton', choiceIndex: 1 });
    request.flush({ eventId: 'e', roundNumber: 1 }, { status: 202, statusText: 'Accepted' });
    fixture.detectChanges();
    expect(text()).toContain('Réponse envoyée');
    expect(fixture.nativeElement.querySelectorAll('button.choice').length).toBe(0);
  });

  it('should let the player answer again when the answer is refused', () => {
    setUp(true);
    loadGame();
    emit(roundStarted());
    component.answer({ choiceIndex: 0 });
    httpTesting
      .expectOne('/api/games/ABC234/answers')
      .flush({ detail: 'Aucune manche en cours' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(text()).toContain('Aucune manche en cours');
    expect(component.sent()).toBeFalse();
  });

  it('should show a good answer with the points and the rank', () => {
    setUp(true);
    loadGame();
    emit(roundStarted());
    emit(
      roundEnded([
        {
          playerId: 10,
          nickname: 'Bob',
          score: 950,
          answered: true,
          correct: true,
          points: 950,
          streak: 2,
        },
        {
          playerId: 11,
          nickname: 'Zoé',
          score: 0,
          answered: false,
          correct: false,
          points: 0,
          streak: 0,
        },
      ]),
    );
    expect(text()).toContain('Bravo ! +950');
    expect(text()).toContain('Série de 2');
    expect(text()).toContain('Get Lucky');
    expect(text()).toContain('1er');
  });

  it('should show a missed round', () => {
    setUp(true);
    loadGame();
    emit(roundStarted());
    emit(
      roundEnded([
        {
          playerId: 11,
          nickname: 'Zoé',
          score: 900,
          answered: true,
          correct: true,
          points: 900,
          streak: 1,
        },
        {
          playerId: 10,
          nickname: 'Bob',
          score: 0,
          answered: false,
          correct: false,
          points: 0,
          streak: 0,
        },
      ]),
    );
    expect(text()).toContain('Trop tard');
    expect(text()).toContain('2e');
  });

  it('should show the final ranking', () => {
    setUp(true);
    loadGame();
    emit({
      type: 'GAME_FINISHED',
      data: {
        leaderboard: [
          { playerId: 11, nickname: 'Zoé', score: 2000 },
          { playerId: 10, nickname: 'Bob', score: 1500 },
        ],
      },
    });
    expect(text()).toContain('Partie terminée');
    expect(text()).toContain('2e sur 2');
    expect(fixture.nativeElement.querySelector('.row.me').textContent).toContain('Bob');
  });
});
