import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Auth } from '../../../core/auth/auth';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,RouterLink,MatCardModule,MatFormFieldModule,
    MatInputModule,MatButtonModule,MatIconModule,MatProgressSpinnerModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection:ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly loading = signal(false);
  protected readonly error = signal<string|null>(null);
  protected readonly hidePassword = signal(true);

  protected readonly form = this.fb.group({
    email:['',[Validators.required,Validators.email]],
    password:['',Validators.required],
  });
  protected readonly unverified = signal(false);
protected readonly resendMsg = signal<string | null>(null);

protected submit(): void {
  if (this.form.invalid || this.loading()) { this.form.markAllAsTouched(); return; }
  this.loading.set(true);
  this.error.set(null);
  this.unverified.set(false);
  this.resendMsg.set(null);

  this.auth.login(this.form.getRawValue()).subscribe({
    next: () => {
      const def = this.auth.user()?.role === 'EMPLOYEE' ? '/staff/movements' : '/dashboard';
const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? def;
      this.router.navigateByUrl(returnUrl);
    },
    error: (err: HttpErrorResponse) => {
      this.loading.set(false);
      if (err.status === 403 && err.error?.code === 'EMAIL_NOT_VERIFIED') {
        this.unverified.set(true);
        return;
      }
      this.error.set(
        err.status === 401 ? 'Invalid email or password.'
        : err.status === 429 ? 'Too many attempts. Wait a minute and try again.'
        : err.status === 0 ? 'Cannot reach the server.'
        : 'Login failed. Please try again.'
      );
    },
  });
}

protected resend(): void {
  this.auth.resendVerification(this.form.controls.email.value).subscribe({
    next: () => this.resendMsg.set('Verification email sent. Check your inbox.'),
    error: () => this.resendMsg.set('Could not resend. Try again shortly.'),
  });
}
}
