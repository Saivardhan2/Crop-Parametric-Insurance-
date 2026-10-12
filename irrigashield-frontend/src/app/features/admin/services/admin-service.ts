import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { UserResDTO } from '../../user/models/UserResDTO';
import { InsurancePlanDTO } from '../../policies/models/InsurancePlanDTO';
import { TriggerRuleDTO } from '../models/TriggerRuleDTO';
import { PayoutSlabDTO } from '../models/PayoutSlabDTO';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  getUsers(): Observable<UserResDTO[]> {
    return this.http.get<UserResDTO[]>(this.baseUrl + '/admin/users');
  }

  createUser(user: any): Observable<UserResDTO> {
    return this.http.post<UserResDTO>(this.baseUrl + '/admin/users', user);
  }

  updateUserStatus(userId: number, status: string): Observable<UserResDTO> {
    return this.http.put<UserResDTO>(this.baseUrl + '/admin/users/' + userId + '/status', { status });
  }

  getPlans(): Observable<InsurancePlanDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/admin/plans').pipe(
      map(items => (items || []).map(p => ({
        ...p,
        planId: p.planId ?? p.id,
        planName: p.planName ?? p.name,
        minCoverageAmount: p.minimumCoverage ?? p.minCoverageAmount ?? 1000,
        maxCoverageAmount: p.maximumCoverage ?? p.maxCoverageAmount ?? 100000,
        basePremium: p.basePremium ?? 2500,
        basePremiumRate: p.basePremium
          ? Math.round((p.basePremium / (p.maximumCoverage || 100000)) * 100 * 10) / 10
          : (p.basePremiumRate ?? 5.0),
        maximumPolicyDays: p.maximumPolicyDays ?? 180,
        season: p.season || 'RABI',
        cropType: p.cropType || 'WHEAT',
        triggerType: p.triggerType || 'WATER_AVAILABILITY',
        active: p.active ?? (p.status ? p.status === 'ACTIVE' : true)
      })))
    );
  }

  createPlan(plan: any): Observable<InsurancePlanDTO> {
    const maxCov = Number(plan.maxCoverageAmount ?? plan.maximumCoverage ?? 100000);
    const minCov = Number(plan.minCoverageAmount ?? plan.minimumCoverage ?? 1000);
    const rate = Number(plan.basePremiumRate ?? plan.basePremiumRatePercentage ?? 5);
    const basePrem = Number(plan.basePremium ?? Math.max(100, Math.round(maxCov * (rate / 100))));

    const payload = {
      planName: plan.planName || plan.name,
      description: plan.description,
      minimumCoverage: Math.min(minCov, maxCov),
      maximumCoverage: maxCov,
      basePremium: basePrem,
      maximumPolicyDays: Number(plan.maximumPolicyDays || 180)
    };

    return this.http.post<any>(this.baseUrl + '/admin/plans', payload).pipe(
      map(p => ({
        ...p,
        planId: p.planId ?? p.id,
        planName: p.planName ?? p.name,
        minCoverageAmount: p.minimumCoverage ?? minCov,
        maxCoverageAmount: p.maximumCoverage ?? maxCov,
        basePremium: p.basePremium ?? basePrem,
        basePremiumRate: rate,
        maximumPolicyDays: p.maximumPolicyDays ?? 180,
        season: plan.season || 'RABI',
        cropType: plan.cropType || 'WHEAT',
        triggerType: 'WATER_AVAILABILITY',
        active: p.active ?? true
      }))
    );
  }

  getTriggerRules(planId: number): Observable<TriggerRuleDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/admin/plans/' + planId + '/trigger-rules').pipe(
      map(items => (items || []).map(r => ({
        ...r,
        triggerRuleId: r.triggerRuleId ?? r.id,
        planId: r.planId ?? planId,
        cropType: r.cropType || 'WHEAT',
        growthStage: r.growthStage || 'FLOWERING',
        availabilityThresholdPercentage: r.availabilityThresholdPercentage ?? r.thresholdValue ?? 40,
        thresholdValue: r.availabilityThresholdPercentage ?? r.thresholdValue ?? 40,
        requiredShortageDays: r.requiredShortageDays ?? 1,
        consecutiveDaysRequired: r.requiredShortageDays ?? 1,
        description: `${r.cropType || 'Crop'} (${r.growthStage || 'Stage'}) - Water < ${r.availabilityThresholdPercentage ?? 40}%`
      })))
    );
  }

  createTriggerRule(planId: number, rule: any): Observable<TriggerRuleDTO> {
    const threshold = Number(rule.availabilityThresholdPercentage ?? rule.thresholdValue ?? 40);
    const payload = {
      cropType: rule.cropType || 'WHEAT',
      growthStage: rule.growthStage || 'FLOWERING',
      availabilityThresholdPercentage: threshold,
      minimumContinuousHours: 24,
      rollingWindowDays: 7,
      requiredShortageDays: 1
    };
    return this.http.post<any>(this.baseUrl + '/admin/plans/' + planId + '/trigger-rules', payload).pipe(
      map(r => ({
        ...r,
        triggerRuleId: r.triggerRuleId ?? r.id,
        planId: r.planId ?? planId,
        cropType: r.cropType || payload.cropType,
        growthStage: r.growthStage || payload.growthStage,
        availabilityThresholdPercentage: r.availabilityThresholdPercentage ?? threshold,
        thresholdValue: r.availabilityThresholdPercentage ?? threshold,
        requiredShortageDays: 1,
        consecutiveDaysRequired: 1,
        description: `${r.cropType || payload.cropType} (${r.growthStage || payload.growthStage}) - Water < ${r.availabilityThresholdPercentage ?? threshold}%`
      }))
    );
  }

  getPayoutSlabs(triggerRuleId: number): Observable<PayoutSlabDTO[]> {
    return this.http.get<any[]>(this.baseUrl + '/admin/trigger-rules/' + triggerRuleId + '/payout-slabs').pipe(
      map(items => (items || []).map(s => ({
        ...s,
        payoutSlabId: s.payoutSlabId ?? s.slabId ?? s.id,
        triggerRuleId: s.triggerRuleId ?? triggerRuleId,
        minAvailabilityPct: s.minAvailabilityPct ?? s.lowerDeficitThreshold ?? 0,
        maxAvailabilityPct: s.maxAvailabilityPct ?? s.upperDeficitThreshold ?? 25,
        lowerDeficitThreshold: s.minAvailabilityPct ?? s.lowerDeficitThreshold ?? 0,
        upperDeficitThreshold: s.maxAvailabilityPct ?? s.upperDeficitThreshold ?? 25,
        payoutPercentage: s.payoutPercentage ?? 50
      })))
    );
  }

  createPayoutSlab(triggerRuleId: number, slab: any): Observable<PayoutSlabDTO> {
    const minPct = Number(slab.minAvailabilityPct ?? slab.lowerDeficitThreshold ?? 0);
    const maxPct = Number(slab.maxAvailabilityPct ?? slab.upperDeficitThreshold ?? 25);
    const payload = {
      minAvailabilityPct: minPct,
      maxAvailabilityPct: maxPct,
      payoutPercentage: Number(slab.payoutPercentage ?? 50)
    };
    return this.http.post<any>(this.baseUrl + '/admin/trigger-rules/' + triggerRuleId + '/payout-slabs', payload).pipe(
      map(s => ({
        ...s,
        payoutSlabId: s.payoutSlabId ?? s.slabId ?? s.id,
        triggerRuleId: s.triggerRuleId ?? triggerRuleId,
        minAvailabilityPct: s.minAvailabilityPct ?? minPct,
        maxAvailabilityPct: s.maxAvailabilityPct ?? maxPct,
        lowerDeficitThreshold: s.minAvailabilityPct ?? minPct,
        upperDeficitThreshold: s.maxAvailabilityPct ?? maxPct,
        payoutPercentage: s.payoutPercentage ?? payload.payoutPercentage
      }))
    );
  }
}
