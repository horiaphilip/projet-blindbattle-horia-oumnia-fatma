import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Quiz, QuizRequest, QuizSummary, TrackResult } from './quiz';
import { Page } from './stats';

@Injectable({
  providedIn: 'root',
})
export class QuizService {
  private apiUrl: string = '/api/quizzes';

  constructor(private http: HttpClient) {}

  public getMyQuizzes(page: number, size: number): Observable<Page<QuizSummary>> {
    return this.http.get<Page<QuizSummary>>(this.apiUrl, {
      params: new HttpParams().set('page', page).set('size', size),
    });
  }

  public getQuiz(id: number): Observable<Quiz> {
    return this.http.get<Quiz>(this.apiUrl + '/' + id);
  }

  public getSharedQuiz(token: string): Observable<Quiz> {
    return this.http.get<Quiz>(this.apiUrl + '/shared/' + encodeURIComponent(token));
  }

  public createQuiz(quiz: QuizRequest): Observable<Quiz> {
    return this.http.post<Quiz>(this.apiUrl, quiz);
  }

  public updateQuiz(id: number, quiz: QuizRequest): Observable<Quiz> {
    return this.http.put<Quiz>(this.apiUrl + '/' + id, quiz);
  }

  public deleteQuiz(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }

  public searchTracks(query: string): Observable<TrackResult[]> {
    return this.http.get<TrackResult[]>('/api/tracks/search', {
      params: new HttpParams().set('q', query),
    });
  }
}
