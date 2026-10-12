export interface PolicyDTO {
  policyId?: number;
  applicationId?: number;
  farmerId?: number;
  farmId?: number;
  planId?: number;
  coverageAmount: number;
  premiumPaid: number;
  startDate: string;
  endDate: string;
  policyStatus: string;
}
