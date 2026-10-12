import { createReducer, on } from '@ngrx/store';
import { InitialUserState } from './user.state';
import * as UserActions from './user.actions';

export const userReducer = createReducer(
  InitialUserState,
  on(UserActions.loadUser, (state) => ({
    ...state,
    userLoadingStatus: { loading: true, success: false, error: '' }
  })),
  on(UserActions.loadUserSuccess, (state, { user }) => {
    localStorage.setItem('currentUser', JSON.stringify(user));
    return {
      ...state,
      currentUser: user,
      userLoadingStatus: { loading: false, success: true, error: '' }
    };
  }),
  on(UserActions.loadUserFailure, (state, { error }) => ({
    ...state,
    userLoadingStatus: { loading: false, success: false, error }
  })),
  on(UserActions.eraseUser, (state) => ({
    ...state,
    currentUser: null,
    userLoadingStatus: { loading: false, success: false, error: '' }
  }))
);
