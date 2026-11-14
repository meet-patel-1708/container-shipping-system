import { Routes } from '@angular/router';

   import { roleGuard } from './core/guards/role-guard';
import { authGuard } from './core/guards/auth-guard';
import { guestGuard } from './core/guards/guest-guard';


export const routes: Routes = [

  // =========================================================
  // LOGIN
  // =========================================================

  {
    path: 'login',

    canActivate: [guestGuard],

    loadComponent: () =>
      import(
        './features/auth/login/login'
      ).then(
        (m) => m.Login
      ),
  },


  // =========================================================
  // REGISTER
  // =========================================================

  {
    path: 'register',

    canActivate: [guestGuard],

    loadComponent: () =>
      import(
        './features/auth/register/register'
      ).then(
        (m) => m.Register
      ),
  },


  // =========================================================
  // VERIFY EMAIL
  // =========================================================
  //
  // IMPORTANT:
  // This route must NOT use authGuard.
  //
  // The user is not authenticated yet.
  //

  {
    path: 'verify-email',

    loadComponent: () =>
      import(
        './features/auth/verify-email/verify-email'
      ).then(
        (m) => m.VerifyEmail
      ),
  },
  {
  path: 'accept-invite',
  loadComponent: () =>
    import('./features/auth/accept-invite/accept-invite').then((m) => m.AcceptInvite),
},
{
  path: 'check-email',
  canActivate: [guestGuard],
  loadComponent: () =>
    import('./features/auth/check-email/check-email').then((m) => m.CheckEmail),
},
  // =========================================================
  // PROTECTED APPLICATION
  // =========================================================

  {
    path: '',

    canActivate: [authGuard],

    loadComponent: () =>
      import(
        './layout/shell/shell'
      ).then(
        (m) => m.Shell
      ),

    children: [

      // Dashboard
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },


      // =====================================================
      // DASHBOARD
      // =====================================================

      {
        path: 'dashboard',

        loadComponent: () =>
          import(
            './features/dashboard/dashboard/dashboard'
          ).then(
            (m) => m.Dashboard
          ),
      },


      // =====================================================
      // TRACKING
      // =====================================================

      {
        path: 'tracking',

        loadComponent: () =>
          import(
            './features/tracking/live-map/live-map'
          ).then(
            (m) => m.LiveMap
          ),
      },


      // =====================================================
      // CATALOG
      // =====================================================

      {
        path: 'catalog',

        loadComponent: () =>
          import(
            './features/catalog/product-list/product-list'
          ).then(
            (m) => m.ProductList
          ),
      },


      // =====================================================
      // BOOKINGS
      // =====================================================

      {
        path: 'bookings',

        loadComponent: () =>
          import(
            './features/booking/booking-list/booking-list'
          ).then(
            (m) => m.BookingList
          ),
      },


      // =====================================================
      // NEW BOOKING
      // =====================================================

      {
        path: 'bookings/new',

        loadComponent: () =>
          import(
            './features/booking/booking-form/booking-form'
          ).then(
            (m) => m.BookingForm
          ),
      },


      // =====================================================
      // BOOKING DETAILS
      // =====================================================

      {
        path: 'bookings/:id',

        loadComponent: () =>
          import(
            './features/booking-details/booking-details'
          ).then(
            (m) => m.BookingDetail
          ),
      },


      // =====================================================
      // PAYMENTS
      // =====================================================

      {
        path: 'payments',

        loadComponent: () =>
          import(
            './features/payments/payment-list/payment-list'
          ).then(
            (m) => m.PaymentList
          ),
      },


      // =====================================================
      // PAYMENT
      // =====================================================

      {
        path: 'payments/:id/pay',

        loadComponent: () =>
          import(
            './features/payments/pay-page/pay-page'
          ).then(
            (m) => m.PayPage
          ),
      },


      // =====================================================
      // FLEET - ADMIN ONLY
      // =====================================================

      {
        path: 'fleet',

        canActivate: [roleGuard('ADMIN','SUPER_ADMIN')],

        loadComponent: () =>
          import(
            './features/fleet/fleet-list/fleet-list'
          ).then(
            (m) => m.FleetList
          ),
      },


      {
        path: 'containers',

        canActivate: [roleGuard('ADMIN','SUPER_ADMIN')],

        loadComponent: () =>
          import(
            './features/containers/container-list/container-list'
          ).then(
            (m) => m.ContainerList
          ),
      },
{
  path: 'admin/staff',
  canActivate: [roleGuard('ADMIN','SUPER_ADMIN')],
  loadComponent: () =>
    import('./features/admin/staff/admin-staff').then((m) => m.AdminStaff),
},
{
  path: 'profile',
  loadComponent: () => import('./features/profile/profile/profile').then((m) => m.Profile),
},
{
  path: 'admin/kyc',
  canActivate: [roleGuard('ADMIN', 'SUPER_ADMIN')],
  loadComponent: () =>
    import('./features/admin/kyc/admin-kyc').then((m) => m.AdminKycPage),
},
{
  path: 'staff/movements',
  canActivate: [roleGuard('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')],
  loadComponent: () =>
    import('./core/staff/movements/staff-movement').then((m) => m.StaffMovements),
},
{ path: 'resignation', canActivate: [roleGuard('EMPLOYEE')],
  loadComponent: () => import('./features/resignation/resignation').then((m) => m.ResignationPage) },
  // app.routes.ts, inside children:
{ path: 'staff/port-desk', canActivate: [roleGuard('EMPLOYEE', 'ADMIN', 'SUPER_ADMIN')],
  loadComponent: () => import('../app/features/admin/staff/port-desk/port-desk').then((m) => m.PortDesk) },
// shell.ts, inside NAV:

{ path: 'careers', canActivate: [roleGuard('USER')],
  loadComponent: () => import('./features/careers/careers').then((m) => m.Careers) },
    ],
  },


  // =========================================================
  // UNKNOWN ROUTES
  // =========================================================

  {
    path: '**',
    redirectTo: '',
  },

];