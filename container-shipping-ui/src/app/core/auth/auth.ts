import { HttpClient } from '@angular/common/http';
import {Injectable,computed,inject,signal} from '@angular/core';
import { Role } from './auth.models';
import { Router } from '@angular/router';
import {Observable,tap,throwError} from 'rxjs';
import { environment }from '../../../environments/environment';
import {AuthResponse,LoginRequest,RegisterRequest,RegistrationResponse,UserResponse} from './auth.models';
import { TokenStorage }from './token-storage';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  readonly role = computed(() => this._user()?.role ?? null);
  private readonly http = inject(HttpClient);
  private readonly storage = inject(TokenStorage);
  private readonly router = inject(Router);
  private readonly base = `${environment.apiUrl}/api/auth`;

  private readonly _user = signal<UserResponse | null>( this.storage.user);

  readonly user = this._user.asReadonly();

  readonly isAuthenticated = computed( () => this._user() !== null);

  readonly isAdmin = computed(() => {
  const r = this._user()?.role;
  return r === 'ADMIN' || r === 'SUPER_ADMIN';
  
});
readonly isSuperAdmin = computed(() => this._user()?.role === 'SUPER_ADMIN');

acceptInvite(token: string, password: string): Observable<string> {
  return this.http.post(`${this.base}/accept-invite`, { token, password }, { responseType: 'text' });
}
hasRole(...roles: Role[]): boolean {
    const r = this._user()?.role;
    return !!r && roles.includes(r);
  }
  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.base}/login`,body)
      .pipe(
        tap((res) =>
          this.persist(res)
        )
      );
  }


  // =========================================================
  // REGISTER
  // =========================================================

  register(
    body: RegisterRequest
  ): Observable<RegistrationResponse> {

    return this.http
      .post<RegistrationResponse>(
        `${this.base}/register`,
        body
      );
  }


  // =========================================================
  // VERIFY EMAIL
  // =========================================================

  verifyEmail(
    token: string
  ): Observable<string> {

    return this.http.post(
      `${this.base}/verify-email`,
      {
        token
      },
      {
        responseType: 'text'
      }
    );
  }


  // =========================================================
  // RESEND VERIFICATION EMAIL
  // =========================================================

  resendVerification(
    email: string
  ): Observable<string> {

    return this.http.post(
      `${this.base}/resend-verification`,
      null,
      {
        params: {
          email
        },
        responseType: 'text'
      }
    );
  }


  // =========================================================
  // REFRESH TOKEN
  // =========================================================

  refresh(): Observable<AuthResponse> {

    const refreshToken =
      this.storage.refreshToken;


    if (!refreshToken) {

      return throwError(
        () =>
          new Error(
            'No refresh token'
          )
      );
    }


    return this.http
      .post<AuthResponse>(
        `${this.base}/refresh`,
        {
          refreshToken
        }
      )
      .pipe(
        tap((res) =>
          this.persist(res)
        )
      );
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {

    this.storage.clear();

    this._user.set(null);


    if (
      !this.router.url.startsWith(
        '/login'
      )
    ) {

      this.router.navigate([
        '/login'
      ]);

    }
  }


  // =========================================================
  // SAVE AUTHENTICATION DATA
  // =========================================================

  private persist(
    res: AuthResponse
  ): void {

    this.storage.save(
      res.accessToken,
      res.refreshToken,
      res.user
    );


    this._user.set(
      res.user
    );
  }
}