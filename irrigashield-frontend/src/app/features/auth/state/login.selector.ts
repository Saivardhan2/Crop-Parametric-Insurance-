import { createFeatureSelector, createSelector } from '@ngrx/store';
import { LoginState } from './login.state';

export const loginState = createFeatureSelector<LoginState>('login');
export const isLoggedInSelector = createSelector(loginState, (state) => state.isLoggedIn);
export const userRoleSelector = createSelector(loginState, (state) => state.role);
export const loginStatusSelector = createSelector(loginState, (state) => state.loginStatus);
export const signupStatusSelector = createSelector(loginState, (state) => state.signupStatus);
