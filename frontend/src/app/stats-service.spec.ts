import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HistoryEntry, LeaderboardEntry, Page } from './stats';
import { StatsService } from './stats-service';

describe('StatsService', () => {
  let service: StatsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(StatsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get a page of the history of a user', () => {
    let result: Page<HistoryEntry> | undefined;
    service.getHistory(7, 2, 10).subscribe((history) => (result = history));
    const request = httpTesting.expectOne('/api/users/7/history?page=2&size=10');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    const page = { content: [], page: 2, size: 10, totalElements: 20, totalPages: 2 };
    request.flush(page);
    expect(result).toEqual(page);
  });

  it('should get a page of the leaderboard', () => {
    let result: Page<LeaderboardEntry> | undefined;
    service.getLeaderboard(1, 5).subscribe((leaderboard) => (result = leaderboard));
    const request = httpTesting.expectOne('/api/leaderboard?page=1&size=5');
    expect(request.request.method).toBe('GET');
    const page = {
      content: [
        { userId: 1, name: 'Ana', gamesPlayed: 3, totalScore: 900, bestScore: 400, wins: 1 },
      ],
      page: 1,
      size: 5,
      totalElements: 6,
      totalPages: 2,
    };
    request.flush(page);
    expect(result).toEqual(page);
  });
});
