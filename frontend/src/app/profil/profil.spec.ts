import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Profil } from './profil';

describe('Profil', () => {
  let component: Profil;
  let fixture: ComponentFixture<Profil>;
  let httpTesting: HttpTestingController;
  const ana = { id: 1, name: 'Ana', email: 'ana@example.com', createdAt: '', updatedAt: null };
  const stats = { userId: 1, gamesPlayed: 0, totalScore: 0, bestScore: 0, badges: [] };

  function setUp(id: string | null) {
    TestBed.configureTestingModule({
      imports: [Profil],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(id === null ? {} : { id: id }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(Profil);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  }

  function logInAsAna() {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    localStorage.setItem('blindbattle.token', 'header.' + payload + '.signature');
    localStorage.setItem('blindbattle.user', JSON.stringify(ana));
  }

  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    component.ngOnDestroy();
    httpTesting.verify();
    localStorage.clear();
  });

  it('should create', fakeAsync(() => {
    setUp('1');
    httpTesting.expectOne('/api/users/1').flush(ana);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(stats);
    tick(0);
    expect(component).toBeTruthy();
  }));

  it('should load my own profile without an id', fakeAsync(() => {
    logInAsAna();
    setUp(null);
    httpTesting.expectOne('/api/users/1').flush(ana);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(stats);
    tick(0);
    expect(component.isMine()).toBeTrue();
  }));

  it('should not offer to edit the profile of someone else', fakeAsync(() => {
    logInAsAna();
    setUp('2');
    httpTesting.expectOne('/api/users/2').flush({ ...ana, id: 2, name: 'Tom' });
    tick(0);
    httpTesting.expectOne('/api/users/2/stats').flush({ ...stats, userId: 2 });
    tick(0);
    fixture.detectChanges();
    expect(component.isMine()).toBeFalse();
    expect(fixture.nativeElement.textContent).not.toContain('Modifier mon profil');
  }));

  it('should fill the form and show a conflict error', fakeAsync(() => {
    logInAsAna();
    setUp(null);
    httpTesting.expectOne('/api/users/1').flush(ana);
    tick(0);
    httpTesting.expectOne('/api/users/1/stats').flush(stats);
    tick(0);
    component.startEditing();
    expect(component.editForm.getRawValue()).toEqual({ name: 'Ana', email: 'ana@example.com' });
    component.editForm.patchValue({ email: 'tom@example.com' });
    component.saveProfile();
    const request = httpTesting.expectOne('/api/users/1');
    expect(request.request.method).toBe('PUT');
    request.flush({ detail: 'Email déjà utilisé' }, { status: 409, statusText: 'Conflict' });
    expect(component.editError()).toBe('Email déjà utilisé');
  }));
});
