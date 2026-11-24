import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Navigation } from './navigation';
import { AuthService } from '../auth-service';

describe('Navigation', () => {
  let component: Navigation;
  let fixture: ComponentFixture<Navigation>;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Navigation],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Navigation);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show Connexion when logged out', () => {
    const text: string = fixture.nativeElement.textContent;
    expect(text).toContain('Connexion');
    expect(text).toContain('Classement');
    expect(text).not.toContain('Déconnexion');
  });

  it('should show the name and Déconnexion when logged in', () => {
    const authService = TestBed.inject(AuthService);
    const httpTesting = TestBed.inject(HttpTestingController);
    authService.login({ email: 'ana@example.com', password: 'motdepasse' }).subscribe();
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    httpTesting.expectOne('/api/auth/login').flush({
      token: 'header.' + payload + '.signature',
      user: { id: 1, name: 'Ana', email: 'ana@example.com', createdAt: '', updatedAt: null },
    });
    fixture.detectChanges();
    const text: string = fixture.nativeElement.textContent;
    expect(text).toContain('Ana');
    expect(text).toContain('Déconnexion');
    expect(text).toContain('Mon profil');
  });

  it('should log out', () => {
    const authService = TestBed.inject(AuthService);
    spyOn(authService, 'logout');
    component.logout();
    expect(authService.logout).toHaveBeenCalled();
  });
});
