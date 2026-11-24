import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { GameService } from '../game-service';
import { Rejoindre } from './rejoindre';

describe('Rejoindre', () => {
  let component: Rejoindre;
  let fixture: ComponentFixture<Rejoindre>;
  let httpTesting: HttpTestingController;

  function setUp(code: string | null) {
    TestBed.configureTestingModule({
      imports: [Rejoindre],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(code === null ? {} : { code }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(Rejoindre);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  async function fill(nickname: string) {
    await fixture.whenStable();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input[name=nickname]');
    input.value = nickname;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  function submit() {
    fixture.nativeElement.querySelector('button[type=submit]').click();
  }

  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should take the code from the link', () => {
    setUp('abc234');
    expect(component.code).toBe('ABC234');
  });

  it('should join the game and open the player screen', async () => {
    setUp('ABC234');
    const navigate = spyOn(TestBed.inject(Router), 'navigate');
    await fill('Ana');
    submit();
    const request = httpTesting.expectOne('/api/games/ABC234/players');
    expect(request.request.body).toEqual({ nickname: 'Ana' });
    request.flush({ playerId: 3, nickname: 'Ana', playerToken: 'jeton' });
    expect(TestBed.inject(GameService).player('ABC234')?.playerToken).toBe('jeton');
    expect(navigate).toHaveBeenCalledWith(['/jeu', 'ABC234']);
  });

  it('should not send an incomplete form', async () => {
    setUp(null);
    await fill('Ana');
    submit();
    httpTesting.expectNone('/api/games//players');
    expect(fixture.nativeElement.querySelector('button[type=submit]').disabled).toBeTrue();
  });

  it('should show why the game cannot be joined', async () => {
    setUp('ABC234');
    await fill('Ana');
    submit();
    httpTesting
      .expectOne('/api/games/ABC234/players')
      .flush({ detail: 'Le pseudo Ana est déjà pris' }, { status: 409, statusText: 'Conflict' });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Le pseudo Ana est déjà pris');
  });
});
