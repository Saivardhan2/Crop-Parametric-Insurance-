import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';

import { routes } from './app.routes';
import { authInterceptor } from './interceptors/authInterceptor';

import { loginReducer } from './features/auth/state/login.reducer';
import { userReducer } from './features/user/state/user.reducer';
import { farmsReducer } from './features/farms/state/farms.reducer';
import { policiesReducer } from './features/policies/state/policies.reducer';
import { claimsReducer } from './features/claims/state/claims.reducer';

import { LoginEffects } from './features/auth/state/login.effects';
import { UserEffects } from './features/user/state/user.effect';
import { FarmsEffects } from './features/farms/state/farms.effects';
import { PoliciesEffects } from './features/policies/state/policies.effects';
import { ClaimsEffects } from './features/claims/state/claims.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),

    provideStore({
      login: loginReducer,
      user: userReducer,
      farms: farmsReducer,
      policies: policiesReducer,
      claims: claimsReducer
    }),

    provideEffects(
      LoginEffects,
      UserEffects,
      FarmsEffects,
      PoliciesEffects,
      ClaimsEffects
    )
  ]
};
