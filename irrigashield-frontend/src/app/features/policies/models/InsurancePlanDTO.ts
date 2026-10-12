export interface InsurancePlanDTO {
  planId?: number;
  planName: string;
  cropType: string;
  season: string;
  basePremiumRate: number;
  maxCoverageAmount: number;
  description: string;
  triggerType: string;
  active?: boolean;
}
