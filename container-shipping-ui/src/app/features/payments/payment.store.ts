import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withHooks, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { catchError, EMPTY, pipe, switchMap, tap } from 'rxjs';
import { PaymentApi } from './payment.api';
import { Payment, PaymentQuery, PaymentStatus } from './payment.models';

type Query = PaymentQuery & { refresh: number };

interface State {
  items: Payment[]; total: number; page: number; size: number;
  status: PaymentStatus | ''; refresh: number; loading: boolean; error: string | null;
}

export const PaymentStore = signalStore(
  withState<State>({ items: [], total: 0, page: 0, size: 10, status: '', refresh: 0, loading: true, error: null }),
  withComputed(({ page, size, status, refresh, items, loading, error }) => ({
    query: computed<Query>(() => ({ page: page(), size: size(), status: status(), refresh: refresh() })),
    isEmpty: computed(() => !loading() && !error() && items().length === 0),
    pendingCount: computed(() => items().filter((p) => p.paymentStatus === 'PENDING').length),
  })),
  withMethods((store, api = inject(PaymentApi)) => ({
    setStatus: (status: PaymentStatus | '') => patchState(store, { status, page: 0 }),
    setPage: (page: number, size: number) => patchState(store, { page, size }),
    reload: () => patchState(store, (s) => ({ refresh: s.refresh + 1 })),

    load: rxMethod<Query>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap((q) =>
          api.list(q).pipe(
            tap((res) => patchState(store, { items: res.content, total: res.totalElements, loading: false })),
            catchError((err: HttpErrorResponse) => {
              patchState(store, {
                loading: false,
                error: err.status === 0 ? 'Cannot reach the server.' : (err.error?.message ?? 'Failed to load payments.'),
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