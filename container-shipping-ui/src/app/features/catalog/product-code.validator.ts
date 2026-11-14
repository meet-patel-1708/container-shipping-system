import { AsyncValidatorFn } from '@angular/forms';
import { map, of, switchMap, timer } from 'rxjs';
import { CatalogApi } from './catalog.api';

/** Debounced: Angular unsubscribes the previous run when the value changes, so the timer cancels it. */
export function productCodeAvailable(api: CatalogApi): AsyncValidatorFn {
  return (control) => {
    const code = (control.value ?? '') as string;
    if (!/^PC\d{5}$/.test(code)) return of(null); // the pattern validator reports this
    return timer(400).pipe(
      switchMap(() => api.codeExists(code)),
      map((exists) => (exists ? { codeTaken: true } : null)),
    );
  };
}