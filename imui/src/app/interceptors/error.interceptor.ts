import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';

const STATUS_MESSAGES: Record<number, string> = {
  0: 'Unable to connect to backend server. Operating in offline resilient mode.',
  403: 'Access denied: You do not have permission for this resource.',
  404: 'Requested financial resource was not found.',
  500: 'Server connection error. Please try again later.'
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = error.error?.message;

      if (error.status === 401) {
        if (req.url.includes('/auth/login')) {
          errorMessage = errorMessage || 'Invalid user ID or password. Please verify your credentials.';
        } else {
          errorMessage = 'Your session has expired. Please sign in again.';
          authService.logout();
        }
      } else if (!errorMessage) {
        errorMessage = STATUS_MESSAGES[error.status] || (error.status >= 500 ? STATUS_MESSAGES[500] : 'An unexpected server error occurred.');
      }

      toastService.showToast(errorMessage, 'danger');
      return throwError(() => error);
    })
  );
};
