import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { QuizPartage } from './quiz-partage';

describe('QuizPartage', () => {
  let component: QuizPartage;
  let fixture: ComponentFixture<QuizPartage>;
  let httpTesting: HttpTestingController;
  const cover = 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [QuizPartage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ token: 'abc-123' }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(QuizPartage);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should display the shared quiz', () => {
    httpTesting.expectOne('/api/quizzes/shared/abc-123').flush({
      id: 5,
      title: 'Années 80',
      description: 'Les tubes',
      visibility: 'PRIVATE',
      shareToken: 'abc-123',
      ownerId: 1,
      ownerName: 'Ana',
      trackCount: 1,
      playableTrackCount: 0,
      createdAt: '',
      updatedAt: null,
      tracks: [
        {
          position: 1,
          trackId: 11,
          title: 'Titre A',
          artist: 'Artiste A',
          coverUrl: cover,
          hasPreview: false,
        },
      ],
    });
    fixture.detectChanges();
    const text: string = fixture.nativeElement.textContent;
    expect(component).toBeTruthy();
    expect(text).toContain('Années 80');
    expect(text).toContain('Les tubes');
    expect(text).toContain('Ana');
    expect(text).toContain('Titre A');
    expect(text).toContain("pas d'extrait");
    expect(fixture.nativeElement.querySelector('img').src).toBe(cover);
    expect(fixture.nativeElement.querySelector('button')).toBeNull();
  });

  it('should show an error for an unknown link', () => {
    httpTesting
      .expectOne('/api/quizzes/shared/abc-123')
      .flush(null, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain("Ce quiz n'existe pas");
  });

  it('should show another error when the server fails', () => {
    httpTesting
      .expectOne('/api/quizzes/shared/abc-123')
      .flush(null, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Impossible de charger le quiz');
  });
});
