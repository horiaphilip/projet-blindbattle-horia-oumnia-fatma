import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { Profil } from './profil';

describe('Profil', () => {
  let component: Profil;
  let fixture: ComponentFixture<Profil>;
  let httpTesting: HttpTestingController;
  const ana = { id: 1, name: 'Ana', email: 'ana@example.com', createdAt: '', updatedAt: null };
  const stats = { userId: 1, gamesPlayed: 0, totalScore: 0, bestScore: 0, badges: [] };
  const emptyHistory = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 };
  const history = {
    content: [
      {
        gameCode: 'ABC123',
        playedAt: '2026-11-20T18:00:00Z',
        rank: 2,
        playerCount: 5,
        finalScore: 1450,
        correctAnswers: 3,
        totalRounds: 5,
      },
      {
        gameCode: 'XYZ789',
        playedAt: '2026-11-18T18:00:00Z',
        rank: 1,
        playerCount: 4,
        finalScore: 2100,
        correctAnswers: 5,
        totalRounds: 5,
      },
    ],
    page: 0,
    size: 10,
    totalElements: 12,
    totalPages: 2,
  };

  function setUp(id: string | null) {
    TestBed.configureTestingModule({
      imports: [Profil],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideCharts(withDefaultRegisterables()),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(id === null ? {} : { id: id }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(Profil);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  function expectHistory(id: number, page: number) {
    return httpTesting.expectOne('/api/users/' + id + '/history?page=' + page + '&size=10');
  }

  function showProfile(historyPage: object, userStats: object = stats) {
    setUp('1');
    httpTesting.expectOne('/api/users/1').flush(ana);
    expectHistory(1, 0).flush(historyPage);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(userStats);
    tick(0);
    fixture.detectChanges();
  }

  function rows(): string[] {
    const elements: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('tbody tr'));
    return elements.map((element) => element.textContent ?? '');
  }

  function logInAsAna() {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    localStorage.setItem('blindbattle.token', 'header.' + payload + '.signature');
    localStorage.setItem('blindbattle.user', JSON.stringify(ana));
  }

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    component.ngOnDestroy();
    httpTesting.verify();
    localStorage.clear();
  });

  it('should create', fakeAsync(() => {
    setUp('1');
    httpTesting.expectOne('/api/users/1').flush(ana);
    expectHistory(1, 0).flush(emptyHistory);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(stats);
    tick(0);
    expect(component).toBeTruthy();
  }));

  it('should load my own profile without an id', fakeAsync(() => {
    logInAsAna();
    setUp(null);
    httpTesting.expectOne('/api/users/1').flush(ana);
    expectHistory(1, 0).flush(emptyHistory);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(stats);
    tick(0);
    expect(component.isMine()).toBeTrue();
  }));

  it('should not offer to edit the profile of someone else', fakeAsync(() => {
    logInAsAna();
    setUp('2');
    httpTesting.expectOne('/api/users/2').flush({ ...ana, id: 2, name: 'Tom' });
    expectHistory(2, 0).flush(emptyHistory);
    tick(0);
    httpTesting.expectOne('/api/users/2/stats').flush({ ...stats, userId: 2 });
    tick(0);
    fixture.detectChanges();
    expect(component.isMine()).toBeFalse();
    expect(fixture.nativeElement.textContent).not.toContain('Modifier mon profil');
  }));

  it('should fill the form and show a conflict error', fakeAsync(() => {
    logInAsAna();
    setUp(null);
    httpTesting.expectOne('/api/users/1').flush(ana);
    expectHistory(1, 0).flush(emptyHistory);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(stats);
    tick(0);
    component.startEditing();
    expect(component.editForm.getRawValue()).toEqual({ name: 'Ana', email: 'ana@example.com' });
    component.editForm.patchValue({ email: 'tom@example.com' });
    component.saveProfile();
    const request = httpTesting.expectOne('/api/users/1');
    expect(request.request.method).toBe('PUT');
    request.flush({ detail: 'Email déjà utilisé' }, { status: 409, statusText: 'Conflict' });
    expect(component.editError()).toBe('Email déjà utilisé');
  }));

  it('should display the history', fakeAsync(() => {
    showProfile(history);
    expect(rows().length).toBe(2);
    expect(rows()[0]).toContain('20/11/2026');
    expect(rows()[0]).toContain('ABC123');
    expect(rows()[0]).toContain('2e / 5');
    expect(rows()[0]).toContain('1450');
    expect(rows()[0]).toContain('3 / 5 bonnes réponses');
    expect(rows()[1]).toContain('1er / 4');
    expect(fixture.nativeElement.querySelector('canvas')).not.toBeNull();
  }));

  it('should show a message when the history is empty', fakeAsync(() => {
    showProfile(emptyHistory);
    expect(fixture.nativeElement.textContent).toContain("Aucune partie jouée pour l'instant");
    expect(rows().length).toBe(0);
    expect(fixture.nativeElement.querySelector('canvas')).toBeNull();
  }));

  it('should load the next history page and keep the chart on the last games', fakeAsync(() => {
    showProfile(history);
    fixture.nativeElement.querySelector('button.next').click();
    const olderGame = { ...history.content[0], gameCode: 'OLD111' };
    expectHistory(1, 1).flush({ ...history, content: [olderGame], page: 1 });
    fixture.detectChanges();
    expect(rows().length).toBe(1);
    expect(rows()[0]).toContain('OLD111');
    expect(component.recentGames().length).toBe(2);
  }));

  it('should show an error when the history cannot be loaded', fakeAsync(() => {
    showProfile(history);
    component.loadHistory(1, 1);
    expectHistory(1, 1).flush(null, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain("Impossible de charger l'historique");
  }));

  it('should display the badges and the average score', fakeAsync(() => {
    showProfile(emptyHistory, {
      userId: 1,
      gamesPlayed: 3,
      totalScore: 1000,
      bestScore: 600,
      badges: [{ code: 'WELCOME', earnedAt: '2026-11-05T10:00:00Z' }],
    });
    const text: string = fixture.nativeElement.textContent;
    expect(component.averageScore()).toBe(333);
    expect(text).toContain('333');
    expect(text).toContain('Bienvenue dans BlindBattle');
    expect(text).toContain('05/11/2026');
  }));

  it('should have an average of zero without any game', fakeAsync(() => {
    showProfile(emptyHistory);
    expect(component.averageScore()).toBe(0);
  }));
});
