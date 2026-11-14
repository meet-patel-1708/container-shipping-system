import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';

import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { Auth } from '../../../core/auth/auth';

import {
  indianPhone,
  matchFields,
  PASSWORD_RULES,
  passwordStrength,
} from '../../../shared/validators/form-validators';

import { HttpErrorResponse } from '@angular/common/http';

import { toSignal } from '@angular/core/rxjs-interop';


@Component({
  selector: 'app-register',

  imports: [
    ReactiveFormsModule,
    RouterLink,

    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,

    MatProgressBarModule,
    MatProgressSpinnerModule
  ],

  templateUrl: './register.html',

  styleUrl: './register.scss',

  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Register {

  private readonly fb =
    inject(NonNullableFormBuilder);

  private readonly auth =
    inject(Auth);

  private readonly router =
    inject(Router);


  // =========================================================
  // STATE
  // =========================================================

  protected readonly loading =
    signal(false);

  protected readonly error =
    signal<string | null>(null);

  protected readonly success =
    signal<string | null>(null);

  protected readonly hide =
    signal(true);


  // =========================================================
  // FORM
  // =========================================================

  protected readonly form = this.fb.group(
    {

      name: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50)
        ]
      ],

      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(100)
        ]
      ],

      phone: [
        '',
        [
          Validators.required,
          indianPhone
        ]
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.maxLength(72),
          passwordStrength
        ]
      ],

      confirmPassword: [
        '',
        Validators.required
      ]

    },

    {
      validators:
        matchFields(
          'password',
          'confirmPassword'
        )
    }
  );


  // =========================================================
  // PASSWORD STRENGTH
  // =========================================================

  private readonly password =
    toSignal(
      this.form.controls.password.valueChanges,
      {
        initialValue: ''
      }
    );


  protected readonly rules =
    computed(() =>
      PASSWORD_RULES.map(
        (r) => ({
          label: r.label,
          ok: r.test(
            this.password()
          )
        })
      )
    );


  protected readonly score =
    computed(() =>
      this.rules()
        .filter((r) => r.ok)
        .length
    );


  protected readonly strengthPercent =
    computed(() =>
      (
        this.score() /
        PASSWORD_RULES.length
      ) * 100
    );


  protected readonly strengthLabel =
    computed(() => {

      const s = this.score();

      return s <= 2
        ? 'Weak'
        : s <= 4
          ? 'Medium'
          : 'Strong';
    });


  // =========================================================
  // REGISTER
  // =========================================================

  protected submit(): void {

    if (
      this.form.invalid ||
      this.loading()
    ) {

      this.form.markAllAsTouched();

      return;
    }


    this.loading.set(true);

    this.error.set(null);

    this.success.set(null);


    /*
     * IMPORTANT:
     *
     * We only send:
     *
     * name
     * email
     * phone
     * password
     *
     * NO ROLE.
     */


    const {
      confirmPassword,
      ...body
    } = this.form.getRawValue();


    this.auth
      .register(body)
      .subscribe({

        // ===================================================
        // SUCCESS
        // ===================================================

        next: () => {
  this.loading.set(false);
  this.router.navigate(['/check-email'], {
    queryParams: { email: body.email }
  });
},


        // ===================================================
        // ERROR
        // ===================================================

        error: (
          err: HttpErrorResponse
        ) => {

          this.loading.set(false);

          this.error.set(

            err.status === 0

              ? 'Cannot reach the server.'

              : err.error?.message
                ?? 'Registration failed. Email or phone may already be in use.'
          );
        }

      });
  }
}