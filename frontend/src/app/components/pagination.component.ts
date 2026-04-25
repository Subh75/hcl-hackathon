import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination" *ngIf="totalPages > 1">
      <button class="page-btn" [disabled]="page <= 0" (click)="goTo(page - 1)">Prev</button>
      <button
        class="page-btn"
        *ngFor="let candidate of pages"
        [class.active]="candidate === page"
        (click)="goTo(candidate)"
      >
        {{ candidate + 1 }}
      </button>
      <button class="page-btn" [disabled]="page >= totalPages - 1" (click)="goTo(page + 1)">Next</button>
    </div>
  `,
  styles: [`
    .pagination {
      display: flex;
      gap: 0.4rem;
      justify-content: flex-end;
      flex-wrap: wrap;
      margin-top: 1rem;
    }

    .page-btn {
      border: 1px solid #b7d2c0;
      background: #f4fbf7;
      color: #2a3d31;
      border-radius: 8px;
      min-width: 2.25rem;
      height: 2.1rem;
      cursor: pointer;
      padding: 0 0.55rem;
      font-weight: 700;
    }

    .page-btn.active {
      border-color: #0a8f56;
      background: #0a8f56;
      color: white;
    }

    .page-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `]
})
export class PaginationComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Output() pageChange = new EventEmitter<number>();

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index);
  }

  goTo(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.pageChange.emit(page);
    }
  }
}
