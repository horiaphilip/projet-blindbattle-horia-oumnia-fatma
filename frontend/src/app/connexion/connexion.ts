import { Component, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth-service';

@Component({
  selector: 'app-connexion',
  imports: [FormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './connexion.html',
  styleUrl: './connexion.scss',
})
export class Connexion {
  public email: string = '';
  public password: string = '';
  public errorMessage = signal<string>('');
  public sending = signal<boolean>(false);

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
  ) {}

  public onSubmit(loginForm: NgForm) {
    if (loginForm.invalid) {
      return;
    }
    this.sending.set(true);
    this.errorMessage.set('');
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (response) => {
        this.snackBar.open('Bienvenue ' + response.user.name + ' !', 'OK', { duration: 3000 });
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        this.router.navigateByUrl(returnUrl ?? '/profil');
      },
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        if (error.status === 401) {
          this.errorMessage.set('Email ou mot de passe incorrect');
        } else {
          this.errorMessage.set(error.error?.detail ?? 'Connexion impossible');
        }
      },
    });
  }
}
