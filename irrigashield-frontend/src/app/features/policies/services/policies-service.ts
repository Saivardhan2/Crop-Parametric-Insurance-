import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import { InsurancePlanDTO } from '../models/InsurancePlanDTO';
import { PolicyApplicationDTO } from '../models/PolicyApplicationDTO';
import { PolicyDTO } from '../models/PolicyDTO';

@Injectable({
  providedIn: 'root'
})
export class PoliciesService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getActivePlans(): Observable<InsurancePlanDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/plans/active').pipe(
      map(items => (items || []).map(p => ({
        ...p,
        maxCoverageAmount: p.maximumCoverage ?? p.maxCoverageAmount ?? 100000,
        basePremiumRate: p.basePremium ? Math.round((p.basePremium / (p.maximumCoverage || 100000)) * 100 * 10) / 10 : 5.0,
        season: p.season || 'RABI',
        cropType: p.cropType || 'WHEAT'
      })))
    );
  }

  getPlan(planId: number): Observable<InsurancePlanDTO> {
    return this.http.get<any>(this.baseUrl + '/plans/' + planId).pipe(
      map(p => ({
        ...p,
        maxCoverageAmount: p.maximumCoverage ?? p.maxCoverageAmount ?? 100000,
        basePremiumRate: p.basePremium ? Math.round((p.basePremium / (p.maximumCoverage || 100000)) * 100 * 10) / 10 : 5.0,
        season: p.season || 'RABI',
        cropType: p.cropType || 'WHEAT'
      }))
    );
  }

  createApplication(app: PolicyApplicationDTO): Observable<PolicyApplicationDTO> {
    const tomorrow = new Date();
    const futureDate = new Date(Date.now() + 90 * 86400000);
    const fmt = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    const startDateStr = fmt(tomorrow);
    const endDateStr = fmt(futureDate);

    const payload = {
      farmId: app.farmId,
      cropId: app.cropId,
      planId: app.planId,
      requestedCoverage: app.requestedCoverageAmount || (app as any).requestedCoverage || 50000,
      proposedStartDate: (app as any).proposedStartDate || startDateStr,
      proposedEndDate: (app as any).proposedEndDate || endDateStr
    };

    return this.http.post<any>(this.baseUrl + '/applications', payload).pipe(
      switchMap((res: any): Observable<PolicyApplicationDTO> => {
        return this.http.post<any>(this.baseUrl + '/applications/' + res.applicationId + '/submit', {}).pipe(
          map((sub: any): PolicyApplicationDTO => ({
            ...sub,
            requestedCoverageAmount: sub.requestedCoverage ?? sub.requestedCoverageAmount
          })),
          catchError(() => of<PolicyApplicationDTO>({
            ...res,
            requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
          }))
        );
      })
    );
  }

  getMyApplications(): Observable<PolicyApplicationDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/applications/my-applications').pipe(
      map(items => (items || []).map(item => ({
        ...item,
        requestedCoverageAmount: item.requestedCoverage ?? item.requestedCoverageAmount
      })))
    );
  }

  submitApplication(appId: number): Observable<PolicyApplicationDTO> {
    return this.http.post<any>(this.baseUrl + '/applications/' + appId + '/submit', {}).pipe(
      map((res: any): PolicyApplicationDTO => ({
        ...res,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }

  getApprovedQuote(appId: number): Observable<any> {
    return this.http.get<any>(this.baseUrl + '/policies/approved-quote/' + appId);
  }

  acceptApprovedQuote(appId: number): Observable<PolicyDTO> {
    return this.http.post<any>(this.baseUrl + '/policies/approved-quote/' + appId + '/accept', {}).pipe(
      map((p: any): PolicyDTO => ({
        ...p,
        policyStatus: p.status ?? p.policyStatus,
        premiumPaid: p.premiumAmount ?? p.premiumPaid
      }))
    );
  }

  getMyPolicies(): Observable<PolicyDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/policies/my-policies').pipe(
      map(items => (items || []).map(p => ({
        ...p,
        policyStatus: p.status ?? p.policyStatus,
        premiumPaid: p.premiumAmount ?? p.premiumPaid
      })))
    );
  }

  getMyCases(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl + '/underwriting/my-cases');
  }

  getAllCases(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl + '/underwriting/all-cases');
  }

  getPendingApplications(): Observable<PolicyApplicationDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/underwriting/applications/pending').pipe(
      map(items => (items || []).map(item => ({
        ...item,
        status: item.applicationStatus ?? item.status,
        requestedCoverageAmount: item.requestedCoverage ?? item.requestedCoverageAmount
      })))
    );
  }

  startReview(appId: number): Observable<PolicyApplicationDTO> {
    return this.http.post<any>(this.baseUrl + '/underwriting/applications/' + appId + '/start-review', {}).pipe(
      catchError(() => of({ status: 'UNDER_REVIEW', applicationId: appId } as any)),
      map((res: any): PolicyApplicationDTO => ({
        ...res,
        status: res.applicationStatus ?? res.status ?? 'UNDER_REVIEW',
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }

  assessRisk(appId: number): Observable<any> {
    return this.http.post<any>(this.baseUrl + '/underwriting/applications/' + appId + '/assess', {});
  }

  approveApplication(appId: number, payload: { approvedCoverageAmount: number; quotedPremiumAmount: number; remarks: string }): Observable<PolicyApplicationDTO> {
    const safeRemarks = (payload.remarks && payload.remarks.trim().length >= 5)
      ? payload.remarks.trim()
      : 'Application approved by underwriter';
    return this.http.put<any>(this.baseUrl + '/underwriting/applications/' + appId + '/approve', {
      remarks: safeRemarks
    }).pipe(
      map((res: any): PolicyApplicationDTO => ({
        ...res,
        status: res.applicationStatus ?? res.status,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount,
        approvedCoverageAmount: res.recommendedCoverage ?? res.approvedCoverageAmount,
        quotedPremiumAmount: res.recommendedPremium ?? res.quotedPremiumAmount
      }))
    );
  }

  rejectApplication(appId: number, payload: { remarks: string }): Observable<PolicyApplicationDTO> {
    const safeRemarks = (payload.remarks && payload.remarks.trim().length >= 5)
      ? payload.remarks.trim()
      : 'Application rejected by underwriter';
    return this.http.put<any>(this.baseUrl + '/underwriting/applications/' + appId + '/reject', {
      remarks: safeRemarks
    }).pipe(
      map((res: any): PolicyApplicationDTO => ({
        ...res,
        status: res.applicationStatus ?? res.status,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }
}
