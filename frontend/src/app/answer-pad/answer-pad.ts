import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { AnswerMode, GuessTarget } from '../game';

export interface PadAnswer {
  text?: string;
  choiceIndex?: number;
}

@Component({
  selector: 'app-answer-pad',
  imports: [FormsModule],
  templateUrl: './answer-pad.html',
  styleUrl: './answer-pad.scss',
})
export class AnswerPad {
  @Input() mode: AnswerMode = 'QUICK_CHOICE';
  @Input() guessTarget: GuessTarget = 'TITLE';
  @Input() choices: string[] = [];
  @Input() disabled: boolean = false;
  @Output() answer = new EventEmitter<PadAnswer>();
  public text: string = '';

  public question(): string {
    switch (this.guessTarget) {
      case 'TITLE':
        return 'Quel est le titre ?';
      case 'ARTIST':
        return "Qui est l'artiste ?";
      case 'BOTH':
        return 'Titre et artiste ?';
    }
  }

  public choose(index: number) {
    if (!this.disabled) {
      this.answer.emit({ choiceIndex: index });
    }
  }

  public onSubmit(answerForm: NgForm) {
    if (answerForm.invalid || this.disabled) {
      return;
    }
    this.answer.emit({ text: this.text.trim() });
    this.text = '';
  }
}
