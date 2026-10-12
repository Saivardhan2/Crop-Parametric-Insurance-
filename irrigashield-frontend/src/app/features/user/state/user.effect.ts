import { inject, Injectable } from '@angular/core';
import { UserService } from '../services/user-service';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import * as UserActions from './user.actions';
import { catchError, map, of, switchMap } from 'rxjs';
import { logout } from '../../auth/state/login.action';

@Injectable()
export class UserEffects {
  private userService = inject(UserService);
  private actions$ = inject(Actions);

  loadUser$ = createEffect(() =>
    this.actions$.pipe(
      ofType(UserActions.loadUser),
      switchMap(() =>
        this.userService.getCurrentUser().pipe(
          map((user) => UserActions.loadUserSuccess({ user })),
          catchError((error) => {
            if (error.status === 401) {
              return of(logout());
            }
            return of(UserActions.loadUserFailure({ error: error.message || 'Failed to load user profile' }));
          })
        )
      )
    )
  );
}
