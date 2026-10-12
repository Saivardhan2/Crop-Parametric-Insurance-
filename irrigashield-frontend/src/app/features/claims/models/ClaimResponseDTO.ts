export interface ClaimResponseDTO {
  readingId?: number;
  claimId?: number;
  policyId: number;
  farmId?: number;
  availabilityPercentage: number;
  thresholdBreached: boolean;
  payoutPercentage: number;
  compensationAmount: number;
  payoutStatus: string;
  claimStatus: string;
  recordedAt?: string;
}
