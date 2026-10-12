import { createReducer, on } from '@ngrx/store';
import { InitialPoliciesState } from './policies.state';
import * as PoliciesActions from './policies.actions';

export const policiesReducer = createReducer(
  InitialPoliciesState,
  on(PoliciesActions.loadActivePlansSuccess, (state, { plans }) => ({ ...state, activePlans: plans })),
  on(PoliciesActions.loadMyApplicationsSuccess, (state, { applications }) => ({ ...state, myApplications: applications })),
  on(PoliciesActions.createApplicationSuccess, (state, { application }) => ({ ...state, myApplications: [...state.myApplications, application] })),
  on(PoliciesActions.loadMyPoliciesSuccess, (state, { policies }) => ({ ...state, myPolicies: policies })),
  on(PoliciesActions.acceptQuoteSuccess, (state, { policy }) => ({ ...state, myPolicies: [...state.myPolicies, policy] })),
  on(PoliciesActions.loadPendingReviewsSuccess, (state, { applications }) => ({ ...state, pendingReviewApplications: applications }))
);
