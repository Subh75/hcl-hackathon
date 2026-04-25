import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PaginationComponent } from '../components/pagination.component';
import { PayeeCardComponent } from '../components/payee-card.component';
import { PayeeListResponse } from '../models/payee.model';
import { AuthService } from '../services/auth.service';
import { PayeeService } from '../services/payee.service';

@Component({
  selector: 'app-payee-list-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, PayeeCardComponent, PaginationComponent],
  template: `
    <div class="page-shell">
      <section class="panel list-panel">
        <header class="header">
          <div>
            <h1 class="title">Favourite Accounts</h1>
            <p class="subtitle">Smart suggestions adapt to interaction patterns and time habits.</p>
          </div>
          <div class="actions">
            <button class="btn btn-secondary" (click)="logout()">Logout</button>
            <button class="btn btn-primary" (click)="navigateToAdd()">Add new account</button>
          </div>
        </header>

        <input class="input search" [formControl]="searchControl" placeholder="Search by name, iban, or bank" />

        <div *ngIf="errorMessage" class="error-text">{{ errorMessage }}</div>

        <section class="smart-block" *ngIf="data?.smartFavourites?.length">
          <h3>Smart Favourites</h3>
          <div class="card-stack">
            <app-payee-card
              *ngFor="let payee of data?.smartFavourites"
              [payee]="payee"
              [smart]="true"
              (select)="onSelect(payee.id)"
              (edit)="onEdit(payee.id)"
            ></app-payee-card>
          </div>
        </section>

        <section class="all-block">
          <h3>All Accounts</h3>
          <div *ngIf="loading" class="subtitle">Loading payees...</div>
          <div class="card-stack" *ngIf="!loading">
            <app-payee-card
              *ngFor="let payee of data?.all?.content || []"
              [payee]="payee"
              (select)="onSelect(payee.id)"
              (edit)="onEdit(payee.id)"
            ></app-payee-card>
          </div>
          <p *ngIf="!loading && (data?.all?.content?.length || 0) === 0" class="subtitle">No payees to show.</p>

          <app-pagination
            [page]="page"
            [totalPages]="data?.all?.totalPages || 0"
            (pageChange)="changePage($event)"
          ></app-pagination>
        </section>
      </section>
    </div>
  `,
  styles: [`
    .list-panel {
      padding: 1.25rem;
      animation: fade-slide 300ms ease-out;
    }

    .header {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      align-items: flex-start;
      flex-wrap: wrap;
      margin-bottom: 1rem;
    }

    .actions {
      display: flex;
      gap: 0.55rem;
      flex-wrap: wrap;
    }

    .search {
      margin-bottom: 1rem;
    }

    .smart-block,
    .all-block {
      margin-top: 1rem;
    }

    h3 {
      margin: 0 0 0.7rem;
      font-family: 'Space Grotesk', sans-serif;
      color: #24362b;
    }

    .card-stack {
      display: grid;
      gap: 0.7rem;
    }

    @keyframes fade-slide {
      from {
        opacity: 0;
        transform: translateY(10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `]
})
export class PayeeListPageComponent implements OnInit {
  readonly searchControl = new FormControl<string>('', { nonNullable: true });

  data: PayeeListResponse | null = null;
  loading = false;
  page = 0;
  searchTerm = '';
  errorMessage = '';

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly payeeService: PayeeService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
  }

  ngOnInit(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((value: string) => {
        this.page = 0;
        this.searchTerm = value;
        this.loadPayees();
      });

    this.loadPayees();
  }

  navigateToAdd(): void {
    void this.router.navigate(['/payees/add']);
  }

  changePage(page: number): void {
    this.page = page;
    this.loadPayees();
  }

  onSelect(payeeId: number): void {
    this.payeeService.logInteraction(payeeId).subscribe({
      next: () => {
        void this.router.navigate(['/payees/view', payeeId]);
      },
      error: () => {
        void this.router.navigate(['/payees/view', payeeId]);
      }
    });
  }

  onEdit(payeeId: number): void {
    void this.router.navigate(['/payees/edit', payeeId]);
  }

  logout(): void {
    this.authService.logout();
  }

  private loadPayees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.payeeService.getPayees(this.page, this.searchTerm).subscribe({
      next: (response: PayeeListResponse) => {
        this.data = response;
        this.loading = false;
      },
      error: (error: { error?: { message?: string } }) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Unable to load payees';
      }
    });
  }
}
