import { UserResDTO } from '../../user/models/UserResDTO';

export interface LoginResDTO {
  tokenType: string;
  accessToken: string;
  expiresIn: number;
  user: UserResDTO;
}
