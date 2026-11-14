import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Auth } from '../../../core/auth/auth';

@Component({
  selector: 'app-check-email',
  imports: [RouterLink, MatCardModule, MatButtonModule],
  templateUrl: './check-email.html',
  styleUrl:'./check-email.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CheckEmail {
  private readonly auth = inject(Auth);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly email =
    inject(ActivatedRoute).snapshot.queryParamMap.get('email') ?? '';

  protected readonly cooldown = signal(60);
  protected readonly message = signal<string | null>(null);

  private timer?: ReturnType<typeof setInterval>;

  constructor() {
    this.destroyRef.onDestroy(() => clearInterval(this.timer));
    this.startCooldown();
  }

  private startCooldown(): void {
    clearInterval(this.timer);
    this.cooldown.set(60);
    this.timer = setInterval(() => {
      this.cooldown.update((v) => v - 1);
      if (this.cooldown() <= 0) clearInterval(this.timer);
    }, 1000);
  }

  protected resend(): void {
    this.auth.resendVerification(this.email).subscribe({
      next: () => {
        this.message.set('Verification email sent again.');
        this.startCooldown();
      },
      error: () => this.message.set('Could not resend. Try again shortly.'),
    });
  }
}