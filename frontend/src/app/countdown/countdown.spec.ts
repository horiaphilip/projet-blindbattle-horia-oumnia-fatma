import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Countdown } from './countdown';

describe('Countdown', () => {
  let component: Countdown;
  let fixture: ComponentFixture<Countdown>;

  function seconds(): string {
    return fixture.nativeElement.querySelector('.seconds').textContent.trim();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Countdown],
    }).compileComponents();

    fixture = TestBed.createComponent(Countdown);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('should show zero without a running round', () => {
    fixture.componentRef.setInput('endsAt', null);
    fixture.detectChanges();
    expect(seconds()).toBe('0');
    expect(component.percent()).toBe(0);
  });

  it('should count down the remaining seconds', fakeAsync(() => {
    fixture.componentRef.setInput('totalSeconds', 20);
    fixture.componentRef.setInput('endsAt', Date.now() + 10000);
    fixture.detectChanges();
    expect(seconds()).toBe('10');
    expect(component.percent()).toBe(50);

    tick(4000);
    fixture.detectChanges();
    expect(seconds()).toBe('6');

    tick(7000);
    fixture.detectChanges();
    expect(seconds()).toBe('0');
    expect(fixture.nativeElement.querySelector('.countdown').classList).toContain('urgent');
  }));
});
