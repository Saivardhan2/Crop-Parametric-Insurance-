import { createFeatureSelector, createSelector } from '@ngrx/store';
import { FarmsState } from './farms.state';

export const farmsState = createFeatureSelector<FarmsState>('farms');
export const farmerProfileSelector = createSelector(farmsState, (state) => state.profile);
export const farmsListSelector = createSelector(farmsState, (state) => state.farms);
export const selectedFarmCropsSelector = createSelector(farmsState, (state) => state.selectedFarmCrops);
export const selectedFarmIrrigationSelector = createSelector(farmsState, (state) => state.selectedFarmIrrigation);
export const farmsStatusSelector = createSelector(farmsState, (state) => state.farmsStatus);
