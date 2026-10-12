import { createReducer, on } from '@ngrx/store';
import { InitialFarmsState } from './farms.state';
import * as FarmsActions from './farms.actions';

export const farmsReducer = createReducer(
  InitialFarmsState,
  on(FarmsActions.loadProfile, (state) => ({ ...state, profileStatus: { loading: true, success: false, error: '' } })),
  on(FarmsActions.loadProfileSuccess, (state, { profile }) => ({ ...state, profile, profileStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.loadProfileFailure, (state, { error }) => ({ ...state, profileStatus: { loading: false, success: false, error } })),
  on(FarmsActions.saveProfile, (state) => ({ ...state, profileStatus: { loading: true, success: false, error: '' } })),
  on(FarmsActions.saveProfileSuccess, (state, { profile }) => ({ ...state, profile, profileStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.saveProfileFailure, (state, { error }) => ({ ...state, profileStatus: { loading: false, success: false, error } })),
  on(FarmsActions.loadFarms, (state) => ({ ...state, farmsStatus: { loading: true, success: false, error: '' } })),
  on(FarmsActions.loadFarmsSuccess, (state, { farms }) => ({ ...state, farms, farmsStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.loadFarmsFailure, (state, { error }) => ({ ...state, farmsStatus: { loading: false, success: false, error } })),
  on(FarmsActions.createFarmSuccess, (state, { farm }) => ({ ...state, farms: [...state.farms, farm], farmsStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.loadCropsSuccess, (state, { crops }) => ({ ...state, selectedFarmCrops: crops, cropsStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.addCropSuccess, (state, { crop }) => ({ ...state, selectedFarmCrops: [...state.selectedFarmCrops, crop], cropsStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.loadIrrigationSuccess, (state, { irrigation }) => ({ ...state, selectedFarmIrrigation: irrigation, irrigationStatus: { loading: false, success: true, error: '' } })),
  on(FarmsActions.saveIrrigationSuccess, (state, { irrigation }) => ({ ...state, selectedFarmIrrigation: irrigation, irrigationStatus: { loading: false, success: true, error: '' } }))
);
