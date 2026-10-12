import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginReqDTO } from '../models/LoginReqDTO';
import { LoginResDTO } from '../models/LoginResDTO';
import { RegisterReqDTO } from '../models/RegisterReqDTO';
import { UserResDTO } from '../../user/models/UserResDTO';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/auth';

  login(req: LoginReqDTO): Observable<LoginResDTO> {
    return this.http.post<LoginResDTO>(`${this.apiUrl}/login`, req);
  }

  register(req: RegisterReqDTO): Observable<UserResDTO> {
    return this.http.post<UserResDTO>(`${this.apiUrl}/register`, req);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {});
  }
}
