import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { FormFieldComponent } from '../components/form-field.component';
import { PayeeDto, PayeePayload } from '../models/payee.model';
import { PayeeService } from '../services/payee.service';

@Component({
  selector: 'app-payee-form-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormFieldComponent],
  template: `
    <div class="page-shell">
      <section class="panel form-panel">
        <h1 class="title">{{ editing ? 'Edit payee account' : 'Add payee account' }}</h1>
        <p class="subtitle">Bank is auto-resolved from IBAN digits 4-7.</p>

        <form [formGroup]="form" (ngSubmit)="save()" novalidate>
          <app-form-field
            id="name"
            label="Name"
            [control]="nameControl"
            [errorMessage]="nameError()"
            placeholder="Enter payee name"
          ></app-form-field>

          <app-form-field
            id="iban"
            label="IBAN"
            [control]="ibanControl"
            [maxLength]="20"
            [errorMessage]="ibanError()"
            placeholder="Enter IBAN"
          ></app-form-field>

          <label class="field" for="bank">
            <span class="field-title">Bank</span>
            <input id="bank" class="input" [formControl]="bankControl" readonly />
            <div class="error-text" *ngIf="bankControl.invalid && (bankControl.touched || bankControl.dirty)">
              Bank is required and auto-calculated from IBAN.
            </div>
          </label>

          <div class="error-text" *ngIf="fieldErrors['name']">{{ fieldErrors['name'] }}</div>
          <div class="error-text" *ngIf="fieldErrors['iban']">{{ fieldErrors['iban'] }}</div>
          <div class="error-text" *ngIf="errorMessage">{{ errorMessage }}</div>

          <div class="actions">
            <button class="btn btn-primary" type="submit" [disabled]="saving">{{ saving ? 'Saving...' : 'Save' }}</button>
            <button class="btn btn-secondary" type="button" (click)="cancel()">Cancel</button>
            <button
              *ngIf="editing"
              class="btn btn-danger"
              type="button"
              (click)="deletePayee()"
              [disabled]="saving"
            >
              Delete
            </button>
          </div>
        </form>
      </section>
    </div>
  `,
  styles: [`
    .form-panel {
      width: min(700px, 100%);
      margin: 0 auto;
      padding: 1.35rem;
      animation: rise 250ms ease-out;
    }

    form {
      margin-top: 1rem;
    }

    .field {
      display: block;
      margin-bottom: 0.9rem;
    }

    .field-title {
      display: block;
      margin-bottom: 0.35rem;
      font-size: 0.88rem;
      font-weight: 700;
      color: #30463a;
    }

    .actions {
      margin-top: 1.2rem;
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem;
    }

    @keyframes rise {
      from {
        transform: translateY(14px);
        opacity: 0;
      }
      to {
        transform: translateY(0);
        opacity: 1;
      }
    }
  `]
})
export class PayeeFormPageComponent implements OnInit {
  readonly form = new FormGroup({
    name: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[a-zA-Z0-9 '\\-]+$/)]
    }),
    iban: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[a-zA-Z0-9]+$/), Validators.maxLength(20)]
    }),
    bank: new FormControl<string>({ value: '', disabled: true }, {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  editing = false;
  payeeId: number | null = null;
  saving = false;
  errorMessage = '';
  fieldErrors: Record<string, string> = {};

  private readonly bankCodeMap: Record<string, string> = {
    '1234': 'Nairobi Bank',
    '1235': 'Denver Bank',
    '1236': 'Moscow Bank',
    '1237': 'Tokio Bank'
  };

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly payeeService: PayeeService
  ) {
  }

  get nameControl(): FormControl<string> {
    return this.form.controls.name;
  }

  get ibanControl(): FormControl<string> {
    return this.form.controls.iban;
  }

  get bankControl(): FormControl<string> {
    return this.form.controls.bank;
  }

  ngOnInit(): void {
    const routeId = this.route.snapshot.paramMap.get('id');
    if (routeId) {
      this.editing = true;
      this.payeeId = Number(routeId);
      this.loadPayee(this.payeeId);
    }

    this.ibanControl.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value: string) => {
        this.setBankFromIban(value);
      });
  }

  save(): void {
    this.errorMessage = '';
    this.fieldErrors = {};

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.bankControl.value) {
      this.errorMessage = 'Bank code from IBAN was not recognized.';
      return;
    }

    const payload: PayeePayload = {
      name: this.nameControl.value.trim(),
      iban: this.ibanControl.value.trim().toUpperCase()
    };

    this.saving = true;
    const request$ = this.editing && this.payeeId !== null
      ? this.payeeService.updatePayee(this.payeeId, payload)
      : this.payeeService.addPayee(payload);

    request$.subscribe({
      next: () => {
        this.saving = false;
        void this.router.navigate(['/payees']);
      },
      error: (error: { error?: { message?: string; errors?: Record<string, string> } }) => {
        this.saving = false;
        this.errorMessage = error.error?.message || 'Save failed';
        this.fieldErrors = error.error?.errors || {};
      }
    });
  }

  deletePayee(): void {
    if (!this.editing || this.payeeId === null) {
      return;
    }
    if (!window.confirm('Delete this payee account?')) {
      return;
    }

    this.saving = true;
    this.payeeService.deletePayee(this.payeeId).subscribe({
      next: () => {
        this.saving = false;
        void this.router.navigate(['/payees']);
      },
      error: (error: { error?: { message?: string } }) => {
        this.saving = false;
        this.errorMessage = error.error?.message || 'Delete failed';
      }
    });
  }

  cancel(): void {
    void this.router.navigate(['/payees']);
  }

  nameError(): string {
    if (this.nameControl.hasError('required')) {
      return 'Name is required';
    }
    if (this.nameControl.hasError('pattern')) {
      return "Name must match [a-zA-Z0-9 '\\-]+";
    }
    return 'Invalid name';
  }

  ibanError(): string {
    if (this.ibanControl.hasError('required')) {
      return 'IBAN is required';
    }
    if (this.ibanControl.hasError('pattern')) {
      return 'IBAN must be alphanumeric';
    }
    if (this.ibanControl.hasError('maxlength')) {
      return 'IBAN must be at most 20 characters';
    }
    return 'Invalid IBAN';
  }

  private loadPayee(payeeId: number): void {
    this.payeeService.getPayee(payeeId).subscribe({
      next: (payee: PayeeDto) => {
        this.form.patchValue({
          name: payee.name,
          iban: payee.iban,
          bank: payee.bank
        });
      },
      error: (error: { error?: { message?: string } }) => {
        this.errorMessage = error.error?.message || 'Unable to load payee';
      }
    });
  }

  private setBankFromIban(iban: string): void {
    const trimmed = iban.trim().toUpperCase();
    if (trimmed.length < 8 || !/^[a-zA-Z0-9]+$/.test(trimmed)) {
      this.bankControl.setValue('', { emitEvent: false });
      return;
    }

    const code = trimmed.substring(4, 8);
    const bank = this.bankCodeMap[code] || '';
    this.bankControl.setValue(bank, { emitEvent: false });
  }
}
