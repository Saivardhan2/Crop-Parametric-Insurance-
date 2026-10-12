import { createAction, props } from '@ngrx/store';
import { InsurancePlanDTO } from '../models/InsurancePlanDTO';
import { PolicyApplicationDTO } from '../models/PolicyApplicationDTO';
import { PolicyDTO } from '../models/PolicyDTO';

export const loadActivePlans = createAction('[POLICIES] LOAD ACTIVE PLANS');
export const loadActivePlansSuccess = createAction('[POLICIES] LOAD ACTIVE PLANS SUCCESS', props<{ plans: InsurancePlanDTO[] }>());
export const loadActivePlansFailure = createAction('[POLICIES] LOAD ACTIVE PLANS FAILURE', props<{ error: string }>());

export const createApplication = createAction('[POLICIES] CREATE APPLICATION', props<{ application: PolicyApplicationDTO }>());
export const createApplicationSuccess = createAction('[POLICIES] CREATE APPLICATION SUCCESS', props<{ application: PolicyApplicationDTO }>());
export const createApplicationFailure = createAction('[POLICIES] CREATE APPLICATION FAILURE', props<{ error: string }>());

export const loadMyApplications = createAction('[POLICIES] LOAD MY APPLICATIONS');
export const loadMyApplicationsSuccess = createAction('[POLICIES] LOAD MY APPLICATIONS SUCCESS', props<{ applications: PolicyApplicationDTO[] }>());
export const loadMyApplicationsFailure = createAction('[POLICIES] LOAD MY APPLICATIONS FAILURE', props<{ error: string }>());

export const acceptQuote = createAction('[POLICIES] ACCEPT QUOTE', props<{ applicationId: number }>());
export const acceptQuoteSuccess = createAction('[POLICIES] ACCEPT QUOTE SUCCESS', props<{ policy: PolicyDTO }>());
export const acceptQuoteFailure = createAction('[POLICIES] ACCEPT QUOTE FAILURE', props<{ error: string }>());

export const loadMyPolicies = createAction('[POLICIES] LOAD MY POLICIES');
export const loadMyPoliciesSuccess = createAction('[POLICIES] LOAD MY POLICIES SUCCESS', props<{ policies: PolicyDTO[] }>());
export const loadMyPoliciesFailure = createAction('[POLICIES] LOAD MY POLICIES FAILURE', props<{ error: string }>());

export const loadPendingReviews = createAction('[POLICIES] LOAD PENDING REVIEWS');
export const loadPendingReviewsSuccess = createAction('[POLICIES] LOAD PENDING REVIEWS SUCCESS', props<{ applications: PolicyApplicationDTO[] }>());
export const loadPendingReviewsFailure = createAction('[POLICIES] LOAD PENDING REVIEWS FAILURE', props<{ error: string }>());
