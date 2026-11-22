import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth-service';
import { User } from '../user';
import { UserService } from '../user-service';

@Component({
  selector: 'app-joueurs',
  imports: [DatePipe, RouterLink],
  templateUrl: './joueurs.html',
  styleUrl: './joueurs.scss',
})
export class Joueurs implements OnInit {
  public users = signal<User[]>([]);
  public loading = signal<boolean>(true);
  public error = signal<boolean>(false);

  constructor(
    private userService: UserService,
    public authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  public isMe(user: User): boolean {
    return this.authService.currentUser()?.id === user.id;
  }

  public deleteUser(user: User) {
    if (!this.isMe(user) || !confirm('Supprimer définitivement ton compte ?')) {
      return;
    }
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.users.update((users) => users.filter((u) => u.id !== user.id));
        this.authService.logout();
        this.snackBar.open('Ton compte a été supprimé', 'OK', { duration: 3000 });
        this.router.navigate(['']);
      },
      error: () => this.error.set(true),
    });
  }
}
