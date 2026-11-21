import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { Inscription } from './inscription';

describe('Inscription', () => {
  let component: Inscription;
  let fixture: ComponentFixture<Inscription>;
  let httpTesting: HttpTestingController;

  const valid = {
    name: 'Ana',
    email: 'ana@example.com',
    password: 'motdepasse',
    confirmPassword: 'motdepasse',
  };

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Inscription],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Inscription);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid with a wrong email', () => {
    component.inscriptionForm.patchValue({ ...valid, email: 'pas-un-email' });
    expect(component.inscriptionForm.valid).toBeFalse();
  });

  it('should be invalid with a short password', () => {
    component.inscriptionForm.patchValue({ ...valid, password: 'court', confirmPassword: 'court' });
    expect(component.inscriptionForm.controls.password.hasError('minlength')).toBeTrue();
  });

  it('should be invalid when passwords differ', () => {
    component.inscriptionForm.patchValue({ ...valid, confirmPassword: 'autrechose' });
    expect(component.inscriptionForm.hasError('passwordsMismatch')).toBeTrue();
    expect(component.inscriptionForm.valid).toBeFalse();
  });

  it('should be valid when everything is correct', () => {
    component.inscriptionForm.patchValue(valid);
    expect(component.inscriptionForm.valid).toBeTrue();
  });

  it('should create the account then log in without sending the confirmation', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate');
    component.inscriptionForm.patchValue(valid);
    component.onSubmit();
    const creation = httpTesting.expectOne('/api/users');
    expect(creation.request.body).toEqual({
      name: 'Ana',
      email: 'ana@example.com',
      password: 'motdepasse',
    });
    const user = { id: 7, name: 'Ana', email: 'ana@example.com', createdAt: '', updatedAt: null };
    creation.flush(user);
    const login = httpTesting.expectOne('/api/auth/login');
    expect(login.request.body).toEqual({ email: 'ana@example.com', password: 'motdepasse' });
    login.flush({ token: 'a.b.c', user: user });
    expect(router.navigate).toHaveBeenCalledWith(['/profil', 7]);
  });
});
