# Verbatim Python Script
import sys
sys.stdout.reconfigure(encoding='utf-8')

policies_content = "import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
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
    return this.http.get<any[]>(${this.baseUrl}/plans/active).pipe(
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
    return this.http.get<any>(${this.baseUrl}/plans/).pipe(
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
    const today = new Date();
    const futureDate = new Date();
    futureDate.setDate(today.getDate() + 90);
    const startDateStr = today.toISOString().split('T')[0];
    const endDateStr = futureDate.toISOString().split('T')[0];

    const payload = {
      farmId: app.farmId,
      cropId: app.cropId,
      planId: app.planId,
      requestedCoverage: app.requestedCoverageAmount || (app as any).requestedCoverage || 50000,
      proposedStartDate: (app as any).proposedStartDate || startDateStr,
      proposedEndDate: (app as any).proposedEndDate || endDateStr
    };

    return this.http.post<any>(${this.baseUrl}/applications, payload).pipe(
      map(res => ({
        ...res,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }

  getMyApplications(): Observable<PolicyApplicationDTO[]> {
    return this.http.get<any[]>(${this.baseUrl}/applications/my-applications).pipe(
      map(items => (items || []).map(item => ({
        ...item,
        requestedCoverageAmount: item.requestedCoverage ?? item.requestedCoverageAmount
      })))
    );
  }

  submitApplication(appId: number): Observable<PolicyApplicationDTO> {
    return this.http.post<any>(${this.baseUrl}/applications//submit, {}).pipe(
      map(res => ({
        ...res,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }

  getApprovedQuote(appId: number): Observable<any> {
    return this.http.get<any>(${this.baseUrl}/policies/approved-quote/);
  }

  acceptApprovedQuote(appId: number): Observable<PolicyDTO> {
    return this.http.post<any>(${this.baseUrl}/policies/approved-quote//accept, {}).pipe(
      map(p => ({
        ...p,
        policyStatus: p.status ?? p.policyStatus,
        premiumPaid: p.premiumAmount ?? p.premiumPaid
      }))
    );
  }

  getMyPolicies(): Observable<PolicyDTO[]> {
    return this.http.get<any[]>(${this.baseUrl}/policies/my-policies).pipe(
      map(items => (items || []).map(p => ({
        ...p,
        policyStatus: p.status ?? p.policyStatus,
        premiumPaid: p.premiumAmount ?? p.premiumPaid
      })))
    );
  }

  getPendingApplications(): Observable<PolicyApplicationDTO[]> {
    return this.http.get<any[]>(${this.baseUrl}/underwriting/applications/pending).pipe(
      map(items => (items || []).map(item => ({
        ...item,
        status: item.applicationStatus ?? item.status,
        requestedCoverageAmount: item.requestedCoverage ?? item.requestedCoverageAmount
      })))
    );
  }

  startReview(appId: number): Observable<PolicyApplicationDTO> {
    return this.http.post<any>(${this.baseUrl}/underwriting/applications//start-review, {}).pipe(
      map(res => ({
        ...res,
        status: res.applicationStatus ?? res.status,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }

  assessRisk(appId: number): Observable<any> {
    return this.http.post<any>(${this.baseUrl}/underwriting/applications//assess, {});
  }

  approveApplication(appId: number, payload: { approvedCoverageAmount: number; quotedPremiumAmount: number; remarks: string }): Observable<PolicyApplicationDTO> {
    return this.http.put<any>(${this.baseUrl}/underwriting/applications//approve, {
      remarks: payload.remarks || 'Application approved by underwriter'
    }).pipe(
      map(res => ({
        ...res,
        status: res.applicationStatus ?? res.status,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount,
        approvedCoverageAmount: res.recommendedCoverage ?? res.approvedCoverageAmount,
        quotedPremiumAmount: res.recommendedPremium ?? res.quotedPremiumAmount
      }))
    );
  }

  rejectApplication(appId: number, payload: { remarks: string }): Observable<PolicyApplicationDTO> {
    return this.http.put<any>(${this.baseUrl}/underwriting/applications//reject, {
      remarks: payload.remarks || 'Application rejected by underwriter'
    }).pipe(
      map(res => ({
        ...res,
        status: res.applicationStatus ?? res.status,
        requestedCoverageAmount: res.requestedCoverage ?? res.requestedCoverageAmount
      }))
    );
  }
}
"

admin_content = "import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { UserResDTO } from '../../user/models/UserResDTO';
import { InsurancePlanDTO } from '../../policies/models/InsurancePlanDTO';
import { TriggerRuleDTO } from '../models/TriggerRuleDTO';
import { PayoutSlabDTO } from '../models/PayoutSlabDTO';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api/admin';

  getUsers(): Observable<UserResDTO[]> {
    return this.http.get<UserResDTO[]>(${this.baseUrl}/users);
  }

  createUser(user: { name: string; email: string; password: string; role: string; phone?: string }): Observable<UserResDTO> {
    const payload = {
      name: user.name,
      email: user.email,
      password: user.password,
      role: user.role,
      phone: user.phone || '9876543210'
    };
    return this.http.post<UserResDTO>(${this.baseUrl}/users, payload);
  }

  getPlans(): Observable<InsurancePlanDTO[]> {
    return this.http.get<any[]>(${this.baseUrl}/plans).pipe(
      map(items => (items || []).map(p => ({
        ...p,
        maxCoverageAmount: p.maximumCoverage ?? p.maxCoverageAmount ?? 100000,
        basePremiumRate: p.basePremium ? Math.round((p.basePremium / (p.maximumCoverage || 100000)) * 100 * 10) / 10 : 5.0,
        season: p.season || 'RABI',
        cropType: p.cropType || 'WHEAT'
      })))
    );
  }

  createPlan(plan: InsurancePlanDTO): Observable<InsurancePlanDTO> {
    const maxCov = plan.maxCoverageAmount || (plan as any).maximumCoverage || 100000;
    const baseRate = plan.basePremiumRate || 5.0;
    const calculatedPremium = Math.round(maxCov * (baseRate / 100));

    const payload = {
      planName: plan.planName,
      description: plan.description || 'Parametric crop irrigation shortage coverage',
      minimumCoverage: (plan as any).minimumCoverage || 5000,
      maximumCoverage: maxCov,
      basePremium: (plan as any).basePremium || calculatedPremium,
      maximumPolicyDays: (plan as any).maximumPolicyDays || 180
    };

    return this.http.post<any>(${this.baseUrl}/plans, payload).pipe(
      map(res => ({
        ...res,
        maxCoverageAmount: res.maximumCoverage ?? res.maxCoverageAmount ?? 100000,
        basePremiumRate: res.basePremium ? Math.round((res.basePremium / (res.maximumCoverage || 100000)) * 100 * 10) / 10 : 5.0,
        season: res.season || 'RABI',
        cropType: res.cropType || 'WHEAT'
      }))
    );
  }

  getTriggerRules(planId: number): Observable<TriggerRuleDTO[]> {
    return this.http.get<any[]>(${this.baseUrl}/plans//trigger-rules).pipe(
      map(items => (items || []).map(r => ({
        ...r,
        triggerRuleId: r.ruleId ?? r.triggerRuleId,
        thresholdValue: r.availabilityThresholdPercentage ?? r.thresholdValue ?? 20.0,
        consecutiveDaysRequired: r.minimumContinuousHours ? Math.round(r.minimumContinuousHours / 24) : (r.consecutiveDaysRequired || 3),
        description: r.description || Shortage below %
      })))
    );
  }

  createTriggerRule(planId: number, rule: TriggerRuleDTO): Observable<TriggerRuleDTO> {
    const payload = {
      cropType: (rule as any).cropType || 'WHEAT',
      growthStage: (rule as any).growthStage || 'VEGETATIVE',
      availabilityThresholdPercentage: (rule as any).thresholdValue ?? (rule as any).availabilityThresholdPercentage ?? 20.0,
      minimumContinuousHours: ((rule as any).consecutiveDaysRequired ? (rule as any).consecutiveDaysRequired * 24 : 72),
      description: rule.description || 'Soil moisture below configured threshold'
    };
    return this.http.post<any>(${this.baseUrl}/plans//trigger-rules, payload).pipe(
      map(r => ({
        ...r,
        triggerRuleId: r.ruleId ?? r.triggerRuleId,
        thresholdValue: r.availabilityThresholdPercentage ?? r.thresholdValue,
        consecutiveDaysRequired: r.minimumContinuousHours ? Math.round(r.minimumContinuousHours / 24) : 3,
        description: r.description || rule.description
      }))
    );
  }

  getPayoutSlabs(triggerRuleId: number): Observable<PayoutSlabDTO[]> {
    return this.http.get<any[]>(${this.baseUrl}/trigger-rules//payout-slabs).pipe(
      map(items => (items || []).map(s => ({
        ...s,
        payoutSlabId: s.slabId ?? s.payoutSlabId,
        lowerDeficitThreshold: s.minAvailabilityPct ?? s.lowerDeficitThreshold ?? 0.0,
        upperDeficitThreshold: s.maxAvailabilityPct ?? s.upperDeficitThreshold ?? 20.0,
        payoutPercentage: s.payoutPercentage
      })))
    );
  }

  createPayoutSlab(triggerRuleId: number, slab: PayoutSlabDTO): Observable<PayoutSlabDTO> {
    const payload = {
      minAvailabilityPct: (slab as any).lowerDeficitThreshold ?? (slab as any).minAvailabilityPct ?? 0.0,
      maxAvailabilityPct: (slab as any).upperDeficitThreshold ?? (slab as any).maxAvailabilityPct ?? 20.0,
      payoutPercentage: slab.payoutPercentage || 25.0,
      minimumDurationHours: 24,
      maximumDurationHours: 72
    };
    return this.http.post<any>(${this.baseUrl}/trigger-rules//payout-slabs, payload).pipe(
      map(s => ({
        ...s,
        payoutSlabId: s.slabId ?? s.payoutSlabId,
        lowerDeficitThreshold: s.minAvailabilityPct ?? s.lowerDeficitThreshold ?? 0.0,
        upperDeficitThreshold: s.maxAvailabilityPct ?? s.upperDeficitThreshold ?? 20.0,
        payoutPercentage: s.payoutPercentage
      }))
    );
  }
}
"

with open(r'd:\Capstone\irrigashield-frontend\src\app\features\policies\services\policies-service.ts', 'w', encoding='utf-8') as f:
    f.write(policies_content)

with open(r'd:\Capstone\irrigashield-frontend\src\app\features\admin\services\admin-service.ts', 'w', encoding='utf-8') as f:
    f.write(admin_content)

print('Both services fixed completely!')
