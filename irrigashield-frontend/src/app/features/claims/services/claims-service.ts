import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { ClaimResponseDTO } from '../models/ClaimResponseDTO';
import { IrrigationReadingReqDTO } from '../models/IrrigationReadingReqDTO';

@Injectable({
  providedIn: 'root'
})
export class ClaimsService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  submitReading(reading: IrrigationReadingReqDTO): Observable<ClaimResponseDTO> {
    return this.http.post<ClaimResponseDTO>(this.baseUrl + '/readings', reading);
  }

  getMyClaims(): Observable<ClaimResponseDTO[]> {
    return this.http.get<ClaimResponseDTO[]>(this.baseUrl + '/claims/my-claims');
  }

  getActivePolicyConfigurations(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl + '/internal/policies/active-configurations').pipe(
      catchError(() => of([]))
    );
  }

  getPolicyClaimConfiguration(policyId: number): Observable<any | null> {
    return this.http.get<any>(this.baseUrl + '/internal/policies/' + policyId + '/claim-configuration').pipe(
      catchError(() => of(null))
    );
  }
}
