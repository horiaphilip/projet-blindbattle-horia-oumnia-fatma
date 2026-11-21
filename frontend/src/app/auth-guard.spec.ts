import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import {
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { authGuard } from './auth-guard';

describe('authGuard', () => {
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/profil' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow a logged in user', () => {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    localStorage.setItem('blindbattle.token', 'header.' + payload + '.signature');
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
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result).toBeTrue();
  });

  it('should redirect to the login page otherwise', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result instanceof UrlTree).toBeTrue();
    expect((result as UrlTree).toString()).toBe('/connexion?returnUrl=%2Fprofil');
  });
});
