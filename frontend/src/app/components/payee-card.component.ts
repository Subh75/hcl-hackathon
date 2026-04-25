import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PayeeDto } from '../models/payee.model';

@Component({
  selector: 'app-payee-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <article class="payee-card panel">
      <div class="avatar">{{ initials() }}</div>
      <div class="meta">
        <div class="name-row">
          <h4>{{ payee.name }}</h4>
          <span class="smart" *ngIf="smart">*</span>
        </div>
        <p>IBAN: {{ payee.iban }}</p>
        <p>Bank: {{ payee.bank }}</p>
      </div>
      <button class="btn btn-secondary edit-btn" (click)="edit.emit()">Edit</button>
    </article>
  `,
  styles: [`
    .payee-card {
      display: grid;
      grid-template-columns: 50px 1fr auto;
      gap: 0.85rem;
      padding: 0.9rem;
      align-items: center;
      border-radius: 14px;
    }

    .avatar {
      width: 50px;
      height: 50px;
      border-radius: 14px;
      background: linear-gradient(150deg, #0a8f56, #13a96a);
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      letter-spacing: 0.03em;
    }

    .meta h4 {
      margin: 0;
      font-size: 1rem;
    }

    .meta p {
      margin: 0.2rem 0 0;
      color: #516258;
      font-size: 0.86rem;
    }

    .name-row {
      display: flex;
      align-items: center;
      gap: 0.35rem;
    }

    .smart {
      color: #b78300;
      font-weight: 900;
    }

    .edit-btn {
      align-self: center;
    }

    @media (max-width: 700px) {
      .payee-card {
        grid-template-columns: 44px 1fr;
      }

      .edit-btn {
        grid-column: 1 / -1;
      }
    }
  `]
})
export class PayeeCardComponent {
  @Input({ required: true }) payee!: PayeeDto;
  @Input() smart = false;
  @Output() edit = new EventEmitter<void>();

  initials(): string {
    const segments = this.payee.name.split(' ').filter(Boolean);
    return segments.slice(0, 2).map((part: string) => part[0].toUpperCase()).join('') || 'NA';
  }
}
