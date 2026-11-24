import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { QuizEditor } from './quiz-editor';
import { TrackResult } from '../quiz';

describe('QuizEditor', () => {
  let component: QuizEditor;
  let fixture: ComponentFixture<QuizEditor>;
  let httpTesting: HttpTestingController;
  let navigate: jasmine.Spy;
  const quiz = {
    id: 5,
    title: 'Années 80',
    description: 'Les tubes',
    visibility: 'PUBLIC',
    shareToken: 'abc-123',
    ownerId: 1,
    ownerName: 'Ana',
    trackCount: 2,
    playableTrackCount: 1,
    createdAt: '',
    updatedAt: null,
    tracks: [
      {
        position: 1,
        trackId: 11,
        title: 'Titre A',
        artist: 'Artiste A',
        coverUrl: '',
        hasPreview: true,
      },
      {
        position: 2,
        trackId: 12,
        title: 'Titre B',
        artist: 'Artiste B',
        coverUrl: '',
        hasPreview: false,
      },
    ],
  };

  function result(id: number, hasPreview: boolean = true): TrackResult {
    return {
      id: id,
      deezerId: id * 100,
      title: 'Morceau ' + id,
      artist: 'Artiste ' + id,
      album: 'Album ' + id,
      coverUrl: null,
      rank: 1,
      durationSeconds: 185,
      hasPreview: hasPreview,
    };
  }

  function setUp(id: string | null) {
    TestBed.configureTestingModule({
      imports: [QuizEditor],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: MatSnackBar, useValue: { open: jasmine.createSpy('open') } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(id === null ? {} : { id: id }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(QuizEditor);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    fixture.detectChanges();
  }

  function setUpEdition() {
    setUp('5');
    httpTesting.expectOne('/api/quizzes/5').flush(quiz);
    fixture.detectChanges();
  }

  function trackIds(): number[] {
    return component.tracks.getRawValue().map((track) => track.trackId);
  }

  function selected(): HTMLElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('.selected .track'));
  }

  function searchRequest(query: string) {
    return httpTesting.expectOne(
      (req) => req.url === '/api/tracks/search' && req.params.get('q') === query,
    );
  }

  function search(query: string) {
    component.searchControl.setValue(query);
    tick(300);
    return searchRequest(query);
  }

  function submitButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[type=submit]');
  }

  afterEach(() => {
    httpTesting.verify();
  });

  it('should create', () => {
    setUp(null);
    expect(component).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Nouveau quiz');
  });

  it('should be invalid without a title', () => {
    setUp(null);
    component.addTrack(result(1));
    expect(component.quizForm.invalid).toBeTrue();
    component.quizForm.patchValue({ title: '   ' });
    expect(component.quizForm.invalid).toBeTrue();
    component.quizForm.patchValue({ title: 'Mon quiz' });
    expect(component.quizForm.valid).toBeTrue();
  });

  it('should be invalid without tracks', () => {
    setUp(null);
    component.quizForm.patchValue({ title: 'Mon quiz' });
    expect(component.quizForm.invalid).toBeTrue();
    expect(submitButton().disabled).toBeTrue();
    component.onSubmit();
    httpTesting.expectNone('/api/quizzes');
  });

  it('should refuse a title or a description that is too long', () => {
    setUp(null);
    component.quizForm.patchValue({ title: 'a'.repeat(61), description: 'b'.repeat(301) });
    expect(component.quizForm.controls.title.hasError('maxlength')).toBeTrue();
    expect(component.quizForm.controls.description.hasError('maxlength')).toBeTrue();
  });

  it('should accept at most 50 tracks', () => {
    setUp(null);
    for (let id = 1; id <= 51; id++) {
      component.addTrack(result(id));
    }
    expect(component.tracks.length).toBe(50);
    expect(component.isFull()).toBeTrue();
  });

  it('should fill the form of an existing quiz', () => {
    setUpEdition();
    expect(component.quizForm.getRawValue().title).toBe('Années 80');
    expect(component.quizForm.getRawValue().description).toBe('Les tubes');
    expect(component.quizForm.getRawValue().visibility).toBe('PUBLIC');
    expect(trackIds()).toEqual([11, 12]);
    expect(fixture.nativeElement.querySelector('input[formControlName=title]').value).toBe(
      'Années 80',
    );
    expect(selected().length).toBe(2);
    expect(selected()[1].textContent).toContain("pas d'extrait");
  });

  it('should show an error when the quiz cannot be loaded', () => {
    setUp('5');
    httpTesting
      .expectOne('/api/quizzes/5')
      .flush({ detail: 'Quiz 5 introuvable' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Quiz 5 introuvable');
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
  });

  it('should add then remove a track', () => {
    setUp(null);
    component.addTrack(result(1));
    component.addTrack(result(2));
    component.addTrack(result(1));
    expect(trackIds()).toEqual([1, 2]);
    component.removeTrack(0);
    expect(trackIds()).toEqual([2]);
    component.removeTrack(0);
    expect(trackIds()).toEqual([]);
    expect(component.quizForm.invalid).toBeTrue();
  });

  it('should send the tracks in the new order', () => {
    setUpEdition();
    fixture.nativeElement.querySelector('button.down').click();
    fixture.detectChanges();
    expect(selected()[0].textContent).toContain('Titre B');
    submitButton().click();
    const request = httpTesting.expectOne('/api/quizzes/5');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      title: 'Années 80',
      description: 'Les tubes',
      visibility: 'PUBLIC',
      trackIds: [12, 11],
    });
    request.flush(quiz);
    expect(navigate).toHaveBeenCalledOnceWith(['/mes-quiz']);
  });

  it('should move a track up and stay inside the list', () => {
    setUpEdition();
    component.moveTrack(0, -1);
    component.moveTrack(1, 1);
    expect(trackIds()).toEqual([11, 12]);
    component.moveTrack(1, -1);
    expect(trackIds()).toEqual([12, 11]);
  });

  it('should create a quiz', () => {
    setUp(null);
    component.quizForm.patchValue({ title: ' Mon quiz ', description: '  ' });
    component.addTrack(result(1));
    component.onSubmit();
    const request = httpTesting.expectOne('/api/quizzes');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      title: 'Mon quiz',
      description: null,
      visibility: 'PRIVATE',
      trackIds: [1],
    });
    request.flush(quiz);
    expect(navigate).toHaveBeenCalledOnceWith(['/mes-quiz']);
  });

  it('should show the validation errors of the server', () => {
    setUpEdition();
    component.onSubmit();
    httpTesting
      .expectOne('/api/quizzes/5')
      .flush(
        { detail: 'Requête invalide', errors: { title: 'Le titre est obligatoire' } },
        { status: 400, statusText: 'Bad Request' },
      );
    fixture.detectChanges();
    expect(component.errorMessages()).toEqual(['Le titre est obligatoire']);
    expect(fixture.nativeElement.textContent).toContain('Le titre est obligatoire');
    expect(component.saving()).toBeFalse();
    expect(navigate).not.toHaveBeenCalled();
  });

  it('should show the detail of another error', () => {
    setUpEdition();
    component.onSubmit();
    httpTesting
      .expectOne('/api/quizzes/5')
      .flush({ detail: 'Morceau 11 introuvable' }, { status: 404, statusText: 'Not Found' });
    expect(component.errorMessages()).toEqual(['Morceau 11 introuvable']);
  });

  it('should wait 300 ms before searching', fakeAsync(() => {
    setUp(null);
    component.searchControl.setValue('daf');
    tick(200);
    component.searchControl.setValue('daft');
    tick(299);
    httpTesting.expectNone((req) => req.url === '/api/tracks/search');
    tick(1);
    searchRequest('daft').flush([]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucun morceau trouvé');
  }));

  it('should cancel the previous search', fakeAsync(() => {
    setUp(null);
    const first = search('daft');
    const second = search('queen');
    expect(first.cancelled).toBeTrue();
    second.flush([result(2)]);
    expect(component.results()?.map((track) => track.id)).toEqual([2]);
  }));

  it('should add a track from the results', fakeAsync(() => {
    setUp(null);
    search('daft').flush([result(1), result(2, false)]);
    fixture.detectChanges();
    const rows = fixture.nativeElement.querySelectorAll('.results .track');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('3:05');
    expect(rows[0].textContent).not.toContain("pas d'extrait");
    expect(rows[1].textContent).toContain("pas d'extrait");
    const button = rows[0].querySelector('button.add');
    button.click();
    fixture.detectChanges();
    expect(trackIds()).toEqual([1]);
    expect(button.disabled).toBeTrue();
    expect(selected().length).toBe(1);
  }));

  it('should keep searching after an error', fakeAsync(() => {
    setUp(null);
    search('daft').flush(null, { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('La recherche est indisponible');
    search('queen').flush([result(2)]);
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain('La recherche est indisponible');
    expect(component.results()?.length).toBe(1);
  }));

  it('should clear the results of an empty search', fakeAsync(() => {
    setUp(null);
    search('daft').flush([result(1)]);
    component.searchControl.setValue(' ');
    tick(300);
    expect(component.results()).toBeNull();
  }));
});
