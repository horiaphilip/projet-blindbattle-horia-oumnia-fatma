import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth-service';
import { User } from './user';

function tokenExpiringIn(seconds: number): string {
  const payload = btoa(JSON.stringify({ sub: '1', exp: Math.floor(Date.now() / 1000) + seconds }));
  return 'header.' + payload + '.signature';
}

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;
  const ana: User = {
    id: 1,
    name: 'Ana',
    email: 'ana@example.com',
    createdAt: '',
    updatedAt: null,
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should log in and store the token and the user', () => {
    service = TestBed.inject(AuthService);
    const token = tokenExpiringIn(3600);
    service.login({ email: 'ana@example.com', password: 'motdepasse' }).subscribe();
    const request = httpTesting.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ email: 'ana@example.com', password: 'motdepasse' });
    request.flush({ token: token, user: ana });
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.currentUser()?.name).toBe('Ana');
    expect(service.token()).toBe(token);
    expect(localStorage.getItem('blindbattle.token')).toBe(token);
  });

  it('should clear everything on logout', () => {
    service = TestBed.inject(AuthService);
    service.login({ email: 'ana@example.com', password: 'motdepasse' }).subscribe();
    httpTesting.expectOne('/api/auth/login').flush({ token: tokenExpiringIn(3600), user: ana });
    service.logout();
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.token()).toBeNull();
    expect(localStorage.getItem('blindbattle.user')).toBeNull();
  });

  it('should return null for an expired token', () => {
    localStorage.setItem('blindbattle.token', tokenExpiringIn(-60));
    localStorage.setItem('blindbattle.user', JSON.stringify(ana));
    service = TestBed.inject(AuthService);
    expect(service.token()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should restore the stored user at creation', () => {
    localStorage.setItem('blindbattle.token', tokenExpiringIn(3600));
    localStorage.setItem('blindbattle.user', JSON.stringify(ana));
    service = TestBed.inject(AuthService);
    expect(service.isLoggedIn()).toBeTrue();
    expect(service.currentUser()?.email).toBe('ana@example.com');
  });

  it('should refresh the current user', () => {
    localStorage.setItem('blindbattle.token', tokenExpiringIn(3600));
    localStorage.setItem('blindbattle.user', JSON.stringify(ana));
    service = TestBed.inject(AuthService);
    service.refreshMe();
    httpTesting.expectOne('/api/users/me').flush({ ...ana, name: 'Ana Maria' });
    expect(service.currentUser()?.name).toBe('Ana Maria');
  });
});
