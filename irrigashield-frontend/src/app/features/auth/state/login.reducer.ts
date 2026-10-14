import { createReducer, on } from '@ngrx/store';
import { InitialLoginState } from './login.state';
import * as LoginActions from './login.action';

export const loginReducer = createReducer(
  InitialLoginState,
  on(LoginActions.login, (state) => ({
    ...state,
    loginStatus: { loading: true, success: false, error: '' }
  })),
  on(LoginActions.loginSuccess, (state, { jwtRes }) => {
    localStorage.setItem('role', jwtRes.user.role);
    localStorage.setItem('currentUser', JSON.stringify(jwtRes.user));
    return {
      ...state,
      isLoggedIn: true,
      token: null,
      role: jwtRes.user.role,
      loginStatus: { loading: false, success: true, error: '' }
    };
  }),
  on(LoginActions.loginFailure, (state, { error }) => ({
    ...state,
    loginStatus: { loading: false, success: false, error }
  })),
  on(LoginActions.logout, (state) => {
    localStorage.removeItem('role');
    localStorage.removeItem('currentUser');
    return {
      ...state,
      isLoggedIn: false,
      token: null,
      role: null,
      loginStatus: { loading: false, success: false, error: '' }
    };
  }),
  on(LoginActions.signup, (state) => ({
    ...state,
    signupStatus: { loading: true, success: false, error: '' }
  })),
  on(LoginActions.signupSuccess, (state) => ({
    ...state,
    signupStatus: { loading: false, success: true, error: '' }
  })),
  on(LoginActions.signupFailure, (state, { error }) => ({
    ...state,
    signupStatus: { loading: false, success: false, error }
  }))
);
