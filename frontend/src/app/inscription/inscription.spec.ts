import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Inscription } from './inscription';

describe('Inscription', () => {
  let component: Inscription;
  let fixture: ComponentFixture<Inscription>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Inscription],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Inscription);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be invalid with a wrong email', () => {
    component.inscriptionForm.patchValue({ name: 'Ana', email: 'pas-un-email' });
    expect(component.inscriptionForm.valid).toBeFalse();
  });

  it('should be valid with a name and an email', () => {
    component.inscriptionForm.patchValue({ name: 'Ana', email: 'ana@example.com' });
    expect(component.inscriptionForm.valid).toBeTrue();
  });
});
