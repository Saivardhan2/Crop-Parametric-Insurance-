import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ClaimsState } from './claims.state';

export const claimsState = createFeatureSelector<ClaimsState>('claims');
export const myClaimsSelector = createSelector(claimsState, (state) => state.myClaims);
export const latestSubmittedClaimSelector = createSelector(claimsState, (state) => state.latestSubmittedClaim);
export const claimsStatusSelector = createSelector(claimsState, (state) => state.claimsStatus);
