import { Injectable } from '@angular/core';
import { UserResponse } from './auth.models';

const ACCESS = 'cs_access_token';
const REFRESH = 'cs_refresh_token';
const USER = 'cs_user';
@Injectable({
  providedIn: 'root',
})
export class TokenStorage {
  get accessToken():string | null{
    return localStorage.getItem(ACCESS);
  }
  get refreshToken():string|null {
    return localStorage.getItem(REFRESH);
  }
  get user(): UserResponse | null {
  const raw = localStorage.getItem(USER);
  if (!raw || !this.refreshToken) return null;
  try {
    const u = JSON.parse(raw) as UserResponse;
    if (!u.role || u.userId == null || u.emailVerified === undefined) { this.clear(); return null; }
    return u;
  } catch {
    return null;
  }
}
  save(access:string,refresh:string,user:UserResponse):void{
    localStorage.setItem(ACCESS,access);
    localStorage.setItem(REFRESH,refresh);
    localStorage.setItem(USER,JSON.stringify(user));
  }
  clear():void{
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
    localStorage.removeItem(USER);
  }
}
