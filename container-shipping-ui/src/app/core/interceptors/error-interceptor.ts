import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

/** Set on a request's HttpContext when the caller handles errors itself. */
export const SKIP_ERROR_TOAST = new HttpContextToken<boolean>(() => false);

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snack = inject(MatSnackBar);
  const skip = req.context.get(SKIP_ERROR_TOAST) || req.url.includes('/api/auth/');

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (!skip) {
        let msg: string | null = null;
        if (err.status === 0) msg = 'Cannot reach the server. Check your connection.';
        else if (err.status === 403) msg = 'You do not have permission to do that.';
        else if (err.status >= 500) msg = 'Server error. Please try again later.';
        else if (err.status !== 401) {
          const details: string[] | undefined = err.error?.validationErrors;
          msg = details?.length ? details.join('; ') : (err.error?.message ?? 'Request failed.');
        }
        if (msg) snack.open(msg, 'Close', { duration: 4000 });
      }
      return throwError(() => err);
    }),
  );
};