import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { catchError, EMPTY, pipe, switchMap, tap } from 'rxjs';
import { BookingApi } from './booking.api';
import { Booking, BookingQuery, BookingStatus } from './booking.models';

type Query = BookingQuery & { refresh: number };

interface State {
  items: Booking[]; total: number; page: number; size: number;
  status: BookingStatus | ''; refresh: number; loading: boolean; error: string | null;
}

export const BookingStore = signalStore(
  withState<State>({ items: [], total: 0, page: 0, size: 10, status: '', refresh: 0, loading: true, error: null }),
  withComputed(({ page, size, status, refresh, items, loading, error }) => ({
    query: computed<Query>(() => ({ page: page(), size: size(), status: status(), refresh: refresh() })),
    isEmpty: computed(() => !loading() && !error() && items().length === 0),
  })),
  withMethods((store, api = inject(BookingApi)) => ({
    setStatus: (status: BookingStatus | '') => patchState(store, { status, page: 0 }),
    setPage: (page: number, size: number) => patchState(store, { page, size }),
    reload: () => patchState(store, (s) => ({ refresh: s.refresh + 1 })),

    load: rxMethod<Query>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((q) =>
          api.list(q).pipe(
            tap((res) =>
              patchState(store, { items: res.content, total: res.totalElements, loading: false }),
            ),
            catchError((err: HttpErrorResponse) => {
              patchState(store, {
                loading: false,
                error: err.status === 0 ? 'Cannot reach the server.' : (err.error?.message ?? 'Failed to load bookings.'),
              });
              return EMPTY;
            }),
          ),
        ),
      ),
    ),
  })),
  withHooks({ onInit: (store) => store.load(store.query) }),
);