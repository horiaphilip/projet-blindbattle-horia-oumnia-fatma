import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.html',
  styleUrl: './pagination.scss',
})
export class Pagination {
  @Input() page: number = 0;
  @Input() totalPages: number = 0;
  @Output() pageChange = new EventEmitter<number>();

  public previous() {
    if (this.page > 0) {
      this.pageChange.emit(this.page - 1);
    }
  }

  public next() {
    if (this.page < this.totalPages - 1) {
      this.pageChange.emit(this.page + 1);
    }
  }
}
