import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  LoginRequest, LoginResponse,
  RefreshRequest, RefreshResponse,
  RegisterRequest, RegisterResponse
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenSignal = signal<string | null>(null);
  private readonly refreshTokenSignal = signal<string | null>(null);
  private readonly customerIdSignal = signal<number | null>(null);
  private readonly roleSignal = signal<string | null>(null);

  /** Used to queue concurrent 401 retries while a refresh is in progress */
  private refreshInProgress = false;
  private refreshSubject = new BehaviorSubject<string | null>(null);

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {
  }

  login(customerId: number, password: string): Observable<LoginResponse> {
    const body: LoginRequest = { customerId, password };
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, body).pipe(
      tap((response: LoginResponse) => {
        this.tokenSignal.set(response.token);
        this.refreshTokenSignal.set(response.refreshToken);
        this.customerIdSignal.set(customerId);
        this.roleSignal.set(response.role);
      })
    );
  }

  register(name: string, password: string): Observable<RegisterResponse> {
    const body: RegisterRequest = { name, password };
    return this.http.post<RegisterResponse>(`${environment.apiUrl}/auth/register`, body);
  }

  refreshAccessToken(): Observable<RefreshResponse> {
    const refreshToken = this.refreshTokenSignal();
    if (!refreshToken) {
      this.logout();
      throw new Error('No refresh token available');
    }

    const body: RefreshRequest = { refreshToken };
    return this.http.post<RefreshResponse>(`${environment.apiUrl}/auth/refresh`, body).pipe(
      tap((response: RefreshResponse) => {
        this.tokenSignal.set(response.token);
        this.refreshTokenSignal.set(response.refreshToken);
      })
    );
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  getRefreshToken(): string | null {
    return this.refreshTokenSignal();
  }

  getCustomerId(): number | null {
    return this.customerIdSignal();
  }

  getRole(): string | null {
    return this.roleSignal();
  }

  isAuthenticated(): boolean {
    return !!this.tokenSignal();
  }

  isRefreshInProgress(): boolean {
    return this.refreshInProgress;
  }

  setRefreshInProgress(value: boolean): void {
    this.refreshInProgress = value;
  }

  getRefreshSubject(): BehaviorSubject<string | null> {
    return this.refreshSubject;
  }

  logout(): void {
    const refreshToken = this.refreshTokenSignal();
    if (refreshToken) {
      this.http.post(`${environment.apiUrl}/auth/logout`, { refreshToken }).subscribe();
    }
    this.tokenSignal.set(null);
    this.refreshTokenSignal.set(null);
    this.customerIdSignal.set(null);
    this.roleSignal.set(null);
    void this.router.navigate(['/login']);
  }
}
