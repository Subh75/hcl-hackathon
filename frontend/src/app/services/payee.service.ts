import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PayeeDto, PayeeListResponse, PayeePayload } from '../models/payee.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PayeeService {
  constructor(
    private readonly http: HttpClient,
    private readonly authService: AuthService
  ) {
  }

  getPayees(page: number, search: string): Observable<PayeeListResponse> {
    return this.http.get<PayeeListResponse>(`${this.baseUrl()}?page=${page}&size=5&search=${encodeURIComponent(search)}`);
  }

  getPayee(id: number): Observable<PayeeDto> {
    return this.http.get<PayeeDto>(`${this.baseUrl()}/${id}`);
  }

  addPayee(payload: PayeePayload): Observable<PayeeDto> {
    return this.http.post<PayeeDto>(this.baseUrl(), payload);
  }

  updatePayee(id: number, payload: PayeePayload): Observable<PayeeDto> {
    return this.http.put<PayeeDto>(`${this.baseUrl()}/${id}`, payload);
  }

  deletePayee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl()}/${id}`);
  }

  logInteraction(payeeId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl()}/${payeeId}/interact`, {});
  }

  private baseUrl(): string {
    const customerId = this.authService.getCustomerId();
    if (customerId === null) {
      throw new Error('No authenticated customer available');
    }
    return `${environment.apiUrl}/customers/${customerId}/payees`;
  }
}
