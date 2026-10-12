import { inject, Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import * as FarmsActions from './farms.actions';
import { tap } from 'rxjs';

@Injectable()
export class FarmsEffects {
  private actions$ = inject(Actions);

  logFarmActions$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(
          FarmsActions.loadProfile,
          FarmsActions.saveProfile,
          FarmsActions.loadFarms,
          FarmsActions.createFarm,
          FarmsActions.loadCrops,
          FarmsActions.addCrop,
          FarmsActions.loadIrrigation,
          FarmsActions.saveIrrigation
        ),
        tap(() => {})
      ),
    { dispatch: false }
  );
}
