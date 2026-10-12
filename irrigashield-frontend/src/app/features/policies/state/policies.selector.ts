import { createFeatureSelector, createSelector } from '@ngrx/store';
import { PoliciesState } from './policies.state';

export const policiesState = createFeatureSelector<PoliciesState>('policies');
export const activePlansSelector = createSelector(policiesState, (state) => state.activePlans);
export const myApplicationsSelector = createSelector(policiesState, (state) => state.myApplications);
export const myPoliciesSelector = createSelector(policiesState, (state) => state.myPolicies);
export const pendingReviewsSelector = createSelector(policiesState, (state) => state.pendingReviewApplications);
