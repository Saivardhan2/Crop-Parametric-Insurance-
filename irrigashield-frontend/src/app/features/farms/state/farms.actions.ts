import { createAction, props } from '@ngrx/store';
import { FarmerProfileDTO } from '../models/FarmerProfileDTO';
import { FarmDTO } from '../models/FarmDTO';
import { CropDTO } from '../models/CropDTO';
import { IrrigationDTO } from '../models/IrrigationDTO';

export const loadProfile = createAction('[FARMS] LOAD PROFILE');
export const loadProfileSuccess = createAction('[FARMS] LOAD PROFILE SUCCESS', props<{ profile: FarmerProfileDTO }>());
export const loadProfileFailure = createAction('[FARMS] LOAD PROFILE FAILURE', props<{ error: string }>());

export const saveProfile = createAction('[FARMS] SAVE PROFILE', props<{ profile: FarmerProfileDTO }>());
export const saveProfileSuccess = createAction('[FARMS] SAVE PROFILE SUCCESS', props<{ profile: FarmerProfileDTO }>());
export const saveProfileFailure = createAction('[FARMS] SAVE PROFILE FAILURE', props<{ error: string }>());

export const loadFarms = createAction('[FARMS] LOAD FARMS');
export const loadFarmsSuccess = createAction('[FARMS] LOAD FARMS SUCCESS', props<{ farms: FarmDTO[] }>());
export const loadFarmsFailure = createAction('[FARMS] LOAD FARMS FAILURE', props<{ error: string }>());

export const createFarm = createAction('[FARMS] CREATE FARM', props<{ farm: FarmDTO }>());
export const createFarmSuccess = createAction('[FARMS] CREATE FARM SUCCESS', props<{ farm: FarmDTO }>());
export const createFarmFailure = createAction('[FARMS] CREATE FARM FAILURE', props<{ error: string }>());

export const loadCrops = createAction('[FARMS] LOAD CROPS', props<{ farmId: number }>());
export const loadCropsSuccess = createAction('[FARMS] LOAD CROPS SUCCESS', props<{ crops: CropDTO[] }>());
export const loadCropsFailure = createAction('[FARMS] LOAD CROPS FAILURE', props<{ error: string }>());

export const addCrop = createAction('[FARMS] ADD CROP', props<{ farmId: number; crop: CropDTO }>());
export const addCropSuccess = createAction('[FARMS] ADD CROP SUCCESS', props<{ crop: CropDTO }>());
export const addCropFailure = createAction('[FARMS] ADD CROP FAILURE', props<{ error: string }>());

export const loadIrrigation = createAction('[FARMS] LOAD IRRIGATION', props<{ farmId: number }>());
export const loadIrrigationSuccess = createAction('[FARMS] LOAD IRRIGATION SUCCESS', props<{ irrigation: IrrigationDTO }>());
export const loadIrrigationFailure = createAction('[FARMS] LOAD IRRIGATION FAILURE', props<{ error: string }>());

export const saveIrrigation = createAction('[FARMS] SAVE IRRIGATION', props<{ farmId: number; irrigation: IrrigationDTO }>());
export const saveIrrigationSuccess = createAction('[FARMS] SAVE IRRIGATION SUCCESS', props<{ irrigation: IrrigationDTO }>());
export const saveIrrigationFailure = createAction('[FARMS] SAVE IRRIGATION FAILURE', props<{ error: string }>());
