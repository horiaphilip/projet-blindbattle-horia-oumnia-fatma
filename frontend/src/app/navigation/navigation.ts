import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth-service';

@Component({
  selector: 'app-navigation',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navigation.html',
  styleUrl: './navigation.scss',
})
export class Navigation {
  constructor(
    public authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {}

  public logout() {
    this.authService.logout();
    this.snackBar.open('Vous êtes déconnecté', 'OK', { duration: 3000 });
    this.router.navigate(['']);
  }
}
