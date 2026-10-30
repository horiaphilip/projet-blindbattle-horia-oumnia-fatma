import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Stats, User, UserRequest } from './user';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private apiUrl: string = '/api/users';

  constructor(private http: HttpClient) {}

  public getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  public getUser(id: number): Observable<User> {
    return this.http.get<User>(this.apiUrl + '/' + id);
  }

  public createUser(user: UserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  public updateUser(id: number, user: UserRequest): Observable<User> {
    return this.http.put<User>(this.apiUrl + '/' + id, user);
  }

  public deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }

  public getStats(id: number): Observable<Stats> {
    return this.http.get<Stats>(this.apiUrl + '/' + id + '/stats');
  }
}
