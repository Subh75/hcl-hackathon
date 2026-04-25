import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="page-shell register-shell">
      <section class="panel register-panel">
        <h1 class="title">Create Account</h1>
        <p class="subtitle">Register to start managing your favourite payees.</p>

        <label for="name" class="field-label">Username</label>
        <input
          id="name"
          class="input"
          type="text"
          [formControl]="nameControl"
          placeholder="Choose a username"
        />
        <div class="error-text" *ngIf="nameControl.invalid && nameControl.touched">
          Username is required.
        </div>

        <label for="password" class="field-label">Password</label>
        <input
          id="password"
          class="input"
          type="password"
          [formControl]="passwordControl"
          placeholder="Choose a password (min 6 chars)"
        />
        <div class="error-text" *ngIf="passwordControl.invalid && passwordControl.touched">
          Password must be at least 6 characters.
        </div>

        <label for="confirmPassword" class="field-label">Confirm Password</label>
        <input
          id="confirmPassword"
          class="input"
          type="password"
          [formControl]="confirmPasswordControl"
          placeholder="Confirm your password"
          (keydown.enter)="register()"
        />
        <div class="error-text" *ngIf="confirmPasswordControl.touched && passwordMismatch()">
          Passwords do not match.
        </div>

        <div class="error-text" *ngIf="errorMessage">{{ errorMessage }}</div>

        <div class="success-text" *ngIf="successMessage">{{ successMessage }}</div>

        <button class="btn btn-primary register-btn" (click)="register()" [disabled]="loading">
          {{ loading ? 'Creating account...' : 'Register' }}
        </button>

        <p class="login-link">
          Already have an account? <a routerLink="/login">Login here</a>
        </p>
      </section>
    </div>
  `,
  styles: [`
    .register-shell {
      min-height: 100vh;
      display: grid;
      place-items: center;
    }

    .register-panel {
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

    .register-btn {
      margin-top: 1rem;
      width: 100%;
    }

    .login-link {
      margin-top: 1rem;
      text-align: center;
      font-size: 0.9rem;
      color: #555;
    }

    .login-link a {
      color: #1a73e8;
      text-decoration: none;
      font-weight: 600;
    }

    .login-link a:hover {
      text-decoration: underline;
    }

    .success-text {
      color: #2e7d32;
      font-size: 0.85rem;
      margin-top: 0.5rem;
      font-weight: 600;
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
export class RegisterPageComponent {
  readonly nameControl = new FormControl<string>('', [Validators.required]);
  readonly passwordControl = new FormControl<string>('', [Validators.required, Validators.minLength(6)]);
  readonly confirmPasswordControl = new FormControl<string>('', [Validators.required]);

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
  }

  passwordMismatch(): boolean {
    return this.passwordControl.value !== this.confirmPasswordControl.value;
  }

  register(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.nameControl.invalid) {
      this.nameControl.markAsTouched();
      return;
    }
    if (this.passwordControl.invalid) {
      this.passwordControl.markAsTouched();
      return;
    }
    this.confirmPasswordControl.markAsTouched();
    if (this.passwordMismatch()) {
      return;
    }

    this.loading = true;
    this.authService.register(this.nameControl.value!, this.passwordControl.value!).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = `Account created! Your Customer ID is ${response.customerId}. Redirecting to login...`;
        setTimeout(() => {
          void this.router.navigate(['/login']);
        }, 2500);
      },
      error: (error: { error?: { message?: string; errors?: Record<string, string> } }) => {
        this.loading = false;
        this.errorMessage = error.error?.errors?.['name'] || error.error?.errors?.['password'] || error.error?.message || 'Registration failed';
      }
    });
  }
}
