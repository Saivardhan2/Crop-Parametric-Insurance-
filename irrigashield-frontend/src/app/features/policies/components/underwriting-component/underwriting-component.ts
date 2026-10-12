import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { SlicePipe } from '@angular/common';
import { loadPendingReviews } from '../../state/policies.actions';
import { pendingReviewsSelector } from '../../state/policies.selector';
import { PoliciesService } from '../../services/policies-service';
import { PolicyApplicationDTO } from '../../models/PolicyApplicationDTO';

@Component({
  selector: 'app-underwriting',
  standalone: true,
  imports: [ReactiveFormsModule, SlicePipe],
  templateUrl: './underwriting-component.html',
  styleUrl: './underwriting-component.css'
})
export class UnderwritingComponent implements OnInit {
  private store = inject(Store);
  private fb = inject(FormBuilder);
  private policiesService = inject(PoliciesService);
  private cdr = inject(ChangeDetectorRef);

  applications$ = this.store.select(pendingReviewsSelector);

  activeTab: 'pending' | 'in-progress' | 'history' = 'pending';

  pendingAppsList: PolicyApplicationDTO[] = [];
  myCases: any[] = [];
  selectedApp: any | null = null;
  riskAssessment: any = null;
  isEditingCompletedDecision = false;

  reviewMessage = '';
  reviewError = '';

  reviewForm: FormGroup = this.fb.group({
    action: ['APPROVE', [Validators.required]],
    approvedCoverageAmount: [50000, [Validators.required, Validators.min(1000)]],
    quotedPremiumAmount: [2500, [Validators.required, Validators.min(100)]],
    remarks: ['Approved after telemetry and irrigation capacity verification.', [Validators.required]]
  });

  ngOnInit(): void {
    this.refreshAll();
  }

  refreshAll(): void {
    this.refreshPending();
    this.refreshMyCases();
  }

  refreshPending(): void {
    this.store.dispatch(loadPendingReviews());
    this.policiesService.getPendingApplications().subscribe({
      next: (apps) => {
        this.pendingAppsList = apps || [];
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });
  }

  refreshMyCases(): void {
    this.policiesService.getMyCases().subscribe({
      next: (cases) => {
        this.myCases = cases || [];
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });
  }

  get inProgressCases(): any[] {
    return this.myCases.filter(c => c.underwritingStatus === 'UNDER_REVIEW' || c.applicationStatus === 'UNDER_REVIEW');
  }

  get completedCases(): any[] {
    return this.myCases.filter(c => c.underwritingStatus === 'APPROVED' || c.underwritingStatus === 'REJECTED' || c.decision === 'APPROVED' || c.decision === 'REJECTED');
  }

  get approvalRate(): string {
    const total = this.completedCases.length;
    if (total === 0) return 'N/A';
    const approved = this.completedCases.filter(c => c.decision === 'APPROVED' || c.underwritingStatus === 'APPROVED').length;
    return `${Math.round((approved / total) * 100)}%`;
  }

  selectApplication(app: any): void {
    this.selectedApp = app;
    this.riskAssessment = null;
    this.isEditingCompletedDecision = false;
    this.reviewMessage = '';
    this.reviewError = '';

    const cov = app.requestedCoverageAmount || app.requestedCoverage || app.recommendedCoverage || 50000;
    const prem = app.recommendedPremium || Math.round(cov * 0.05);
    const currentDecision = app.decision || app.underwritingStatus;

    this.reviewForm.patchValue({
      action: currentDecision === 'REJECTED' ? 'APPROVE' : (currentDecision === 'APPROVED' ? 'REJECT' : 'APPROVE'),
      approvedCoverageAmount: cov,
      quotedPremiumAmount: prem,
      remarks: app.remarks || 'Approved after telemetry and irrigation capacity verification.'
    });

    if (app.overallRiskScore) {
      this.riskAssessment = {
        overallRiskScore: app.overallRiskScore,
        riskLevel: app.riskLevel,
        recommendedPremium: app.recommendedPremium,
        recommendedCoverage: app.recommendedCoverage,
        waterAvailabilityScore: app.waterAvailabilityScore,
        equipmentConditionScore: app.equipmentConditionScore,
        historicalFailureScore: app.historicalFailureScore,
        seasonalRiskScore: app.seasonalRiskScore,
        backupSourceScore: app.backupSourceScore
      };
    }

    this.cdr.detectChanges();
  }

  startChangeDecision(): void {
    if (!this.selectedApp) return;
    const currentDecision = this.selectedApp.decision || this.selectedApp.underwritingStatus;
    const nextAction = currentDecision === 'REJECTED' ? 'APPROVE' : 'REJECT';
    this.reviewForm.patchValue({
      action: nextAction,
      remarks: nextAction === 'APPROVE'
        ? 'Decision updated to APPROVED after secondary underwriting review.'
        : 'Decision updated to REJECTED after secondary underwriting review.'
    });
    this.isEditingCompletedDecision = true;
    this.cdr.detectChanges();
  }

  quickFlipDecision(app: any): void {
    const appId = app?.applicationId;
    if (!appId) return;
    const currentDecision = app.decision || app.underwritingStatus;
    const cov = app.recommendedCoverage || app.approvedCoverageAmount || app.requestedCoverageAmount || app.requestedCoverage || 50000;
    const prem = app.recommendedPremium || app.quotedPremiumAmount || Math.round(cov * 0.05);

    if (currentDecision === 'REJECTED') {
      this.policiesService.approveApplication(appId, {
        approvedCoverageAmount: +cov,
        quotedPremiumAmount: +prem,
        remarks: 'Decision changed from REJECTED to APPROVED by underwriter.'
      }).subscribe({
        next: (updated) => {
          this.reviewMessage = `Application #${appId} decision changed from REJECTED to APPROVED.`;
          this.reviewError = '';
          this.selectedApp = updated;
          this.isEditingCompletedDecision = false;
          this.refreshAll();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.reviewError = err.error?.message || 'Failed to change decision to APPROVED.';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.policiesService.rejectApplication(appId, {
        remarks: 'Decision changed from APPROVED to REJECTED by underwriter.'
      }).subscribe({
        next: (updated) => {
          this.reviewMessage = `Application #${appId} decision changed from APPROVED to REJECTED.`;
          this.reviewError = '';
          this.selectedApp = updated;
          this.isEditingCompletedDecision = false;
          this.refreshAll();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.reviewError = err.error?.message || 'Failed to change decision to REJECTED.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onStartReview(appId?: number): void {
    if (appId) {
      this.reviewMessage = '';
      this.reviewError = '';
      this.policiesService.startReview(appId).subscribe({
        next: (caseRes) => {
          this.refreshAll();
          this.activeTab = 'in-progress';
          this.selectApplication(caseRes);
          this.runAssessment(appId);
        },
        error: () => {
          this.refreshAll();
          this.activeTab = 'in-progress';
          this.runAssessment(appId);
        }
      });
    }
  }

  onAssessRisk(appId?: number): void {
    if (appId) {
      this.runAssessment(appId);
    }
  }

  private runAssessment(appId: number): void {
    this.policiesService.assessRisk(appId).subscribe({
      next: (res) => {
        this.riskAssessment = res;
        if (res.recommendedPremium) {
          this.reviewForm.patchValue({
            quotedPremiumAmount: res.recommendedPremium,
            approvedCoverageAmount: res.recommendedCoverage || this.reviewForm.value.approvedCoverageAmount
          });
        }
        this.reviewMessage = 'Automated IoT & irrigation risk assessment computed.';
        this.refreshMyCases();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.reviewError = err.error?.message || 'Error running risk assessment';
        this.cdr.detectChanges();
      }
    });
  }

  onSubmitReview(): void {
    if (this.reviewForm.valid && this.selectedApp?.applicationId) {
      const appId = this.selectedApp.applicationId;
      const { action, approvedCoverageAmount, quotedPremiumAmount, remarks } = this.reviewForm.value;

      if (action === 'APPROVE') {
        this.policiesService.approveApplication(appId, {
          approvedCoverageAmount: +approvedCoverageAmount,
          quotedPremiumAmount: +quotedPremiumAmount,
          remarks
        }).subscribe({
          next: () => {
            this.reviewMessage = `Application #${appId} successfully APPROVED. Farmer notified.`;
            this.reviewError = '';
            this.isEditingCompletedDecision = false;
            this.selectedApp = null;
            this.refreshAll();
            this.activeTab = 'history';
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.reviewError = err.error?.message || 'Failed to approve application.';
            this.cdr.detectChanges();
          }
        });
      } else {
        this.policiesService.rejectApplication(appId, { remarks }).subscribe({
          next: () => {
            this.reviewMessage = `Application #${appId} REJECTED. Remarks recorded.`;
            this.reviewError = '';
            this.isEditingCompletedDecision = false;
            this.selectedApp = null;
            this.refreshAll();
            this.activeTab = 'history';
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.reviewError = err.error?.message || 'Failed to reject application.';
            this.cdr.detectChanges();
          }
        });
      }
    }
  }
}
