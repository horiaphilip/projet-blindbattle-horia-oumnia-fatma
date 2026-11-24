import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AnswerRequest, Game, GameSettings, GameState, JoinResponse, Preview, Theme } from './game';

const PLAYER_KEY = 'blindbattle.player.';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  private apiUrl: string = '/api/games';

  constructor(private http: HttpClient) {}

  public createGame(settings: GameSettings): Observable<Game> {
    return this.http.post<Game>(this.apiUrl, { settings: settings });
  }

  public getGame(code: string): Observable<Game> {
    return this.http.get<Game>(this.url(code));
  }

  public getState(code: string): Observable<GameState> {
    return this.http.get<GameState>(this.url(code) + '/state');
  }

  public join(code: string, nickname: string): Observable<JoinResponse> {
    return this.http.post<JoinResponse>(this.url(code) + '/players', { nickname: nickname });
  }

  public start(code: string): Observable<GameState> {
    return this.http.post<GameState>(this.url(code) + '/start', {});
  }

  public answer(code: string, answer: AnswerRequest): Observable<void> {
    return this.http.post<void>(this.url(code) + '/answers', answer);
  }

  public preview(code: string): Observable<Preview> {
    return this.http.get<Preview>(this.url(code) + '/rounds/current/preview');
  }

  public getThemes(): Observable<Theme[]> {
    return this.http.get<Theme[]>('/api/themes');
  }

  public savePlayer(code: string, player: JoinResponse): void {
    try {
      sessionStorage.setItem(PLAYER_KEY + code, JSON.stringify(player));
    } catch {}
  }

  public player(code: string): JoinResponse | null {
    try {
      const stored = sessionStorage.getItem(PLAYER_KEY + code);
      return stored === null ? null : (JSON.parse(stored) as JoinResponse);
    } catch {
      return null;
    }
  }

  private url(code: string): string {
    return this.apiUrl + '/' + encodeURIComponent(code.toUpperCase());
  }
}
