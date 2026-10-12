import { createAction, props } from '@ngrx/store';
import { ClaimResponseDTO } from '../models/ClaimResponseDTO';
import { IrrigationReadingReqDTO } from '../models/IrrigationReadingReqDTO';

export const loadMyClaims = createAction('[CLAIMS] LOAD MY CLAIMS');
export const loadMyClaimsSuccess = createAction('[CLAIMS] LOAD MY CLAIMS SUCCESS', props<{ claims: ClaimResponseDTO[] }>());
export const loadMyClaimsFailure = createAction('[CLAIMS] LOAD MY CLAIMS FAILURE', props<{ error: string }>());

export const submitReading = createAction('[CLAIMS] SUBMIT READING', props<{ reading: IrrigationReadingReqDTO }>());
export const submitReadingSuccess = createAction('[CLAIMS] SUBMIT READING SUCCESS', props<{ claimResponse: ClaimResponseDTO }>());
export const submitReadingFailure = createAction('[CLAIMS] SUBMIT READING FAILURE', props<{ error: string }>());
