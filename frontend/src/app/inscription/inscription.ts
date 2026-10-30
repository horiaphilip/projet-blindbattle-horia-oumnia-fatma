import { Component, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService } from '../user-service';

@Component({
  selector: 'app-inscription',
  imports: [ReactiveFormsModule],
  templateUrl: './inscription.html',
  styleUrl: './inscription.scss',
})
export class Inscription {
  public inscriptionForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(40)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });
  public errorMessage = signal<string>('');
  public sending = signal<boolean>(false);

  constructor(
    private userService: UserService,
    private router: Router,
  ) {}

  public onSubmit() {
    if (this.inscriptionForm.invalid) {
      return;
    }
    this.sending.set(true);
    this.errorMessage.set('');
    this.userService.createUser(this.inscriptionForm.getRawValue()).subscribe({
      next: (user) => this.router.navigate(['/profil', user.id]),
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        this.errorMessage.set(error.error?.detail ?? 'Impossible de créer le compte');
      },
    });
  }
}
