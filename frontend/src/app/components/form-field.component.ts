import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-form-field',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <label class="field" [attr.for]="id">
      <span class="label">{{ label }}</span>
      <input
        class="input"
        [id]="id"
        [type]="type"
        [placeholder]="placeholder"
        [formControl]="control"
        [attr.maxlength]="maxLength ?? null"
        [readonly]="readonly"
      />
      <div class="error-text" *ngIf="showError()">{{ errorMessage }}</div>
    </label>
  `,
  styles: [`
    .field {
      display: block;
      margin-bottom: 0.9rem;
    }

    .label {
      display: block;
      margin-bottom: 0.35rem;
      font-size: 0.88rem;
      font-weight: 700;
      color: #30463a;
    }
  `]
})
export class FormFieldComponent {
  @Input({ required: true }) label!: string;
  @Input({ required: true }) id!: string;
  @Input({ required: true }) control!: FormControl<unknown>;
  @Input() type = 'text';
  @Input() placeholder = '';
  @Input() maxLength?: number;
  @Input() readonly = false;
  @Input() errorMessage = 'Invalid value';

  showError(): boolean {
    return this.control.invalid && (this.control.dirty || this.control.touched);
  }
}
