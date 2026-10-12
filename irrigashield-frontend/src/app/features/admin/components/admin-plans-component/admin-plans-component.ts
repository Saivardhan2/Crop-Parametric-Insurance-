import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../services/admin-service';

@Component({
  selector: 'app-admin-plans',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-plans-component.html',
  styleUrl: './admin-plans-component.css'
})
export class AdminPlansComponent implements OnInit {
  private adminService = inject(AdminService);
  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);

  plans: any[] = [];
  selectedPlan: any | null = null;
  triggerRules: any[] = [];
  selectedRule: any | null = null;
  payoutSlabs: any[] = [];

  showPlanModal = false;
  feedbackMsg = '';
  errorMsg = '';

  planForm: FormGroup = this.fb.group({
    planName: ['', [Validators.required]],
    description: ['', [Validators.required]],
    cropType: ['WHEAT', [Validators.required]],
    season: ['RABI', [Validators.required]],
    maxCoverageAmount: [100000, [Validators.required, Validators.min(1000)]],
    basePremiumRate: [5, [Validators.required, Validators.min(0.1), Validators.max(100)]]
  });

  triggerForm: FormGroup = this.fb.group({
    cropType: ['WHEAT', [Validators.required]],
    growthStage: ['FLOWERING', [Validators.required]],
    availabilityThresholdPercentage: [40, [Validators.required, Validators.min(1), Validators.max(100)]]
  });

  slabForm: FormGroup = this.fb.group({
    lowerDeficitThreshold: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
    upperDeficitThreshold: [25, [Validators.required, Validators.min(0), Validators.max(100)]],
    payoutPercentage: [50, [Validators.required, Validators.min(1), Validators.max(100)]]
  });

  ngOnInit(): void {
    this.loadPlans();
  }

  loadPlans(): void {
    this.adminService.getPlans().subscribe({
      next: (plans) => {
        this.plans = plans || [];
        if (this.plans.length > 0 && !this.selectedPlan) {
          this.selectPlan(this.plans[0]);
        }
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });
  }

  selectPlan(plan: any): void {
    this.selectedPlan = plan;
    this.selectedRule = null;
    this.payoutSlabs = [];
    this.errorMsg = '';
    const planId = plan.planId ?? plan.id;
    if (planId) {
      this.loadTriggerRules(planId);
    }
    this.cdr.detectChanges();
  }

  loadTriggerRules(planId: number): void {
    this.adminService.getTriggerRules(planId).subscribe({
      next: (rules) => {
        this.triggerRules = rules || [];
        if (this.triggerRules.length > 0) {
          const currentId = this.selectedRule?.triggerRuleId;
          const found = currentId ? this.triggerRules.find(r => r.triggerRuleId === currentId) : null;
          this.selectTriggerRule(found || this.triggerRules[0]);
        } else {
          this.selectedRule = null;
          this.payoutSlabs = [];
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.triggerRules = [];
        this.cdr.detectChanges();
      }
    });
  }

  selectTriggerRule(rule: any): void {
    this.selectedRule = rule;
    const ruleId = rule?.triggerRuleId ?? rule?.id;
    if (ruleId) {
      this.loadPayoutSlabs(ruleId);
    }
    this.cdr.detectChanges();
  }

  loadPayoutSlabs(triggerRuleId: number): void {
    this.adminService.getPayoutSlabs(triggerRuleId).subscribe({
      next: (slabs) => {
        this.payoutSlabs = (slabs || []).sort((a, b) => (a.lowerDeficitThreshold ?? 0) - (b.lowerDeficitThreshold ?? 0));
        this.cdr.detectChanges();
      },
      error: () => {
        this.payoutSlabs = [];
        this.cdr.detectChanges();
      }
    });
  }

  onCreatePlan(): void {
    if (this.planForm.valid) {
      this.errorMsg = '';
      this.adminService.createPlan(this.planForm.value).subscribe({
        next: (created) => {
          this.showPlanModal = false;
          this.feedbackMsg = `Plan "${created.planName}" created.`;
          this.planForm.reset({
            planName: '',
            description: '',
            cropType: 'WHEAT',
            season: 'RABI',
            maxCoverageAmount: 100000,
            basePremiumRate: 5
          });
          this.plans = [created, ...this.plans.filter(p => (p.planId ?? p.id) !== (created.planId ?? (created as any).id))];
          this.selectedPlan = created;
          this.triggerRules = [];
          this.selectedRule = null;
          this.payoutSlabs = [];
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMsg = err?.error?.message || 'Failed to create insurance plan.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onCreateTrigger(): void {
    const planId = this.selectedPlan?.planId ?? this.selectedPlan?.id;
    if (this.triggerForm.valid && planId) {
      this.errorMsg = '';
      this.adminService.createTriggerRule(planId, this.triggerForm.value).subscribe({
        next: (createdRule) => {
          this.triggerRules = [createdRule, ...this.triggerRules.filter(r => r.triggerRuleId !== createdRule.triggerRuleId)];
          this.selectedRule = createdRule;
          this.payoutSlabs = [];
          this.feedbackMsg = `Trigger Rule added for ${createdRule.cropType || ''} (${createdRule.growthStage || ''}).`;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMsg = err?.error?.message || 'Trigger rule for this Crop + Stage already exists on this plan.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onCreateSlab(): void {
    const ruleId = this.selectedRule?.triggerRuleId ?? this.selectedRule?.id;
    if (this.slabForm.valid && ruleId) {
      const val = this.slabForm.value;
      if (Number(val.lowerDeficitThreshold) >= Number(val.upperDeficitThreshold)) {
        this.errorMsg = 'Min Water % must be less than Max Water %.';
        this.cdr.detectChanges();
        return;
      }
      this.errorMsg = '';
      this.adminService.createPayoutSlab(ruleId, val).subscribe({
        next: (createdSlab) => {
          this.payoutSlabs = [...this.payoutSlabs, createdSlab].sort((a, b) => (a.lowerDeficitThreshold ?? 0) - (b.lowerDeficitThreshold ?? 0));
          this.feedbackMsg = `Payout Slab (${val.lowerDeficitThreshold}% - ${val.upperDeficitThreshold}% Water -> ${val.payoutPercentage}% Payout) added.`;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMsg = err?.error?.message || 'Failed to add payout slab.';
          this.cdr.detectChanges();
        }
      });
    }
  }
}
