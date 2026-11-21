import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { NgForm } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Connexion } from './connexion';

describe('Connexion', () => {
  let component: Connexion;
  let fixture: ComponentFixture<Connexion>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Connexion],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Connexion);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  function loginForm(): NgForm {
    return fixture.debugElement.query(By.directive(NgForm)).injector.get(NgForm);
  }

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid when empty', () => {
    expect(loginForm().invalid).toBeTrue();
  });

  it('should be valid with an email and a password', async () => {
    component.email = 'ana@example.com';
    component.password = 'motdepasse';
    fixture.detectChanges();
    await fixture.whenStable();
    expect(loginForm().valid).toBeTrue();
  });

  it('should display an error on wrong credentials', async () => {
    component.email = 'ana@example.com';
    component.password = 'mauvais';
    fixture.detectChanges();
    await fixture.whenStable();
    component.onSubmit(loginForm());
    httpTesting
      .expectOne('/api/auth/login')
      .flush({ detail: 'Identifiants invalides' }, { status: 401, statusText: 'Unauthorized' });
    fixture.detectChanges();
    expect(component.errorMessage()).toBe('Email ou mot de passe incorrect');
    expect(fixture.nativeElement.textContent).toContain('Email ou mot de passe incorrect');
  });
});
