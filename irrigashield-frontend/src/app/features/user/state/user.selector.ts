import { createFeatureSelector, createSelector } from '@ngrx/store';
import { UserState } from './user.state';

export const userState = createFeatureSelector<UserState>('user');
export const currentUserSelector = createSelector(userState, (state) => state.currentUser);
export const userLoadingStatusSelector = createSelector(userState, (state) => state.userLoadingStatus);
