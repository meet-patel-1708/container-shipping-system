import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { PagedResponse } from '../../../core/auth/auth.models';
import { Staff, StaffService } from '../../../core/staff/staff.service';
import { FleetLookup } from '../../fleet/fleet-lookup';
import { AdminHiring } from './admin-hiring';
import { AdminJoining } from './admin-joining';
import {AdminResignations} from './admin-resignations';
@Component({
  selector: 'app-admin-staff',
  imports: [MatButtonModule, MatPaginatorModule, AdminHiring, AdminJoining, AdminResignations],
  templateUrl: './admin-staff.html',
  styleUrl: './admin-staff.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminStaff {
  private readonly api = inject(StaffService);
  protected readonly fleet = inject(FleetLookup);
  protected readonly result = signal<PagedResponse<Staff> | null>(null);
  protected readonly page = signal(0);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  constructor() { this.fleet.load(); this.load(); }

  protected load(): void {
    this.api.list(this.page()).subscribe({
      next: (r) => this.result.set(r),
      error: (e) => this.error.set(this.msg(e)),
    });
  }

  protected onPage(e: PageEvent): void { this.page.set(e.pageIndex); this.load(); }

  protected resend(s: Staff): void {
    this.error.set(null); this.info.set(null);
    this.api.resend(s.userId).subscribe({
      next: () => this.info.set(`Invitation re-sent to ${s.email}.`),
      error: (e) => this.error.set(this.msg(e)),
    });
  }

  private msg(err: unknown): string {
    const e = err as HttpErrorResponse;
    if (e.status === 0) return 'Cannot reach the server.';
    return e.error?.message ?? 'Something went wrong.';
  }
}