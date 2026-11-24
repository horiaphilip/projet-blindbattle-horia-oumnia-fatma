import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AnswerPad, PadAnswer } from './answer-pad';

describe('AnswerPad', () => {
  let fixture: ComponentFixture<AnswerPad>;
  let emitted: PadAnswer[];

  function setUp(inputs: Record<string, unknown>) {
    Object.entries(inputs).forEach(([name, value]) => fixture.componentRef.setInput(name, value));
    fixture.detectChanges();
  }

  async function type(text: string) {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    input.value = text;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnswerPad],
    }).compileComponents();

    fixture = TestBed.createComponent(AnswerPad);
    emitted = [];
    fixture.componentInstance.answer.subscribe((answer) => emitted.push(answer));
  });

  it('should emit the index of the chosen proposal', () => {
    setUp({ mode: 'QUICK_CHOICE', choices: ['A', 'B', 'C', 'D'] });
    const buttons: HTMLButtonElement[] = Array.from(
      fixture.nativeElement.querySelectorAll('button.choice'),
    );
    expect(buttons.map((button) => button.textContent?.trim())).toEqual(['A', 'B', 'C', 'D']);
    buttons[2].click();
    expect(emitted).toEqual([{ choiceIndex: 2 }]);
  });

  it('should not emit when disabled', () => {
    setUp({ mode: 'QUICK_CHOICE', choices: ['A', 'B', 'C', 'D'], disabled: true });
    fixture.componentInstance.choose(1);
    expect(emitted).toEqual([]);
  });

  it('should ask the right question', () => {
    setUp({ mode: 'QUICK_CHOICE', guessTarget: 'ARTIST', choices: [] });
    expect(fixture.nativeElement.textContent).toContain("Qui est l'artiste ?");
  });

  it('should emit the typed answer without surrounding spaces', async () => {
    setUp({ mode: 'TYPE_IT' });
    await fixture.whenStable();
    fixture.detectChanges();
    const submit: HTMLButtonElement = fixture.nativeElement.querySelector('button[type=submit]');
    expect(submit.disabled).toBeTrue();
    await type('  Get Lucky ');
    expect(submit.disabled).toBeFalse();
    submit.click();
    expect(emitted).toEqual([{ text: 'Get Lucky' }]);
  });
});
