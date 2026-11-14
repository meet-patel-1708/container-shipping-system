import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../auth/auth';
import { Role } from '../auth/auth.models';

export const roleGuard = (...roles: Role[]): CanActivateFn => () => {
  const auth = inject(Auth);
  return auth.hasRole(...roles)
    ? true
    : inject(Router).createUrlTree(['/dashboard']);
};