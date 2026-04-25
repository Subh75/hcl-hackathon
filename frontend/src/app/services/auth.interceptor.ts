import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { catchError, switchMap, filter, take, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Don't attach token to auth endpoints (login, register, refresh)
  const isAuthUrl = request.url.includes('/auth/');
  if (!token || isAuthUrl) {
    return next(request);
  }

  const requestWithToken = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(requestWithToken).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthUrl) {
        // Try to refresh the token
        if (!authService.isRefreshInProgress()) {
          authService.setRefreshInProgress(true);
          authService.getRefreshSubject().next(null);

          return authService.refreshAccessToken().pipe(
            switchMap((response) => {
              authService.setRefreshInProgress(false);
              authService.getRefreshSubject().next(response.token);

              // Retry original request with new token
              const retryRequest = request.clone({
                setHeaders: {
                  Authorization: `Bearer ${response.token}`
                }
              });
              return next(retryRequest);
            }),
            catchError((refreshError) => {
              authService.setRefreshInProgress(false);
              authService.logout();
              return throwError(() => refreshError);
            })
          );
        } else {
          // Wait for the ongoing refresh to complete, then retry
          return authService.getRefreshSubject().pipe(
            filter(token => token !== null),
            take(1),
            switchMap((newToken) => {
              const retryRequest = request.clone({
                setHeaders: {
                  Authorization: `Bearer ${newToken}`
                }
              });
              return next(retryRequest);
            })
          );
        }
      }
      return throwError(() => error);
    })
  );
};
