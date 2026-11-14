import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  RegisterRequest,
  RegistrationResponse,
  LoginRequest,
  AuthResponse
} from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);


  /*
   * Angular
   *    ↓
   * API Gateway :8080
   *    ↓
   * Auth Service :8081
   */

  private baseUrl = 'http://localhost:8080/api/auth';


  // =========================================================
  // REGISTER
  // =========================================================

  register(
    request: RegisterRequest
  ): Observable<RegistrationResponse> {

    return this.http.post<RegistrationResponse>(
      `${this.baseUrl}/register`,
      request
    );
  }


  // =========================================================
  // LOGIN
  // =========================================================

  login(
    request: LoginRequest
  ): Observable<AuthResponse> {

    return this.http.post<AuthResponse>(
      `${this.baseUrl}/login`,
      request
    );
  }


  // =========================================================
  // VERIFY EMAIL
  // =========================================================

  verifyEmail(
    token: string
  ): Observable<string> {

    return this.http.post(
      `${this.baseUrl}/verify-email`,
      {
        token: token
      },
      {
        responseType: 'text'
      }
    );
  }


  // =========================================================
  // RESEND VERIFICATION
  // =========================================================

  resendVerification(
    email: string
  ): Observable<string> {

    return this.http.post(
      `${this.baseUrl}/resend-verification?email=${encodeURIComponent(email)}`,
      {},
      {
        responseType: 'text'
      }
    );
  }


  // =========================================================
  // SAVE TOKENS
  // =========================================================

  saveTokens(
    response: AuthResponse
  ): void {

    localStorage.setItem(
      'accessToken',
      response.accessToken
    );

    localStorage.setItem(
      'refreshToken',
      response.refreshToken
    );
  }


  // =========================================================
  // GET ACCESS TOKEN
  // =========================================================

  getAccessToken(): string | null {

    return localStorage.getItem(
      'accessToken'
    );
  }


  // =========================================================
  // LOGOUT
  // =========================================================

  logout(): void {

    localStorage.removeItem(
      'accessToken'
    );

    localStorage.removeItem(
      'refreshToken'
    );
  }
}