import { Status } from '../../auth/state/login.state';
import { FarmerProfileDTO } from '../models/FarmerProfileDTO';
import { FarmDTO } from '../models/FarmDTO';
import { CropDTO } from '../models/CropDTO';
import { IrrigationDTO } from '../models/IrrigationDTO';

export interface FarmsState {
  profile: FarmerProfileDTO | null;
  profileStatus: Status;
  farms: FarmDTO[];
  farmsStatus: Status;
  selectedFarmCrops: CropDTO[];
  cropsStatus: Status;
  selectedFarmIrrigation: IrrigationDTO | null;
  irrigationStatus: Status;
}

export const InitialFarmsState: FarmsState = {
  profile: null,
  profileStatus: { loading: false, success: false, error: '' },
  farms: [],
  farmsStatus: { loading: false, success: false, error: '' },
  selectedFarmCrops: [],
  cropsStatus: { loading: false, success: false, error: '' },
  selectedFarmIrrigation: null,
  irrigationStatus: { loading: false, success: false, error: '' }
};
