import { Component, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth-service';
import { passwordsMatch } from '../password-validators';
import { UserService } from '../user-service';

@Component({
  selector: 'app-inscription',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './inscription.html',
  styleUrl: './inscription.scss',
})
export class Inscription {
  public inscriptionForm = new FormGroup(
    {
      name: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.maxLength(40)],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.email],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
    },
    { validators: passwordsMatch },
  );
  public errorMessage = signal<string>('');
  public sending = signal<boolean>(false);

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {}

  public onSubmit() {
    if (this.inscriptionForm.invalid) {
      return;
    }
    this.sending.set(true);
    this.errorMessage.set('');
    const { name, email, password } = this.inscriptionForm.getRawValue();
    this.userService
      .createUser({ name, email, password })
      .pipe(switchMap(() => this.authService.login({ email, password })))
      .subscribe({
        next: (response) => {
          this.snackBar.open('Bienvenue ' + response.user.name + ' !', 'OK', { duration: 3000 });
          this.router.navigate(['/profil', response.user.id]);
        },
        error: (error: HttpErrorResponse) => {
          this.sending.set(false);
          this.errorMessage.set(error.error?.detail ?? 'Impossible de créer le compte');
        },
      });
  }
}
