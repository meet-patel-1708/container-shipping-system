import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PagedResponse } from '../../../core/auth/auth.models';
import { AdminKyc, AdminKycService } from '../../../core/kyc/admin-kyc.serivce';
import { KycStatus } from '../../../core/profile/profile.service';

@Component({
  selector: 'app-admin-kyc',
  imports: [MatButtonModule, MatButtonToggleModule, MatPaginatorModule, MatProgressSpinnerModule],
  templateUrl: './admin-kyc.html',
  styleUrl: './admin-kyc.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminKycPage {
  private readonly api = inject(AdminKycService);

  protected readonly filters: (KycStatus | 'ALL')[] = ['PENDING', 'VERIFIED', 'REJECTED', 'ALL'];
  protected readonly filter = signal<KycStatus | 'ALL'>('PENDING');
  protected readonly page = signal(0);
  protected readonly result = signal<PagedResponse<AdminKyc> | null>(null);
  protected readonly selected = signal<AdminKyc | null>(null);
  protected readonly loading = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  constructor() { this.load(); }

  protected load(): void {
    this.loading.set(true);
    const f = this.filter();
    this.api.list(f === 'ALL' ? undefined : f, this.page()).subscribe({
      next: (r) => {
        this.result.set(r);
        this.loading.set(false);
        // keep the detail panel in sync with the refreshed row
        const sel = this.selected();
        if (sel) this.selected.set(r.content.find((k) => k.kycId === sel.kycId) ?? null);
      },
      error: (e) => { this.loading.set(false); this.error.set(this.msg(e)); },
    });
  }

  protected setFilter(f: KycStatus | 'ALL'): void {
    this.filter.set(f);
    this.page.set(0);
    this.selected.set(null);
    this.clear();
    this.load();
  }

  protected onPage(e: PageEvent): void { this.page.set(e.pageIndex); this.load(); }

  protected select(k: AdminKyc): void { this.selected.set(k); this.clear(); }

  protected decide(k: AdminKyc, status: KycStatus): void {
    this.clear();
    this.busy.set(true);
    this.api.setStatus(k.kycId, status).subscribe({
      next: () => {
        this.busy.set(false);
        this.info.set(`${k.userName}: KYC marked ${status}.`);
        this.selected.set(null);
        this.load();
      },
      error: (e) => { this.busy.set(false); this.error.set(this.msg(e)); },
    });
  }

  private clear(): void { this.error.set(null); this.info.set(null); }

  private msg(err: unknown): string {
    const e = err as HttpErrorResponse;
    if (e.status === 0) return 'Cannot reach the server.';
    return e.error?.message ?? 'Something went wrong.';
  }
}