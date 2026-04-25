import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page-shell login-shell">
      <section class="panel login-panel">
        <h1 class="title">Favourite Payee</h1>
        <p class="subtitle">Login with your credentials to manage trusted accounts.</p>

        <label for="customerId" class="field-label">Customer ID</label>
        <input
          id="customerId"
          class="input"
          type="number"
          [formControl]="customerIdControl"
          placeholder="Enter customer id"
        />
        <div class="error-text" *ngIf="customerIdControl.invalid && customerIdControl.touched">
          Valid customer id is required.
        </div>

        <label for="password" class="field-label">Password</label>
        <input
          id="password"
          class="input"
          type="password"
          [formControl]="passwordControl"
          (keydown.enter)="login()"
        />
        <div class="error-text" *ngIf="passwordControl.invalid && passwordControl.touched">
          Password is required.
        </div>

        <div class="error-text" *ngIf="errorMessage">{{ errorMessage }}</div>

        <button class="btn btn-primary login-btn" (click)="login()" [disabled]="loading">
          {{ loading ? 'Signing in...' : 'Login' }}
        </button>

        <p class="register-link">
          Don't have an account? <a routerLink="/register">Register here</a>
        </p>
      </section>
    </div>
  `,
  styles: [`
    .login-shell {
      min-height: 100vh;
      display: grid;
      place-items: center;
    }

    .login-panel {
      width: min(460px, 100%);
      padding: 1.4rem;
      animation: reveal 260ms ease-out;
    }

    .field-label {
      display: block;
      margin-top: 1.05rem;
      margin-bottom: 0.35rem;
      font-weight: 700;
      color: #2f4136;
      font-size: 0.9rem;
    }

    .login-btn {
      margin-top: 1rem;
      width: 100%;
    }

    .register-link {
      margin-top: 1rem;
      text-align: center;
      font-size: 0.9rem;
      color: #555;
    }

    .register-link a {
      color: #1a73e8;
      text-decoration: none;
      font-weight: 600;
    }

    .register-link a:hover {
      text-decoration: underline;
    }

    @keyframes reveal {
      from {
        transform: translateY(12px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }
  `]
})
export class LoginPageComponent {
  readonly customerIdControl = new FormControl<number | null>(null, [Validators.required, Validators.min(1)]);
  readonly passwordControl = new FormControl<string>('', [Validators.required]);

  loading = false;
  errorMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
  }

  login(): void {
    this.errorMessage = '';
    if (this.customerIdControl.invalid || this.customerIdControl.value === null) {
      this.customerIdControl.markAsTouched();
      return;
    }
    if (this.passwordControl.invalid) {
      this.passwordControl.markAsTouched();
      return;
    }

    this.loading = true;
    this.authService.login(this.customerIdControl.value, this.passwordControl.value!).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/payees']);
      },
      error: (error: { error?: { message?: string; errors?: Record<string, string> } }) => {
        this.loading = false;
        this.errorMessage = error.error?.errors?.['password'] || error.error?.errors?.['customerId'] || error.error?.message || 'Login failed';
      }
    });
  }
}
