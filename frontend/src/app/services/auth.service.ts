import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenSignal = signal<string | null>(null);
  private readonly customerIdSignal = signal<number | null>(null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {
  }

  login(customerId: number): Observable<LoginResponse> {
    const body: LoginRequest = { customerId };
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, body).pipe(
      tap((response: LoginResponse) => {
        this.tokenSignal.set(response.token);
        this.customerIdSignal.set(customerId);
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
    void this.router.navigate(['/login']);
  }
}
