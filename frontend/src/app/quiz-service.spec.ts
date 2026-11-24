import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Quiz, QuizRequest, QuizSummary, TrackResult } from './quiz';
import { QuizService } from './quiz-service';
import { Page } from './stats';

describe('QuizService', () => {
  let service: QuizService;
  let httpTesting: HttpTestingController;
  const quizRequest: QuizRequest = {
    title: 'Années 80',
    description: null,
    visibility: 'PUBLIC',
    trackIds: [3, 1],
  };
  const quiz: Quiz = {
    id: 5,
    title: 'Années 80',
    description: null,
    visibility: 'PUBLIC',
    shareToken: 'abc-123',
    ownerId: 1,
    ownerName: 'Ana',
    trackCount: 0,
    playableTrackCount: 0,
    createdAt: '',
    updatedAt: null,
    tracks: [],
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(QuizService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get a page of my quizzes', () => {
    let result: Page<QuizSummary> | undefined;
    service.getMyQuizzes(1, 10).subscribe((page) => (result = page));
    const request = httpTesting.expectOne('/api/quizzes?page=1&size=10');
    expect(request.request.method).toBe('GET');
    const page = { content: [], page: 1, size: 10, totalElements: 10, totalPages: 1 };
    request.flush(page);
    expect(result).toEqual(page);
  });

  it('should get a quiz', () => {
    let result: Quiz | undefined;
    service.getQuiz(5).subscribe((found) => (result = found));
    const request = httpTesting.expectOne('/api/quizzes/5');
    expect(request.request.method).toBe('GET');
    request.flush(quiz);
    expect(result).toEqual(quiz);
  });

  it('should get a shared quiz', () => {
    let result: Quiz | undefined;
    service.getSharedQuiz('abc-123').subscribe((found) => (result = found));
    const request = httpTesting.expectOne('/api/quizzes/shared/abc-123');
    expect(request.request.method).toBe('GET');
    request.flush(quiz);
    expect(result).toEqual(quiz);
  });

  it('should post a new quiz', () => {
    let result: Quiz | undefined;
    service.createQuiz(quizRequest).subscribe((created) => (result = created));
    const request = httpTesting.expectOne('/api/quizzes');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(quizRequest);
    request.flush(quiz);
    expect(result).toEqual(quiz);
  });

  it('should put an existing quiz', () => {
    let result: Quiz | undefined;
    service.updateQuiz(5, quizRequest).subscribe((updated) => (result = updated));
    const request = httpTesting.expectOne('/api/quizzes/5');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(quizRequest);
    request.flush(quiz);
    expect(result).toEqual(quiz);
  });

  it('should delete a quiz', () => {
    let done = false;
    service.deleteQuiz(5).subscribe(() => (done = true));
    const request = httpTesting.expectOne('/api/quizzes/5');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    expect(done).toBeTrue();
  });

  it('should search tracks', () => {
    let result: TrackResult[] | undefined;
    service.searchTracks('daft punk').subscribe((tracks) => (result = tracks));
    const request = httpTesting.expectOne((req) => req.url === '/api/tracks/search');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('q')).toBe('daft punk');
    request.flush([]);
    expect(result).toEqual([]);
  });
});
