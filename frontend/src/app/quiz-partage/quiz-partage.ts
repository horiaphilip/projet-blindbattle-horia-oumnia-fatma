import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Quiz } from '../quiz';
import { QuizService } from '../quiz-service';

@Component({
  selector: 'app-quiz-partage',
  imports: [],
  templateUrl: './quiz-partage.html',
  styleUrl: './quiz-partage.scss',
})
export class QuizPartage implements OnInit {
  public quiz = signal<Quiz | null>(null);
  public errorMessage = signal<string>('');

  constructor(
    private route: ActivatedRoute,
    private quizService: QuizService,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.quizService.getSharedQuiz(token).subscribe({
      next: (quiz) => this.quiz.set(quiz),
      error: (error: HttpErrorResponse) =>
        this.errorMessage.set(
          error.status === 404 || error.status === 400
            ? "Ce quiz n'existe pas ou n'est plus partagé."
            : 'Impossible de charger le quiz.',
        ),
    });
  }
}
