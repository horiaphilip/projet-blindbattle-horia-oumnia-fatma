import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Pagination } from './pagination';

describe('Pagination', () => {
  let component: Pagination;
  let fixture: ComponentFixture<Pagination>;
  let emitted: number[];

  function setUp(page: number, totalPages: number) {
    fixture.componentRef.setInput('page', page);
    fixture.componentRef.setInput('totalPages', totalPages);
    fixture.detectChanges();
  }

  function button(name: string): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button.' + name);
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Pagination],
    }).compileComponents();

    fixture = TestBed.createComponent(Pagination);
    component = fixture.componentInstance;
    emitted = [];
    component.pageChange.subscribe((page) => emitted.push(page));
  });

  it('should create', () => {
    setUp(0, 1);
    expect(component).toBeTruthy();
  });

  it('should disable the previous button on the first page', () => {
    setUp(0, 3);
    expect(button('previous').disabled).toBeTrue();
    expect(button('next').disabled).toBeFalse();
  });

  it('should disable the next button on the last page', () => {
    setUp(2, 3);
    expect(button('previous').disabled).toBeFalse();
    expect(button('next').disabled).toBeTrue();
  });

  it('should emit the next page', () => {
    setUp(1, 3);
    button('next').click();
    expect(emitted).toEqual([2]);
  });

  it('should emit the previous page', () => {
    setUp(1, 3);
    button('previous').click();
    expect(emitted).toEqual([0]);
  });

  it('should display the position', () => {
    setUp(1, 3);
    expect(fixture.nativeElement.textContent).toContain('Page 2 / 3');
  });

  it('should be hidden when there is only one page', () => {
    setUp(0, 1);
    expect(fixture.nativeElement.querySelector('nav')).toBeNull();
  });
});
