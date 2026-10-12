import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResDTO } from '../models/UserResDTO';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/users';

  getCurrentUser(): Observable<UserResDTO> {
    return this.http.get<UserResDTO>(`${this.apiUrl}/me`);
  }
}
