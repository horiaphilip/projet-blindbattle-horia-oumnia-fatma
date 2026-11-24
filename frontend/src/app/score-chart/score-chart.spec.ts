import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideCharts, withDefaultRegisterables } from 'ng2-charts';
import { ScoreChart } from './score-chart';

describe('ScoreChart', () => {
  let component: ScoreChart;
  let fixture: ComponentFixture<ScoreChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScoreChart],
      providers: [provideCharts(withDefaultRegisterables())],
    }).compileComponents();

    fixture = TestBed.createComponent(ScoreChart);
    component = fixture.componentInstance;
    const game = {
      gameCode: 'ABC123',
      playedAt: '2026-11-20T12:00:00',
      rank: 1,
      playerCount: 2,
      finalScore: 800,
      correctAnswers: 4,
      totalRounds: 5,
    };
    fixture.componentRef.setInput('entries', [
      game,
      { ...game, gameCode: 'XYZ789', playedAt: '2026-11-18T09:30:00', finalScore: 500 },
    ]);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
    expect(fixture.nativeElement.querySelector('canvas')).not.toBeNull();
  });

  it('should order the scores from the oldest to the latest game', () => {
    expect(component.data.labels).toEqual(['18/11 09:30', '20/11 12:00']);
    expect(component.data.datasets[0].data).toEqual([500, 800]);
  });

  it('should use the theme colors', () => {
    expect(component.data.datasets[0].borderColor).toBe('#ff3d7f');
    expect(component.data.datasets[0].pointBackgroundColor).toBe('#3dd6ff');
  });
});
