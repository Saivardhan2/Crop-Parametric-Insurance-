export interface PolicyApplicationDTO {
  applicationId?: number;
  farmerId?: number;
  farmId: number;
  cropId: number;
  planId: number;
  requestedCoverageAmount: number;
  status?: string;
  submittedAt?: string;
  approvedCoverageAmount?: number;
  quotedPremiumAmount?: number;
  remarks?: string;
}
