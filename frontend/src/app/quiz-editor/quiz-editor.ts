import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, Subscription, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Quiz, QuizRequest, QuizVisibility, SelectedTrack, TrackResult } from '../quiz';
import { QuizService } from '../quiz-service';

const MAX_TRACKS = 50;

@Component({
  selector: 'app-quiz-editor',
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './quiz-editor.html',
  styleUrl: './quiz-editor.scss',
})
export class QuizEditor implements OnInit, OnDestroy {
  public quizId = signal<number | null>(null);
  public loading = signal<boolean>(false);
  public loadError = signal<string>('');
  public saving = signal<boolean>(false);
  public errorMessages = signal<string[]>([]);
  public results = signal<TrackResult[] | null>(null);
  public searching = signal<boolean>(false);
  public searchError = signal<string>('');
  public searchControl = new FormControl('', { nonNullable: true });
  public quizForm = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(60)],
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(300)],
    }),
    visibility: new FormControl<QuizVisibility>('PRIVATE', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    tracks: new FormArray<FormControl<SelectedTrack>>(
      [],
      [Validators.required, Validators.maxLength(MAX_TRACKS)],
    ),
  });
  private searchSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private quizService: QuizService,
    private snackBar: MatSnackBar,
  ) {}

  public get tracks(): FormArray<FormControl<SelectedTrack>> {
    return this.quizForm.controls.tracks;
  }

  ngOnInit(): void {
    this.searchSubscription = this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        switchMap((query) => this.search(query.trim())),
      )
      .subscribe((results) => {
        this.searching.set(false);
        this.results.set(results);
      });
    const param = this.route.snapshot.paramMap.get('id');
    if (param !== null) {
      this.loadQuiz(Number(param));
    }
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  public isSelected(trackId: number): boolean {
    return this.tracks.getRawValue().some((track) => track.trackId === trackId);
  }

  public isFull(): boolean {
    return this.tracks.length >= MAX_TRACKS;
  }

  public addTrack(result: TrackResult) {
    if (this.isSelected(result.id) || this.isFull()) {
      return;
    }
    this.pushTrack({
      trackId: result.id,
      title: result.title,
      artist: result.artist,
      coverUrl: result.coverUrl,
      hasPreview: result.hasPreview,
    });
  }

  public removeTrack(index: number) {
    this.tracks.removeAt(index);
  }

  public moveTrack(index: number, offset: number) {
    const target = index + offset;
    if (target < 0 || target >= this.tracks.length) {
      return;
    }
    const control = this.tracks.at(index);
    this.tracks.removeAt(index);
    this.tracks.insert(target, control);
  }

  public duration(seconds: number): string {
    return Math.floor(seconds / 60) + ':' + String(seconds % 60).padStart(2, '0');
  }

  public onSubmit() {
    if (this.quizForm.invalid || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.errorMessages.set([]);
    this.save().subscribe({
      next: () => {
        this.snackBar.open('Quiz enregistré', 'OK', { duration: 3000 });
        this.router.navigate(['/mes-quiz']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.errorMessages.set(this.messages(error));
      },
    });
  }

  private loadQuiz(id: number) {
    if (Number.isNaN(id)) {
      this.loadError.set('Quiz introuvable');
      return;
    }
    this.quizId.set(id);
    this.loading.set(true);
    this.quizService.getQuiz(id).subscribe({
      next: (quiz) => {
        this.fillForm(quiz);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.loadError.set(error.error?.detail ?? 'Impossible de charger le quiz');
      },
    });
  }

  private fillForm(quiz: Quiz) {
    this.quizForm.patchValue({
      title: quiz.title,
      description: quiz.description ?? '',
      visibility: quiz.visibility,
    });
    this.tracks.clear();
    quiz.tracks.forEach(({ trackId, title, artist, coverUrl, hasPreview }) =>
      this.pushTrack({ trackId, title, artist, coverUrl, hasPreview }),
    );
  }

  private pushTrack(track: SelectedTrack) {
    this.tracks.push(new FormControl<SelectedTrack>(track, { nonNullable: true }));
  }

  private search(query: string): Observable<TrackResult[] | null> {
    this.searchError.set('');
    if (query === '') {
      return of(null);
    }
    return of(query).pipe(
      tap(() => this.searching.set(true)),
      switchMap(() => this.quizService.searchTracks(query)),
      catchError((error: HttpErrorResponse) => {
        this.searchError.set(error.error?.detail ?? 'La recherche est indisponible');
        return of(null);
      }),
    );
  }

  private save(): Observable<Quiz> {
    const id = this.quizId();
    return id === null
      ? this.quizService.createQuiz(this.request())
      : this.quizService.updateQuiz(id, this.request());
  }

  private request(): QuizRequest {
    const { title, description, visibility, tracks } = this.quizForm.getRawValue();
    return {
      title: title.trim(),
      description: description.trim() === '' ? null : description.trim(),
      visibility: visibility,
      trackIds: tracks.map((track) => track.trackId),
    };
  }

  private messages(error: HttpErrorResponse): string[] {
    const errors: string[] = Object.values(error.error?.errors ?? {});
    return errors.length > 0 ? errors : [error.error?.detail ?? "Impossible d'enregistrer le quiz"];
  }
}
