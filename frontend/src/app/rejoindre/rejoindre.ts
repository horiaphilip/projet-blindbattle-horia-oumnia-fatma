import { Component, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AuthService } from '../auth-service';
import { GameService } from '../game-service';

@Component({
  selector: 'app-rejoindre',
  imports: [FormsModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  templateUrl: './rejoindre.html',
  styleUrl: './rejoindre.scss',
})
export class Rejoindre implements OnInit {
  public code: string = '';
  public nickname: string = '';
  public errorMessage = signal<string>('');
  public sending = signal<boolean>(false);

  constructor(
    private gameService: GameService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.code = (this.route.snapshot.paramMap.get('code') ?? '').toUpperCase();
    this.nickname = (this.authService.currentUser()?.name ?? '').slice(0, 20);
  }

  public onSubmit(joinForm: NgForm) {
    if (joinForm.invalid || this.sending()) {
      return;
    }
    const code = this.code.trim().toUpperCase();
    this.sending.set(true);
    this.errorMessage.set('');
    this.gameService.join(code, this.nickname.trim()).subscribe({
      next: (player) => {
        this.gameService.savePlayer(code, player);
        this.router.navigate(['/jeu', code]);
      },
      error: (error: HttpErrorResponse) => {
        this.sending.set(false);
        const errors: string[] = Object.values(error.error?.errors ?? {});
        this.errorMessage.set(
          errors[0] ?? error.error?.detail ?? 'Impossible de rejoindre la partie',
        );
      },
    });
  }
}
