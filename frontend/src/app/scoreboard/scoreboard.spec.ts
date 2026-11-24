import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Scoreboard } from './scoreboard';

describe('Scoreboard', () => {
  let fixture: ComponentFixture<Scoreboard>;
  const standings = [
    { playerId: 1, nickname: 'Ana', score: 2400, answered: true, correct: true, points: 900 },
    { playerId: 2, nickname: 'Bob', score: 1200, answered: true, correct: false, points: 0 },
    { playerId: 3, nickname: 'Zoé', score: 300, answered: false, correct: false, points: 0 },
  ];

  function rows(): HTMLElement[] {
    return Array.from(fixture.nativeElement.querySelectorAll('.row'));
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Scoreboard],
    }).compileComponents();

    fixture = TestBed.createComponent(Scoreboard);
  });

  it('should list the players with their rank and score', () => {
    fixture.componentRef.setInput('standings', standings);
    fixture.componentRef.setInput('highlight', 2);
    fixture.detectChanges();
    expect(rows().length).toBe(3);
    expect(rows()[0].textContent).toContain('1');
    expect(rows()[0].textContent).toContain('Ana');
    expect(rows()[0].textContent).toContain('2400');
    expect(rows()[1].classList).toContain('me');
    expect(fixture.nativeElement.querySelector('.points')).toBeNull();
  });

  it('should show the points of the round', () => {
    fixture.componentRef.setInput('standings', standings);
    fixture.componentRef.setInput('showPoints', true);
    fixture.detectChanges();
    const points = rows().map((row) => row.querySelector('.points')?.textContent?.trim());
    expect(points).toEqual(['+900', '✗', '–']);
  });

  it('should limit the number of rows', () => {
    fixture.componentRef.setInput('standings', standings);
    fixture.componentRef.setInput('limit', 2);
    fixture.detectChanges();
    expect(rows().length).toBe(2);
  });

  it('should show a message without players', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aucun joueur');
  });
});
