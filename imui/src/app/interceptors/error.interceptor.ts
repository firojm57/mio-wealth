import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected server error occurred.';

      if (error.status === 401) {
        if (req.url.includes('/auth/login')) {
          errorMessage = error.error?.message || 'Invalid user ID or password. Please verify your credentials.';
        } else {
          errorMessage = 'Your session has expired. Please sign in again.';
          authService.logout();
        }
      } else if (error.status === 403) {
        errorMessage = 'Access denied: You do not have permission for this resource.';
      } else if (error.status === 404) {
        errorMessage = 'Requested financial resource was not found.';
      } else if (error.status >= 500) {
        errorMessage = error.error?.message || 'Server connection error. Please try again later.';
      } else if (error.status === 0) {
        // Backend offline / network error: handled quietly by feature services with fallback
        errorMessage = 'Unable to connect to backend server. Operating in offline resilient mode.';
      } else if (error.error?.message) {
        errorMessage = error.error.message;
      }

      toastService.showToast(errorMessage, 'danger');
      return throwError(() => error);
    })
  );
};
