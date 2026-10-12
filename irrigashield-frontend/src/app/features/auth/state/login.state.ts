export interface Status {
  loading: boolean;
  success: boolean;
  error: string;
}
export interface LoginState {
  isLoggedIn: boolean;
  token: string | null;
  role: string | null;
  loginStatus: Status;
  signupStatus: Status;
}
export const InitialLoginState: LoginState = {
  isLoggedIn: !!localStorage.getItem('token'),
  token: localStorage.getItem('token'),
  role: localStorage.getItem('role'),
  loginStatus: { loading: false, success: false, error: '' },
  signupStatus: { loading: false, success: false, error: '' }
};
