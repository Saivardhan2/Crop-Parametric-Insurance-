import { Status } from '../../auth/state/login.state';
import { ClaimResponseDTO } from '../models/ClaimResponseDTO';

export interface ClaimsState {
  myClaims: ClaimResponseDTO[];
  claimsStatus: Status;
  latestSubmittedClaim: ClaimResponseDTO | null;
  submissionStatus: Status;
}

export const InitialClaimsState: ClaimsState = {
  myClaims: [],
  claimsStatus: { loading: false, success: false, error: '' },
  latestSubmittedClaim: null,
  submissionStatus: { loading: false, success: false, error: '' }
};
