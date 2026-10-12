import { createReducer, on } from '@ngrx/store';
import { InitialClaimsState } from './claims.state';
import * as ClaimsActions from './claims.actions';

export const claimsReducer = createReducer(
  InitialClaimsState,
  on(ClaimsActions.loadMyClaimsSuccess, (state, { claims }) => ({ ...state, myClaims: claims })),
  on(ClaimsActions.submitReading, (state) => ({ ...state, submissionStatus: { loading: true, success: false, error: '' } })),
  on(ClaimsActions.submitReadingSuccess, (state, { claimResponse }) => ({ ...state, latestSubmittedClaim: claimResponse, submissionStatus: { loading: false, success: true, error: '' } })),
  on(ClaimsActions.submitReadingFailure, (state, { error }) => ({ ...state, submissionStatus: { loading: false, success: false, error } }))
);
