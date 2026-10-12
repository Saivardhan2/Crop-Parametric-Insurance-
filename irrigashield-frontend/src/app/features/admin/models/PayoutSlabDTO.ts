export interface PayoutSlabDTO {
  payoutSlabId?: number;
  triggerRuleId: number;
  minAvailabilityPct?: number;
  maxAvailabilityPct?: number;
  lowerDeficitThreshold: number;
  upperDeficitThreshold: number;
  payoutPercentage: number;
  minimumDurationHours?: number;
  maximumDurationHours?: number;
}
