import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GameSettings } from './game';
import { GameService } from './game-service';

describe('GameService', () => {
  let service: GameService;
  let httpTesting: HttpTestingController;
  const settings: GameSettings = {
    answerMode: 'QUICK_CHOICE',
    guessTarget: 'TITLE',
    roundCount: 5,
    roundSeconds: 20,
    difficulty: 'EASY',
    themeId: 'chart-world',
    quizId: null,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GameService);
    httpTesting = TestBed.inject(HttpTestingController);
    sessionStorage.clear();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should create a game with its settings', () => {
    service.createGame(settings).subscribe();
    const request = httpTesting.expectOne('/api/games');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ settings: settings });
    request.flush({});
  });

  it('should read a game and its state with the code in upper case', () => {
    service.getGame('abc234').subscribe();
    service.getState('abc234').subscribe();
    httpTesting.expectOne('/api/games/ABC234').flush({});
    httpTesting.expectOne('/api/games/ABC234/state').flush({});
  });

  it('should join, start and answer', () => {
    service.join('ABC234', 'Ana').subscribe();
    const join = httpTesting.expectOne('/api/games/ABC234/players');
    expect(join.request.method).toBe('POST');
    expect(join.request.body).toEqual({ nickname: 'Ana' });
    join.flush({});

    service.start('ABC234').subscribe();
    expect(httpTesting.expectOne('/api/games/ABC234/start').request.method).toBe('POST');

    service.answer('ABC234', { playerToken: 'jeton', choiceIndex: 2 }).subscribe();
    const answer = httpTesting.expectOne('/api/games/ABC234/answers');
    expect(answer.request.body).toEqual({ playerToken: 'jeton', choiceIndex: 2 });
    answer.flush(null);
  });

  it('should get the preview of the current round and the themes', () => {
    service.preview('ABC234').subscribe();
    service.getThemes().subscribe();
    httpTesting.expectOne('/api/games/ABC234/rounds/current/preview').flush({});
    httpTesting.expectOne('/api/themes').flush([]);
  });

  it('should remember the player of a game', () => {
    expect(service.player('ABC234')).toBeNull();
    service.savePlayer('ABC234', { playerId: 7, nickname: 'Ana', playerToken: 'jeton' });
    expect(service.player('ABC234')).toEqual({
      playerId: 7,
      nickname: 'Ana',
      playerToken: 'jeton',
    });
    expect(service.player('ZZZZZZ')).toBeNull();
  });
});
