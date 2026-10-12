export interface IrrigationDTO {
  profileId?: number;
  farmId: number;
  irrigationType: string;
  capacityLitersPerHour: number;
  efficiencyPercentage: number;
  automationLevel: string;
}
