import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Pagination } from '../pagination/pagination';
import { QuizSummary } from '../quiz';
import { QuizService } from '../quiz-service';
import { Page } from '../stats';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-mes-quiz',
  imports: [DatePipe, RouterLink, Pagination],
  templateUrl: './mes-quiz.html',
  styleUrl: './mes-quiz.scss',
})
export class MesQuiz implements OnInit, OnDestroy {
  public quizzes = signal<Page<QuizSummary> | null>(null);
  public errorMessage = signal<string>('');
  private subscription: Subscription | null = null;

  constructor(
    private quizService: QuizService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  public loadPage(page: number) {
    this.subscription?.unsubscribe();
    this.errorMessage.set('');
    this.subscription = this.quizService.getMyQuizzes(page, PAGE_SIZE).subscribe({
      next: (quizzes) => this.quizzes.set(quizzes),
      error: () => this.errorMessage.set('Impossible de charger tes quiz.'),
    });
  }

  public deleteQuiz(quiz: QuizSummary) {
    if (!confirm('Supprimer définitivement le quiz « ' + quiz.title + ' » ?')) {
      return;
    }
    this.errorMessage.set('');
    this.quizService.deleteQuiz(quiz.id).subscribe({
      next: () => {
        this.snackBar.open('Quiz supprimé', 'OK', { duration: 3000 });
        this.loadPage(this.pageAfterDelete());
      },
      error: (error: HttpErrorResponse) =>
        this.errorMessage.set(error.error?.detail ?? 'Impossible de supprimer le quiz'),
    });
  }

  public copyShareLink(quiz: QuizSummary) {
    this.errorMessage.set('');
    this.quizService.getQuiz(quiz.id).subscribe({
      next: (fullQuiz) => this.copy(location.origin + '/quiz/partage/' + fullQuiz.shareToken),
      error: (error: HttpErrorResponse) =>
        this.errorMessage.set(error.error?.detail ?? 'Impossible de récupérer le lien de partage'),
    });
  }

  private async copy(link: string): Promise<void> {
    try {
      await navigator.clipboard.writeText(link);
      this.snackBar.open('Lien de partage copié', 'OK', { duration: 3000 });
    } catch {
      this.snackBar.open('Lien de partage : ' + link, 'OK');
    }
  }

  private pageAfterDelete(): number {
    const quizzes = this.quizzes();
    if (quizzes === null) {
      return 0;
    }
    return quizzes.content.length === 1 ? Math.max(quizzes.page - 1, 0) : quizzes.page;
  }
}
