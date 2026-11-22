import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Joueurs } from './joueurs';

describe('Joueurs', () => {
  let component: Joueurs;
  let fixture: ComponentFixture<Joueurs>;
  let httpTesting: HttpTestingController;
  const players = [
    { id: 1, name: 'Ana', email: 'ana@example.com', createdAt: '', updatedAt: null },
    { id: 2, name: 'Tom', email: 'tom@example.com', createdAt: '', updatedAt: null },
  ];

  function setUp() {
    TestBed.configureTestingModule({
      imports: [Joueurs],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(Joueurs);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    httpTesting.expectOne('/api/users').flush(players);
    fixture.detectChanges();
  }

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should create', () => {
    setUp();
    expect(component).toBeTruthy();
  });

  it('should hide every delete button when logged out', () => {
    setUp();
    expect(fixture.nativeElement.querySelectorAll('button.danger').length).toBe(0);
  });

  it('should show the delete button only on my row', () => {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    localStorage.setItem('blindbattle.token', 'header.' + payload + '.signature');
    localStorage.setItem('blindbattle.user', JSON.stringify(players[1]));
    setUp();
    const buttons = fixture.nativeElement.querySelectorAll('button.danger');
    expect(buttons.length).toBe(1);
    expect(component.isMe(players[1])).toBeTrue();
    expect(component.isMe(players[0])).toBeFalse();
  });
});
