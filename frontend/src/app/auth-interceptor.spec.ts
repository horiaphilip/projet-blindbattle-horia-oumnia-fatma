import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { authInterceptor } from './auth-interceptor';
import { AuthService } from './auth-service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
  const token = 'header.' + payload + '.signature';

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  function logIn() {
    localStorage.setItem('blindbattle.token', token);
    localStorage.setItem(
      'blindbattle.user',
      JSON.stringify({
        id: 1,
        name: 'Ana',
        email: 'ana@example.com',
        createdAt: '',
        updatedAt: null,
      }),
    );
  }

  it('should add the token to api requests', () => {
    logIn();
    http.get('/api/users/me').subscribe();
    const request = httpTesting.expectOne('/api/users/me');
    expect(request.request.headers.get('Authorization')).toBe('Bearer ' + token);
    request.flush({});
  });

  it('should not add a header without a token', () => {
    http.get('/api/users').subscribe();
    const request = httpTesting.expectOne('/api/users');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush([]);
  });

  it('should not add a header to other urls', () => {
    logIn();
    http.get('https://api.deezer.com/chart').subscribe();
    const request = httpTesting.expectOne('https://api.deezer.com/chart');
    expect(request.request.headers.has('Authorization')).toBeFalse();
    request.flush({});
  });

  it('should log out on a 401 when a token was sent', () => {
    logIn();
    const authService = TestBed.inject(AuthService);
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    http.get('/api/users/me').subscribe({ error: () => {} });
    httpTesting.expectOne('/api/users/me').flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(authService.isLoggedIn()).toBeFalse();
    expect(router.navigate).toHaveBeenCalled();
  });
});
