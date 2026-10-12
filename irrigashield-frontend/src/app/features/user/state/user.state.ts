import { Status } from '../../auth/state/login.state';
import { UserResDTO } from '../models/UserResDTO';

export interface UserState {
  currentUser: UserResDTO | null;
  userLoadingStatus: Status;
}

export const InitialUserState: UserState = {
  currentUser: localStorage.getItem('currentUser') ? JSON.parse(localStorage.getItem('currentUser')!) : null,
  userLoadingStatus: { loading: false, success: false, error: '' }
};
