import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import * as ClaimsActions from './claims.actions';
import { tap } from 'rxjs';

@Injectable()
export class ClaimsEffects {
  private actions$ = inject(Actions);

  logClaimActions$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(
          ClaimsActions.loadMyClaims,
          ClaimsActions.submitReading
        ),
        tap(() => {})
      ),
    { dispatch: false }
  );
}
