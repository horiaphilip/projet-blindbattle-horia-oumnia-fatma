import { ComponentFixture, TestBed, fakeAsync, flush } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MesQuiz } from './mes-quiz';

describe('MesQuiz', () => {
  let component: MesQuiz;
  let fixture: ComponentFixture<MesQuiz>;
  let httpTesting: HttpTestingController;
  const quizzes = {
    content: [
      {
        id: 5,
        title: 'Années 80',
        visibility: 'PUBLIC',
        trackCount: 12,
        createdAt: '2026-11-20T12:00:00Z',
      },
      {
        id: 6,
        title: 'Rap français',
        visibility: 'PRIVATE',
        trackCount: 1,
        createdAt: '2026-11-18T12:00:00Z',
      },
    ],
    page: 0,
    size: 10,
    totalElements: 12,
    totalPages: 2,
  };

  function flushPage(page: typeof quizzes) {
    httpTesting.expectOne('/api/quizzes?page=' + page.page + '&size=10').flush(page);
    fixture.detectChanges();
  }

  function cards(): HTMLElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('.quiz'));
  }

  function click(selector: string) {
    fixture.nativeElement.querySelector(selector).click();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [MesQuiz],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } },
      ],
    });
    fixture = TestBed.createComponent(MesQuiz);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should create', () => {
    flushPage(quizzes);
    expect(component).toBeTruthy();
  });

  it('should display my quizzes', () => {
    flushPage(quizzes);
    expect(cards().length).toBe(2);
    expect(cards()[0].textContent).toContain('Années 80');
    expect(cards()[0].textContent).toContain('12 morceaux');
    expect(cards()[0].textContent).toContain('Public');
    expect(cards()[0].textContent).toContain('20/11/2026');
    expect(cards()[0].querySelector('a.edit')?.getAttribute('href')).toBe('/quiz/5/edit');
    expect(cards()[1].textContent).toContain('1 morceau ');
    expect(cards()[1].textContent).toContain('Privé');
  });

  it('should show a message without any quiz', () => {
    flushPage({ ...quizzes, content: [], totalElements: 0, totalPages: 0 });
    expect(fixture.nativeElement.textContent).toContain("Aucun quiz pour l'instant");
  });

  it('should request the next page', () => {
    flushPage(quizzes);
    click('button.next');
    flushPage({ ...quizzes, content: [quizzes.content[1]], page: 1 });
    expect(cards().length).toBe(1);
  });

  it('should delete a quiz after confirmation', () => {
    flushPage(quizzes);
    const confirmSpy = spyOn(window, 'confirm').and.returnValue(true);
    click('button.danger');
    expect(confirmSpy).toHaveBeenCalled();
    const request = httpTesting.expectOne('/api/quizzes/5');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    flushPage({ ...quizzes, content: [quizzes.content[1]], totalElements: 1, totalPages: 1 });
    expect(cards().length).toBe(1);
    expect(cards()[0].textContent).toContain('Rap français');
  });

  it('should not delete a quiz without confirmation', () => {
    flushPage(quizzes);
    spyOn(window, 'confirm').and.returnValue(false);
    click('button.danger');
    httpTesting.expectNone('/api/quizzes/5');
    expect(cards().length).toBe(2);
  });

  it('should go back to the previous page after deleting the last quiz of a page', () => {
    flushPage(quizzes);
    component.loadPage(1);
    flushPage({ ...quizzes, content: [quizzes.content[1]], page: 1 });
    spyOn(window, 'confirm').and.returnValue(true);
    click('button.danger');
    httpTesting.expectOne('/api/quizzes/6').flush(null, { status: 204, statusText: 'No Content' });
    flushPage(quizzes);
    expect(cards().length).toBe(2);
  });

  it('should show the error of a refused deletion', () => {
    flushPage(quizzes);
    spyOn(window, 'confirm').and.returnValue(true);
    click('button.danger');
    httpTesting
      .expectOne('/api/quizzes/5')
      .flush(
        { detail: 'Ce quiz ne vous appartient pas' },
        { status: 403, statusText: 'Forbidden' },
      );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Ce quiz ne vous appartient pas');
  });

  it('should copy the share link', fakeAsync(() => {
    flushPage(quizzes);
    const writeText = spyOn(navigator.clipboard, 'writeText').and.resolveTo();
    click('button.share');
    httpTesting.expectOne('/api/quizzes/5').flush({ id: 5, shareToken: 'abc-123' });
    flush();
    expect(writeText).toHaveBeenCalledOnceWith(location.origin + '/quiz/partage/abc-123');
    expect(TestBed.inject(MatSnackBar).open).toHaveBeenCalledWith(
      'Lien de partage copié',
      'OK',
      jasmine.anything(),
    );
  }));

  it('should show the share link when the clipboard is refused', fakeAsync(() => {
    flushPage(quizzes);
    spyOn(navigator.clipboard, 'writeText').and.rejectWith(new Error());
    click('button.share');
    httpTesting.expectOne('/api/quizzes/5').flush({ id: 5, shareToken: 'abc-123' });
    flush();
    expect(TestBed.inject(MatSnackBar).open).toHaveBeenCalledWith(
      'Lien de partage : ' + location.origin + '/quiz/partage/abc-123',
      'OK',
    );
  }));

  it('should show an error when the quizzes cannot be loaded', () => {
    httpTesting
      .expectOne('/api/quizzes?page=0&size=10')
      .flush(null, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Impossible de charger tes quiz');
  });
});
