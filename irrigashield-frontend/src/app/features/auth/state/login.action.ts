import { createAction, props } from '@ngrx/store';
import { LoginReqDTO } from '../models/LoginReqDTO';
import { LoginResDTO } from '../models/LoginResDTO';
import { RegisterReqDTO } from '../models/RegisterReqDTO';

export const login = createAction('[LOGIN] LOGIN', props<{ jwtReq: LoginReqDTO }>());
export const loginSuccess = createAction('[LOGIN] LOGIN SUCCESS', props<{ jwtRes: LoginResDTO }>());
export const loginFailure = createAction('[LOGIN] LOGIN FAILURE', props<{ error: string }>());
export const logout = createAction('[LOGIN] LOGOUT');

export const signup = createAction('[SIGN UP]', props<{ userReq: RegisterReqDTO }>());
export const signupSuccess = createAction('[SIGN UP] SUCCESS');
export const signupFailure = createAction('[SIGN UP] FAILURE', props<{ error: string }>());
