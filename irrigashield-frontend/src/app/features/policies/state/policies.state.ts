import { Status } from '../../auth/state/login.state';
import { InsurancePlanDTO } from '../models/InsurancePlanDTO';
import { PolicyApplicationDTO } from '../models/PolicyApplicationDTO';
import { PolicyDTO } from '../models/PolicyDTO';

export interface PoliciesState {
  activePlans: InsurancePlanDTO[];
  plansStatus: Status;
  myApplications: PolicyApplicationDTO[];
  applicationsStatus: Status;
  pendingReviewApplications: PolicyApplicationDTO[];
  reviewStatus: Status;
  myPolicies: PolicyDTO[];
  policiesStatus: Status;
}

export const InitialPoliciesState: PoliciesState = {
  activePlans: [],
  plansStatus: { loading: false, success: false, error: '' },
  myApplications: [],
  applicationsStatus: { loading: false, success: false, error: '' },
  pendingReviewApplications: [],
  reviewStatus: { loading: false, success: false, error: '' },
  myPolicies: [],
  policiesStatus: { loading: false, success: false, error: '' }
};
