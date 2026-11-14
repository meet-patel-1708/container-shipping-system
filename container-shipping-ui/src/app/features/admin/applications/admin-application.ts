import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PagedResponse } from '../../../core/auth/auth.models';
import {
  ApplicationStatus, DOC_LABELS, DriverApplication, DriverDocument,
} from '../../../core/driver/driver.model';
import { DriverService } from '../../../core/driver/driver.service';

@Component({
  selector: 'app-admin-applications',
  imports: [
    DatePipe, FormsModule, MatButtonModule, MatButtonToggleModule, MatFormFieldModule,
    MatInputModule, MatPaginatorModule, MatProgressSpinnerModule,
  ],
  templateUrl: './admin-application.html',
  styleUrl: './admin-application.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminApplications {
  private readonly api = inject(DriverService);

  protected readonly labels = DOC_LABELS;
  protected readonly filters: (ApplicationStatus | 'ALL')[] = ['SUBMITTED', 'APPROVED', 'REJECTED', 'DRAFT', 'ALL'];

  protected readonly filter = signal<ApplicationStatus | 'ALL'>('SUBMITTED');
  protected readonly page = signal(0);
  protected readonly result = signal<PagedResponse<DriverApplication> | null>(null);
  protected readonly selected = signal<DriverApplication | null>(null);
  protected readonly loading = signal(false);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly rejecting = signal(false);
  protected reason = '';

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    const f = this.filter();
    this.api.list(f === 'ALL' ? undefined : f, this.page()).subscribe({
      next: (res) => {
        this.result.set(res);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.msg(err));
      },
    });
  }

  protected setFilter(f: ApplicationStatus | 'ALL'): void {
    this.filter.set(f);
    this.page.set(0);
    this.select(null);
    this.load();
  }

  protected onPage(e: PageEvent): void {
    this.page.set(e.pageIndex);
    this.load();
  }

  protected select(a: DriverApplication | null): void {
    this.selected.set(a);
    this.rejecting.set(false);
    this.reason = '';
    this.error.set(null);
  }

  protected approve(a: DriverApplication): void {
    this.busy.set(true);
    this.error.set(null);
    this.api.approve(a.applicationId).subscribe({
      next: (res) => this.done(res),
      error: (err) => this.fail(err),
    });
  }

  protected reject(a: DriverApplication): void {
    if (!this.reason.trim()) {
      this.error.set('Enter a reason for the rejection.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.api.reject(a.applicationId, this.reason.trim()).subscribe({
      next: (res) => this.done(res),
      error: (err) => this.fail(err),
    });
  }

  protected view(d: DriverDocument): void {
    // open the tab first (inside the click), so the popup blocker allows it
    const tab = window.open('', '_blank');
    this.api.adminFile(d.documentId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        if (tab) tab.location.href = url;
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: (err) => {
        tab?.close();
        this.error.set(this.msg(err));
      },
    });
  }

  private done(res: DriverApplication): void {
    this.busy.set(false);
    this.rejecting.set(false);
    this.reason = '';
    this.selected.set(res);
    this.load();
  }

  private fail(err: unknown): void {
    this.busy.set(false);
    this.error.set(this.msg(err));
  }

  private msg(err: unknown): string {
    const e = err as HttpErrorResponse;
    if (e.status === 0) return 'Cannot reach the server.';
    return e.error?.message ?? 'Something went wrong.';
  }
}