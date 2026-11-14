import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, map, Observable, shareReplay, switchMap, throwError } from 'rxjs';
import { Auth } from '../auth/auth';
import { TokenStorage } from '../auth/token-storage';

const PUBLIC_PATHS = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh',
  '/api/auth/verify-email',
  '/api/auth/resend-verification',
  '/api/auth/accept-invite',
];

// One in-flight refresh shared by every request that hits a 401 at the same time
let refresh$: Observable<string> | null = null;

const isPublic = (url: string) => PUBLIC_PATHS.some((p) => url.includes(p));

const withToken = (req: HttpRequest<unknown>, token: string) =>
  req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(Auth);
  const storage = inject(TokenStorage);

  if (isPublic(req.url)) return next(req);

  const token = storage.accessToken;
  const authed = token ? withToken(req, token) : req;

  return next(authed).pipe(
    catchError((err) => {
      if (err instanceof HttpErrorResponse && err.status === 401 && storage.refreshToken) {
        return handle401(req, next, auth);
      }
      return throwError(() => err);
    }),
  );
};

function handle401(req: HttpRequest<unknown>, next: HttpHandlerFn, auth: Auth) {
  refresh$ ??= auth.refresh().pipe(
    map((res) => res.accessToken),
    catchError((e) => {
      auth.logout();
      return throwError(() => e);
    }),
    finalize(() => (refresh$ = null)),
    shareReplay(1),
  );

  return refresh$.pipe(switchMap((token) => next(withToken(req, token))));
}