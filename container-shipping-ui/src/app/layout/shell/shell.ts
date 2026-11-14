import { BreakpointObserver } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Auth } from '../../core/auth/auth';
import { Role } from '../../core/auth/auth.models';
import { DestroyRef, effect } from '@angular/core';   
import { StatusWatcher } from '../../core/notify/status-watcher';
interface NavItem {
  label: string;
  icon: string;
  link: string;
  roles: Role[];
}

const ADMINS: Role[] = ['ADMIN', 'SUPER_ADMIN'];

const NAV: NavItem[] = [
  { label: 'Dashboard', icon: 'dashboard', link: '/dashboard', roles: ['USER', 'EMPLOYEE', ...ADMINS] },
  { label: 'Bookings', icon: 'inventory_2', link: '/bookings', roles: ['USER', ...ADMINS] },
  { label: 'Catalog', icon: 'category', link: '/catalog', roles: ['USER', ...ADMINS] },
  { label: 'Live Tracking', icon: 'location_on', link: '/tracking', roles: ['USER', 'EMPLOYEE', ...ADMINS] },
  { label: 'Payments', icon: 'payments', link: '/payments', roles: ['USER', ...ADMINS] },
  { label: 'Fleet', icon: 'directions_boat', link: '/fleet', roles: ADMINS },
  { label: 'Containers', icon: 'inventory', link: '/containers', roles: ADMINS },
  { label: 'Staff', icon: 'group_add', link: '/admin/staff', roles: ADMINS },
  { label: 'KYC review', icon: 'verified_user', link: '/admin/kyc', roles: ADMINS },
  { label: 'Movements', icon: 'sync_alt', link: '/staff/movements', roles: ['EMPLOYEE', ...ADMINS] },
  { label: 'Profile', icon: 'person', link: '/profile', roles: ['USER', 'EMPLOYEE', ...ADMINS] },
  { label: 'Careers', icon: 'work', link: '/careers', roles: ['USER'] },
  { label: 'Resignation', icon: 'logout', link: '/resignation', roles: ['EMPLOYEE'] },
  { label: 'Port desk', icon: 'move_to_inbox', link: '/staff/port-desk', roles: ['EMPLOYEE', ...ADMINS] },
];

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatSidenavModule,
    MatListModule, MatIconModule, MatButtonModule,
  ],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {
  private readonly watcher = inject(StatusWatcher);
  protected readonly auth = inject(Auth);
constructor() {
  effect(() => { if (this.auth.user()) this.watcher.start(); else this.watcher.stop(); });
  inject(DestroyRef).onDestroy(() => this.watcher.stop());
}
  protected readonly isMobile = toSignal(
    inject(BreakpointObserver).observe('(max-width: 800px)').pipe(map((r) => r.matches)),
    { initialValue: false },
  );

  protected readonly nav = computed(() => {
    
    const role = this.auth.role();
    return role ? NAV.filter((i) => i.roles.includes(role)) : [];
    
  });
}