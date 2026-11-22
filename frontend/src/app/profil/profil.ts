import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Subscription, catchError, of, switchMap, takeWhile, timer } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../auth-service';
import { BadgeLabelPipe } from '../badge-label-pipe';
import { Stats, User } from '../user';
import { UserService } from '../user-service';

@Component({
  selector: 'app-profil',
  imports: [
    DatePipe,
    RouterLink,
    BadgeLabelPipe,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './profil.html',
  styleUrl: './profil.scss',
})
export class Profil implements OnInit, OnDestroy {
  public user = signal<User | null>(null);
  public stats = signal<Stats | null>(null);
  public notFound = signal<boolean>(false);
  public editing = signal<boolean>(false);
  public saving = signal<boolean>(false);
  public editError = signal<string>('');
  public isMine = computed(() => {
    const user = this.user();
    return user !== null && user.id === this.authService.currentUser()?.id;
  });
  public editForm = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(40)],
    }),
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
  });
  private statsSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    const param = this.route.snapshot.paramMap.get('id');
    const id = param !== null ? Number(param) : this.authService.currentUser()?.id;
    if (id === undefined || Number.isNaN(id)) {
      this.notFound.set(true);
      return;
    }
    this.userService.getUser(id).subscribe({
      next: (user) => {
        this.user.set(user);
        this.waitForStats(id);
      },
      error: () => this.notFound.set(true),
    });
  }

  ngOnDestroy(): void {
    this.statsSubscription?.unsubscribe();
  }

  public startEditing() {
    const user = this.user();
    if (user === null) {
      return;
    }
    this.editForm.patchValue({ name: user.name, email: user.email });
    this.editError.set('');
    this.editing.set(true);
  }

  public cancelEditing() {
    this.editing.set(false);
  }

  public saveProfile() {
    const user = this.user();
    if (user === null || this.editForm.invalid) {
      return;
    }
    this.saving.set(true);
    this.editError.set('');
    this.userService.updateUser(user.id, this.editForm.getRawValue()).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.saving.set(false);
        this.editing.set(false);
        this.authService.refreshMe();
        this.snackBar.open('Profil mis à jour', 'OK', { duration: 3000 });
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.editError.set(error.error?.detail ?? 'Impossible de modifier le profil');
      },
    });
  }

  private waitForStats(id: number) {
    this.statsSubscription = timer(0, 1000)
      .pipe(
        switchMap(() => this.userService.getStats(id).pipe(catchError(() => of(null)))),
        takeWhile((stats) => stats === null, true),
      )
      .subscribe((stats) => this.stats.set(stats));
  }
}
