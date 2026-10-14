import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { AuthService } from '../service/auth-service';
import * as LoginActions from './login.action';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { eraseUser } from '../../user/state/user.actions';

@Injectable()
export class LoginEffects {
  private actions$ = inject(Actions);
  private authService = inject(AuthService);
  private router = inject(Router);

  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(LoginActions.login),
      switchMap(({ jwtReq }) =>
        this.authService.login(jwtReq).pipe(
          tap((jwtRes) => {
            if (jwtRes?.user) {
              localStorage.setItem('role', jwtRes.user.role);
              localStorage.setItem('currentUser', JSON.stringify(jwtRes.user));
            }
            const role = jwtRes?.user?.role;
            if (role === 'ADMIN') {
              this.router.navigate(['/admin/users']);
            } else if (role === 'UNDERWRITER') {
              this.router.navigate(['/underwriting']);
            } else if (role === 'DATA_PROVIDER') {
              this.router.navigate(['/data-provider']);
            } else if (role === 'CLAIMS_OFFICER') {
              this.router.navigate(['/claims-officer/claims']);
            } else {
              this.router.navigate(['/farmer/farms']);
            }
          }),
          map((jwtRes) => LoginActions.loginSuccess({ jwtRes })),
          catchError((error: HttpErrorResponse) => {
            const valErrs = error.error?.validationErrors ? Object.values(error.error.validationErrors).join(', ') : '';
            const msg = valErrs || error.error?.message || 'Login failed. Please check credentials.';
            return of(LoginActions.loginFailure({ error: msg }));
          })
        )
      )
    )
  );

  signup$ = createEffect(() =>
    this.actions$.pipe(
      ofType(LoginActions.signup),
      switchMap(({ userReq }) =>
        this.authService.register(userReq).pipe(
          tap(() => this.router.navigate(['/login'])),
          map(() => LoginActions.signupSuccess()),
          catchError((error: HttpErrorResponse) => {
            const valErrs = error.error?.validationErrors ? Object.values(error.error.validationErrors).join(', ') : '';
            const msg = valErrs || error.error?.message || 'Registration failed.';
            return of(LoginActions.signupFailure({ error: msg }));
          })
        )
      )
    )
  );

  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(LoginActions.logout),
      switchMap(() =>
        this.authService.logout().pipe(
          catchError(() => of(null)),
          tap(() => {
            localStorage.removeItem('role');
            localStorage.removeItem('currentUser');
            this.router.navigate(['/login']);
          }),
          map(() => eraseUser())
        )
      )
    )
  );
}
