import { createAction, props } from '@ngrx/store';
import { UserResDTO } from '../models/UserResDTO';

export const loadUser = createAction('[USER] LOAD USER');
export const loadUserSuccess = createAction('[USER] LOAD USER SUCCESS', props<{ user: UserResDTO }>());
export const loadUserFailure = createAction('[USER] LOAD USER FAILURE', props<{ error: string }>());
export const eraseUser = createAction('[USER] ERASE USER');
