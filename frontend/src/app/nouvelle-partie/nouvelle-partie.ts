import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { AnswerMode, Difficulty, GameSettings, GuessTarget, Theme } from '../game';
import { GameService } from '../game-service';
import { QuizSummary } from '../quiz';
import { QuizService } from '../quiz-service';

type Source = 'THEME' | 'QUIZ';

function sourceChosen(group: AbstractControl): ValidationErrors | null {
  const { source, themeId, quizId } = group.value;
  if (source === 'THEME' && !themeId) {
    return { themeMissing: true };
  }
  if (source === 'QUIZ' && !quizId) {
    return { quizMissing: true };
  }
  return null;
}

@Component({
  selector: 'app-nouvelle-partie',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './nouvelle-partie.html',
  styleUrl: './nouvelle-partie.scss',
})
export class NouvellePartie implements OnInit {
  public themes = signal<Theme[]>([]);
  public quizzes = signal<QuizSummary[]>([]);
  public sending = signal<boolean>(false);
  public errorMessages = signal<string[]>([]);
  public gameForm = new FormGroup(
    {
      source: new FormControl<Source>('THEME', { nonNullable: true }),
      themeId: new FormControl<string>('chart-world', { nonNullable: true }),
      quizId: new FormControl<number | null>(null),
      answerMode: new FormControl<AnswerMode>('QUICK_CHOICE', { nonNullable: true }),
      guessTarget: new FormControl<GuessTarget>('TITLE', { nonNullable: true }),
      roundCount: new FormControl<number>(5, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(3), Validators.max(20)],
      }),
      roundSeconds: new FormControl<number>(20, {
        nonNullable: true,
        validators: [Validators.required, Validators.min(10), Validators.max(30)],
      }),
      difficulty: new FormControl<Difficulty>('EASY', { nonNullable: true }),
    },
    { validators: sourceChosen },
  );

  constructor(
    private gameService: GameService,
    private quizService: QuizService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.gameService.getThemes().subscribe({
      next: (themes) => this.themes.set(themes),
      error: () =>
        this.errorMessages.set(['Les thèmes sont indisponibles, choisis un de tes quiz.']),
    });
    this.quizService.getMyQuizzes(0, 50).subscribe({
      next: (page) => this.quizzes.set(page.content.filter((quiz) => quiz.trackCount > 0)),
    });
  }

  public isQuiz(): boolean {
    return this.gameForm.controls.source.value === 'QUIZ';
  }

  public onSubmit() {
    if (this.gameForm.invalid || this.sending()) {
      return;
    }
    this.sending.set(true);
    this.errorMessages.set([]);
    this.gameService.createGame(this.settings()).subscribe({
      next: (game) => this.router.navigate(['/host', game.code]),
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        const errors: string[] = Object.values(error.error?.errors ?? {});
        this.errorMessages.set(
          errors.length > 0 ? errors : [error.error?.detail ?? 'Impossible de créer la partie'],
        );
      },
    });
  }

  private settings(): GameSettings {
    const value = this.gameForm.getRawValue();
    const quiz = value.source === 'QUIZ';
    return {
      answerMode: value.answerMode,
      guessTarget: value.guessTarget,
      roundCount: value.roundCount,
      roundSeconds: value.roundSeconds,
      difficulty: value.difficulty,
      themeId: quiz ? null : value.themeId,
      quizId: quiz ? value.quizId : null,
    };
  }
}
