import { HttpErrorResponse } from '@angular/common/http';
import { computed, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, Observable, switchMap, tap } from 'rxjs';
import { PagedResponse } from '../../core/auth/auth.models';
import { ListQuery } from './fleet.model';

/** Call from a component field initializer (needs an injection context). */
export function pagedList<T>(
  fetch: (q: ListQuery) => Observable<PagedResponse<T>>,
  defaultSort: string,
) {
  const page = signal(0);
  const size = signal(10);
  const query = signal('');
  const sort = signal(defaultSort);
  const refresh = signal(0);
  const items = signal<T[]>([]);
  const total = signal(0);
  const loading = signal(true);
  const error = signal<string | null>(null);

  const params = computed(() => ({
    page: page(),
    size: size(),
    query: query(),
    sort: sort(),
    refresh: refresh(),
  }));

  toObservable(params)
    .pipe(
      tap(() => {
        loading.set(true);
        error.set(null);
      }),
      switchMap((q) =>
        fetch(q).pipe(
          tap((res) => {
            // deleted the last item on the last page: step back (changes params, refetches)
            if (res.content.length === 0 && res.pageNumber > 0) {
              page.set(Math.max(res.totalPages - 1, 0));
              return;
            }
            items.set(res.content);
            total.set(res.totalElements);
            loading.set(false);
          }),
          catchError((err: HttpErrorResponse) => {
            loading.set(false);
            error.set(err.status === 0 ? 'Cannot reach the server.' : 'Failed to load.');
            return EMPTY;
          }),
        ),
      ),
      takeUntilDestroyed(),
    )
    .subscribe();

  return {
    items,
    total,
    loading,
    error,
    page,
    size,
    sort,
    defaultSort,
    isEmpty: computed(() => !loading() && !error() && items().length === 0),
    setQuery: (q: string) => {
      query.set(q);
      page.set(0);
    },
    setSort: (s: string) => {
      sort.set(s);
      page.set(0);
    },
    setPage: (p: number, s: number) => {
      page.set(p);
      size.set(s);
    },
    reload: () => refresh.update((n) => n + 1),
  };
}