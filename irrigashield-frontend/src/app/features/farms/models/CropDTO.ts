export interface CropDTO {
  cropId?: number;
  farmId: number;
  cropName: string;
  cropType: string;
  sowingDate: string;
  expectedHarvestDate: string;
  plantedAreaHectares: number;
}
