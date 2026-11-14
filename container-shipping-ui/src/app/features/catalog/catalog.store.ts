import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { catchError, EMPTY, pipe, switchMap, tap } from 'rxjs';
import { CatalogApi } from './catalog.api';
import { ListParams, Product } from './catalog.model';

type CatalogParams = ListParams & { refresh: number };

interface CatalogState {
  items: Product[];
  totalElements: number;
  page: number;
  size: number;
  sort: string;
  query: string;
  refresh: number; // bump to force a refetch (after create/update/delete)
  loading: boolean;
  error: string | null;
}

const initialState: CatalogState = {
  items: [],
  totalElements: 0,
  page: 0,
  size: 12,
  sort: 'createdAt,desc',
  query: '',
  refresh: 0,
  loading: true,
  error: null,
};

// Provided at component level (providers: [CatalogStore]) so state resets when you leave the page.
export const CatalogStore = signalStore(
  withState(initialState),
  withComputed(({ page, size, sort, query, refresh, items, loading, error }) => ({
    params: computed<CatalogParams>(() => ({
      page: page(), size: size(), sort: sort(), query: query(), refresh: refresh(),
    })),
    isEmpty: computed(() => !loading() && !error() && items().length === 0),
  })),
  withMethods((store, api = inject(CatalogApi)) => ({
    setQuery: (query: string) => patchState(store, { query, page: 0 }),
    setSort: (sort: string) => patchState(store, { sort, page: 0 }),
    setPage: (page: number, size: number) => patchState(store, { page, size }),
    reload: () => patchState(store, (s) => ({ refresh: s.refresh + 1 })),

    // Runs whenever the params signal changes; switchMap cancels the stale request.
    load: rxMethod<CatalogParams>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((p) =>
          api.list(p).pipe(
            tap((res) => {
              // Deleted the last item on the last page: step back (changes params -> refetch)
              if (res.content.length === 0 && res.pageNumber > 0) {
                patchState(store, { page: Math.max(res.totalPages - 1, 0) });
                return;
              }
              patchState(store, { items: res.content, totalElements: res.totalElements, loading: false });
            }),
            catchError((err: HttpErrorResponse) => {
              patchState(store, {
                loading: false,
                error: err.status === 0 ? 'Cannot reach the server.' : (err.error?.message ?? 'Failed to load products.'),
              });
              return EMPTY;
            }),
          ),
        ),
      ),
    ),
  })),
  withHooks({
    onInit: (store) => store.load(store.params),
  }),
);