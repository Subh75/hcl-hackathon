import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly TOKEN_STORAGE_KEY = 'favourite-payee.token';
  private static readonly CUSTOMER_ID_STORAGE_KEY = 'favourite-payee.customerId';

  private readonly tokenSignal = signal<string | null>(null);
  private readonly customerIdSignal = signal<number | null>(null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {
    const storedToken = localStorage.getItem(AuthService.TOKEN_STORAGE_KEY);
    const storedCustomerId = localStorage.getItem(AuthService.CUSTOMER_ID_STORAGE_KEY);

    if (storedToken) {
      this.tokenSignal.set(storedToken);
    }

    if (storedCustomerId) {
      const parsedCustomerId = Number(storedCustomerId);
      if (!Number.isNaN(parsedCustomerId)) {
        this.customerIdSignal.set(parsedCustomerId);
      }
    }
  }

  login(customerId: number): Observable<LoginResponse> {
    const body: LoginRequest = { customerId };
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, body).pipe(
      tap((response: LoginResponse) => {
        this.tokenSignal.set(response.token);
        this.customerIdSignal.set(customerId);
        localStorage.setItem(AuthService.TOKEN_STORAGE_KEY, response.token);
        localStorage.setItem(AuthService.CUSTOMER_ID_STORAGE_KEY, String(customerId));
      })
    );
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  getCustomerId(): number | null {
    return this.customerIdSignal();
  }

  isAuthenticated(): boolean {
    return !!this.tokenSignal();
  }

  logout(): void {
    this.tokenSignal.set(null);
    this.customerIdSignal.set(null);
    localStorage.removeItem(AuthService.TOKEN_STORAGE_KEY);
    localStorage.removeItem(AuthService.CUSTOMER_ID_STORAGE_KEY);
    void this.router.navigate(['/login']);
  }
}
