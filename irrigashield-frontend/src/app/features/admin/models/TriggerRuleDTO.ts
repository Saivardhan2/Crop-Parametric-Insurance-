export interface TriggerRuleDTO {
  triggerRuleId?: number;
  planId: number;
  cropType?: string;
  growthStage?: string;
  availabilityThresholdPercentage?: number;
  requiredShortageDays?: number;
  rollingWindowDays?: number;
  minimumContinuousHours?: number;
  triggerCondition?: string;
  thresholdValue: number;
  consecutiveDaysRequired: number;
  description: string;
}
