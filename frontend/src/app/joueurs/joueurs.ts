import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
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

  constructor(private userService: UserService) {}

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

  public deleteUser(user: User) {
    if (!confirm('Supprimer le compte de ' + user.name + ' ?')) {
      return;
    }
    this.userService.deleteUser(user.id).subscribe(() => {
      this.users.update((users) => users.filter((u) => u.id !== user.id));
    });
  }
}
