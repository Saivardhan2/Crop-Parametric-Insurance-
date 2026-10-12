import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { PoliciesService } from '../services/policies-service';
import * as PoliciesActions from './policies.actions';
import { catchError, map, of, switchMap } from 'rxjs';

@Injectable()
export class PoliciesEffects {
  private actions$ = inject(Actions);
  private policiesService = inject(PoliciesService);

  createApplication$ = createEffect(() =>
    this.actions$.pipe(
      ofType(PoliciesActions.createApplication),
      switchMap(({ application }) => this.policiesService.createApplication(application).pipe(
        map((created) => PoliciesActions.createApplicationSuccess({ application: created })),
        catchError((error) => of(PoliciesActions.createApplicationFailure({ error: error.message || 'Failed' })))
      ))
    )
  );
}
