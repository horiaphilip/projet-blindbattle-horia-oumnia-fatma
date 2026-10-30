import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Joueurs } from './joueurs';

describe('Joueurs', () => {
  let component: Joueurs;
  let fixture: ComponentFixture<Joueurs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Joueurs],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Joueurs);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
