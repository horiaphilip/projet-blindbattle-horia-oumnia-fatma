import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Classement } from './classement';

describe('Classement', () => {
  let component: Classement;
  let fixture: ComponentFixture<Classement>;
  let httpTesting: HttpTestingController;
  const names = ['Ana', 'Tom', 'Léa', 'Max'];
  const firstPage = {
    content: names.map((name, index) => ({
      userId: index + 1,
      name: name,
      gamesPlayed: 5,
      totalScore: 4000 - index * 500,
      bestScore: 1200,
      wins: 2,
    })),
    page: 0,
    size: 10,
    totalElements: 12,
    totalPages: 2,
  };
  const secondPage = {
    ...firstPage,
    content: [
      { userId: 11, name: 'Zoé', gamesPlayed: 1, totalScore: 100, bestScore: 100, wins: 0 },
    ],
    page: 1,
  };

  function setUp() {
    TestBed.configureTestingModule({
      imports: [Classement],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(Classement);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  function flushPage(page: typeof firstPage) {
    httpTesting.expectOne('/api/leaderboard?page=' + page.page + '&size=10').flush(page);
    fixture.detectChanges();
  }

  function rows(): HTMLElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('tbody tr'));
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
    flushPage(firstPage);
    expect(component).toBeTruthy();
  });

  it('should display the rows of the page', () => {
    setUp();
    flushPage(firstPage);
    expect(rows().length).toBe(4);
    expect(rows()[0].textContent).toContain('Ana');
    expect(rows()[0].textContent).toContain('4000');
    expect(rows()[3].querySelector('a')?.getAttribute('href')).toBe('/profil/4');
  });

  it('should request the next page', () => {
    setUp();
    flushPage(firstPage);
    fixture.nativeElement.querySelector('button.next').click();
    flushPage(secondPage);
    expect(rows().length).toBe(1);
    expect(rows()[0].textContent).toContain('11');
    expect(rows()[0].textContent).toContain('Zoé');
  });

  it('should show the podium on the first page only', () => {
    setUp();
    flushPage(firstPage);
    const steps = fixture.nativeElement.querySelectorAll('.podium .step');
    expect(steps.length).toBe(3);
    expect(steps[0].classList).toContain('place-1');
    expect(steps[0].textContent).toContain('Ana');
    component.loadPage(1);
    flushPage(secondPage);
    expect(fixture.nativeElement.querySelector('.podium')).toBeNull();
  });

  it('should highlight the row of the logged-in user', () => {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    localStorage.setItem('blindbattle.token', 'header.' + payload + '.signature');
    localStorage.setItem(
      'blindbattle.user',
      JSON.stringify({ id: 2, name: 'Tom', email: 'tom@example.com' }),
    );
    setUp();
    flushPage(firstPage);
    const highlighted = fixture.nativeElement.querySelectorAll('tbody tr.me');
    expect(highlighted.length).toBe(1);
    expect(highlighted[0].textContent).toContain('Tom');
  });

  it('should not highlight any row when logged out', () => {
    setUp();
    flushPage(firstPage);
    expect(fixture.nativeElement.querySelector('.me')).toBeNull();
  });

  it('should show a message when nobody has played', () => {
    setUp();
    flushPage({ ...firstPage, content: [], totalElements: 0, totalPages: 0 });
    expect(fixture.nativeElement.textContent).toContain("Aucune partie jouée pour l'instant");
  });

  it('should show an error when the server does not answer', () => {
    setUp();
    httpTesting
      .expectOne('/api/leaderboard?page=0&size=10')
      .flush(null, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Impossible de charger le classement');
  });
});
