import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserService } from './user-service';

describe('UserService', () => {
  let service: UserService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should post a new user', () => {
    service.createUser({ name: 'Ana', email: 'ana@example.com' }).subscribe((user) => {
      expect(user.id).toBe(1);
    });
    const request = httpTesting.expectOne('/api/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ name: 'Ana', email: 'ana@example.com' });
    request.flush({ id: 1, name: 'Ana', email: 'ana@example.com', createdAt: '', updatedAt: null });
  });

  it('should get the stats of a user', () => {
    service.getStats(1).subscribe((stats) => {
      expect(stats.badges[0].code).toBe('WELCOME');
    });
    httpTesting.expectOne('/api/users/1/stats').flush({
      userId: 1,
      gamesPlayed: 0,
      totalScore: 0,
      bestScore: 0,
      badges: [{ code: 'WELCOME', earnedAt: '' }],
    });
  });
});
