import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { submitReading } from '../../state/claims.actions';
import { latestSubmittedClaimSelector, claimsStatusSelector } from '../../state/claims.selector';
import { ClaimsService } from '../../services/claims-service';
import { ClaimResponseDTO } from '../../models/ClaimResponseDTO';

@Component({
  selector: 'app-data-provider',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './data-provider-component.html',
  styleUrl: './data-provider-component.css'
})
export class DataProviderComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private claimsService = inject(ClaimsService);
  private cdr = inject(ChangeDetectorRef);

  latestClaim$ = this.store.select(latestSubmittedClaimSelector);
  status$ = this.store.select(claimsStatusSelector);

  latestClaim: ClaimResponseDTO | null = null;
  activePolicies: any[] = [];
  selectedPolicy: any | null = null;
  submitting = false;
  errorMessage = '';

  readingForm: FormGroup = this.fb.group({
    policyId: [1, [Validators.required, Validators.min(1)]],
    availabilityPercentage: [25, [Validators.required, Validators.min(0), Validators.max(100)]]
  });

  ngOnInit(): void {
    this.loadActivePolicies();
  }

  loadActivePolicies(): void {
    this.claimsService.getActivePolicyConfigurations().subscribe({
      next: (results) => {
        this.activePolicies = (results || []).filter(r => r !== null);
        const policyControl = this.readingForm.get('policyId');
        if (this.activePolicies.length > 0 && !this.selectedPolicy && !policyControl?.dirty) {
          this.onSelectPolicy(this.activePolicies[0]);
        } else if (policyControl?.dirty) {
          const matched = this.activePolicies.find(p => p.policyId === Number(policyControl.value));
          if (matched) {
            this.selectedPolicy = matched;
          }
        }
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });
  }

  onSelectPolicy(pol: any): void {
    this.selectedPolicy = pol;
    this.readingForm.patchValue({ policyId: pol.policyId });
    this.errorMessage = '';
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.readingForm.valid) {
      this.submitting = true;
      this.errorMessage = '';
      const payload = {
        policyId: Number(this.readingForm.value.policyId),
        availabilityPercentage: Number(this.readingForm.value.availabilityPercentage)
      };
      this.store.dispatch(submitReading({ reading: payload }));

      this.claimsService.submitReading(payload).subscribe({
        next: (res) => {
          this.submitting = false;
          this.latestClaim = res;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.submitting = false;
          this.errorMessage = err.error?.message || 'Failed to submit reading. Ensure the Policy ID is active.';
          this.cdr.detectChanges();
        }
      });
    }
  }
}
