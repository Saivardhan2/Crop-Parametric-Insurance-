export interface FarmDTO {
  farmId?: number;
  farmerId?: number;
  farmName: string;
  latitude: number;
  longitude: number;
  totalAreaHectares: number;
  soilType: string;
  waterSource: string;
}
