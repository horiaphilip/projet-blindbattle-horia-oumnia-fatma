import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HistoryEntry, LeaderboardEntry, Page } from './stats';

@Injectable({
  providedIn: 'root',
})
export class StatsService {
  private apiUrl: string = '/api';

  constructor(private http: HttpClient) {}

  public getHistory(userId: number, page: number, size: number): Observable<Page<HistoryEntry>> {
    return this.http.get<Page<HistoryEntry>>(this.apiUrl + '/users/' + userId + '/history', {
      params: this.pageParams(page, size),
    });
  }

  public getLeaderboard(page: number, size: number): Observable<Page<LeaderboardEntry>> {
    return this.http.get<Page<LeaderboardEntry>>(this.apiUrl + '/leaderboard', {
      params: this.pageParams(page, size),
    });
  }

  private pageParams(page: number, size: number): HttpParams {
    return new HttpParams().set('page', page).set('size', size);
  }
}
