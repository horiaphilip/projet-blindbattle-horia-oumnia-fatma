import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { NouvellePartie } from './nouvelle-partie';

describe('NouvellePartie', () => {
  let component: NouvellePartie;
  let fixture: ComponentFixture<NouvellePartie>;
  let httpTesting: HttpTestingController;
  const themes = [
    { id: 'chart-world', name: 'Top Monde', category: 'Classements', coverUrl: null },
    { id: 'genre-rock', name: 'Rock', category: 'Genres', coverUrl: null },
  ];
  const quizzes = {
    content: [
      { id: 5, title: 'Années 80', visibility: 'PRIVATE', trackCount: 8, createdAt: '' },
      { id: 6, title: 'Vide', visibility: 'PRIVATE', trackCount: 0, createdAt: '' },
    ],
    page: 0,
    size: 50,
    totalElements: 2,
    totalPages: 1,
  };

  function load() {
    httpTesting.expectOne('/api/themes').flush(themes);
    httpTesting.expectOne('/api/quizzes?page=0&size=50').flush(quizzes);
    fixture.detectChanges();
  }

  function submit() {
    fixture.nativeElement.querySelector('button[type=submit]').click();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NouvellePartie],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    fixture = TestBed.createComponent(NouvellePartie);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should list the themes and only the quizzes with tracks', () => {
    load();
    fixture.nativeElement.querySelector('mat-select').click();
    fixture.detectChanges();
    const options = Array.from(document.querySelectorAll('mat-option')).map((option) =>
      option.textContent?.trim(),
    );
    expect(options).toEqual(['Top Monde', 'Rock']);
    expect(component.quizzes().map((quiz) => quiz.id)).toEqual([5]);
  });

  it('should create a theme game and open the host screen', () => {
    load();
    const navigate = spyOn(TestBed.inject(Router), 'navigate');
    component.gameForm.patchValue({ themeId: 'genre-rock', roundCount: 8, answerMode: 'TYPE_IT' });
    fixture.detectChanges();
    submit();
    const request = httpTesting.expectOne('/api/games');
    expect(request.request.body.settings).toEqual({
      answerMode: 'TYPE_IT',
      guessTarget: 'TITLE',
      roundCount: 8,
      roundSeconds: 20,
      difficulty: 'EASY',
      themeId: 'genre-rock',
      quizId: null,
    });
    request.flush({ code: 'ABC234' });
    expect(navigate).toHaveBeenCalledWith(['/host', 'ABC234']);
  });

  it('should require a quiz when the source is a quiz', () => {
    load();
    component.gameForm.patchValue({ source: 'QUIZ' });
    expect(component.gameForm.invalid).toBeTrue();
    component.gameForm.patchValue({ quizId: 5 });
    expect(component.gameForm.valid).toBeTrue();
    component.onSubmit();
    const request = httpTesting.expectOne('/api/games');
    expect(request.request.body.settings.quizId).toBe(5);
    expect(request.request.body.settings.themeId).toBeNull();
    request.flush({ code: 'QUIZ23' });
  });

  it('should refuse a round count out of range', () => {
    load();
    component.gameForm.patchValue({ roundCount: 25 });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('button[type=submit]').disabled).toBeTrue();
  });

  it('should show the error returned by the server', () => {
    load();
    submit();
    httpTesting
      .expectOne('/api/games')
      .flush(
        { detail: 'Requête invalide', errors: { 'settings.roundCount': '20 manches maximum' } },
        { status: 400, statusText: 'Bad Request' },
      );
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('20 manches maximum');
  });
});
