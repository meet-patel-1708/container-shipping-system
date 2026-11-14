import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal
} from '@angular/core';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import { MatButtonModule }
  from '@angular/material/button';

import { MatCardModule }
  from '@angular/material/card';

import { MatProgressSpinnerModule }
  from '@angular/material/progress-spinner';

import { HttpErrorResponse }
  from '@angular/common/http';

import { Auth }
  from '../../../core/auth/auth';


@Component({
  selector: 'app-verify-email',

  imports: [
    MatButtonModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],

  templateUrl: './verify-email.html',

  styleUrl: './verify-email.scss',

  changeDetection:
    ChangeDetectionStrategy.OnPush
})
export class VerifyEmail {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly auth =
    inject(Auth);


  protected readonly loading =
    signal(true);

  protected readonly success =
    signal(false);

  protected readonly error =
    signal(false);

  protected readonly message =
    signal('');


  constructor() {

    const token =
      this.route.snapshot
        .queryParamMap
        .get('token');


    if (!token) {

      this.loading.set(false);

      this.error.set(true);

      this.message.set(
        'Invalid verification link.'
      );

      return;
    }


    this.verify(token);
  }


  private verify(
    token: string
  ): void {

    this.auth
      .verifyEmail(token)
      .subscribe({

        next: (response) => {

          this.loading.set(false);

          this.success.set(true);

          this.message.set(
            response
          );
        },


        error: (
          err: HttpErrorResponse
        ) => {

          this.loading.set(false);

          this.error.set(true);


          this.message.set(

            typeof err.error === 'string'

              ? err.error

              : err.error?.message
                ?? 'Email verification failed.'
          );
        }

      });
  }


  protected goToLogin(): void {

    this.router.navigateByUrl(
      '/login'
    );
  }
}