import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, User } from './user';

const TOKEN_KEY = 'blindbattle.token';
const USER_KEY = 'blindbattle.user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl: string = '/api/auth';
  private user = signal<User | null>(this.storedUser());
  public currentUser = this.user.asReadonly();
  public isLoggedIn = computed(() => this.user() !== null);

  constructor(private http: HttpClient) {}

  public login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.apiUrl + '/login', request).pipe(
      tap((response) => {
        this.write(TOKEN_KEY, response.token);
        this.saveUser(response.user);
      }),
    );
  }

  public logout(): void {
    this.remove(TOKEN_KEY);
    this.remove(USER_KEY);
    this.user.set(null);
  }

  public token(): string | null {
    const token = this.read(TOKEN_KEY);
    if (token === null) {
      return null;
    }
    if (this.isExpired(token)) {
      this.logout();
      return null;
    }
    return token;
  }

  public refreshMe(): void {
    this.http.get<User>('/api/users/me').subscribe((user) => this.saveUser(user));
  }

  private saveUser(user: User): void {
    this.write(USER_KEY, JSON.stringify(user));
    this.user.set(user);
  }

  private storedUser(): User | null {
    const user = this.read(USER_KEY);
    const token = this.read(TOKEN_KEY);
    if (user === null || token === null || this.isExpired(token)) {
      return null;
    }
    try {
      return JSON.parse(user) as User;
    } catch {
      return null;
    }
  }

  private isExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const claims = JSON.parse(atob(payload));
      return typeof claims.exp !== 'number' || claims.exp * 1000 <= Date.now();
    } catch {
      return true;
    }
  }

  private read(key: string): string | null {
    try {
      return localStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private write(key: string, value: string): void {
    try {
      localStorage.setItem(key, value);
    } catch {}
  }

  private remove(key: string): void {
    try {
      localStorage.removeItem(key);
    } catch {}
  }
}
