import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule, MatButtonToggleGroup, MatButtonToggle } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule, MatIcon } from '@angular/material/icon';
import { MatMenuModule, MatMenu } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent, MatPaginator } from '@angular/material/paginator';
import { MatProgressBarModule, MatProgressBar } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { Container, ContainerStatus } from '../../booking/booking.models';
import { pagedList } from '../../fleet/paged-list';
import { ContainerApi } from '../container.api';

const FILTERS: { label: string; value: ContainerStatus | '' }[] = [
  { label: 'All', value: '' },
  { label: 'Available', value: 'AVAILABLE' },
  { label: 'In transit', value: 'IN_TRANSIT' },
  { label: 'Maintenance', value: 'MAINTENANCE' },
  { label: 'Retired', value: 'RETIRED' },
];

@Component({
  selector: 'app-container-list',
  imports: [DatePipe,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatMenuModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule],
  templateUrl: './container-list.html',
  styleUrl: './container-list.scss',
})
export class ContainerList {
  private readonly api = inject(ContainerApi);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly filters = FILTERS;
  protected readonly cols = ['id', 'company', 'type', 'size', 'status', 'created', 'actions'];
  protected status: ContainerStatus | '' = '';

  // reuses the fleet paging helper; its "query" field carries the status filter
  protected readonly list = pagedList<Container>((q) => this.api.list(q), 'createdAt,desc');

  protected setFilter(s: ContainerStatus | ''): void {
    this.status = s;
    this.list.setQuery(s);
  }

  protected onPage(e: PageEvent): void {
    this.list.setPage(e.pageIndex, e.pageSize);
  }

  protected async openCreate(): Promise<void> {
    const { ContainerDialog } = await import('../container-dialog');
    this.dialog.open(ContainerDialog, { width: '480px', maxWidth: '95vw' })
      .afterClosed().subscribe((created) => {
        if (!created?.length) return;
        this.snack.open(`${created.length} container${created.length > 1 ? 's' : ''} created`, 'Close', { duration: 3000 });
        this.list.reload();
      });
  }

  protected change(c: Container, status: ContainerStatus): void {
    this.api.setStatus(c.containerId, status).subscribe({
      next: () => { this.snack.open(`Container #${c.containerId} is now ${status.toLowerCase()}`, 'Close', { duration: 3000 }); this.list.reload(); },
      error: (e: HttpErrorResponse) =>
        this.snack.open(e.status === 409 ? 'A container in transit cannot be changed.' : 'Could not change the status.', 'Close', { duration: 4000 }),
    });
  }
}