import { Component, OnInit, inject, ChangeDetectorRef, Optional } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { loadProfile, saveProfile } from '../../state/farms.actions';
import { farmerProfileSelector, farmsStatusSelector } from '../../state/farms.selector';
import { FarmsService } from '../../services/farms-service';
import { ClaimsService } from '../../../claims/services/claims-service';
import { ClaimResponseDTO, ClaimFeedbackReqDTO } from '../../../claims/models/ClaimResponseDTO';

@Component({
  selector: 'app-farmer-profile',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, CommonModule],
  templateUrl: './farmer-profile-component.html',
  styleUrl: './farmer-profile-component.css'
})
export class FarmerProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  @Optional() private farmsService = inject(FarmsService, { optional: true });
  @Optional() private claimsService = inject(ClaimsService, { optional: true });
  private cdr = inject(ChangeDetectorRef);

  profile$ = this.store.select(farmerProfileSelector);
  status$ = this.store.select(farmsStatusSelector);

  savedProfile: any = null;
  hasBackendRecord = false;
  isEditing = false;
  loading = true;
  successMessage = '';
  errorMessage = '';

  // Claims & Feedback integration
  settledClaims: ClaimResponseDTO[] = [];
  claimsLoading = false;
  activeFeedbackForms: { [key: number]: ClaimFeedbackReqDTO } = {};
  feedbackLoading: { [key: number]: boolean } = {};
  feedbackMessage: { [key: number]: { type: 'success' | 'error'; text: string } } = {};

  profileForm: FormGroup = this.fb.group({
    fullName: [''],
    phone: ['', [Validators.required]],
    address: ['', [Validators.required]],
    village: [''],
    district: [''],
    state: [''],
    postalCode: [''],
    experienceYears: [5, [Validators.min(0)]],
    bankAccountNumber: ['', [Validators.required]],
    bankIfscCode: ['SBIN0001234']
  });

  ngOnInit(): void {
    this.store.dispatch(loadProfile());
    this.fetchProfile();
    this.fetchFarmerClaims();
  }

  fetchProfile(): void {
    if (!this.farmsService) {
      this.loading = false;
      this.isEditing = true;
      return;
    }
    this.loading = true;
    this.farmsService.getProfile().subscribe({
      next: (profile) => {
        this.loading = false;
        const isAutoPlaceholder = profile && profile.addressLine === 'Plot 42 Farm Road' && profile.phoneNumber === '9876543210';
        if (profile && profile.farmerId && !isAutoPlaceholder) {
          this.savedProfile = profile;
          this.hasBackendRecord = true;
          this.isEditing = false;
          this.populateForm(profile);
        } else {
          this.savedProfile = null;
          this.hasBackendRecord = !!(profile && profile.farmerId);
          this.isEditing = true;
          const rawUser = localStorage.getItem('currentUser');
          const userObj = rawUser ? JSON.parse(rawUser) : {};
          this.profileForm.patchValue({
            fullName: profile?.fullName || userObj?.name || '',
            phone: '',
            address: '',
            district: '',
            state: '',
            bankAccountNumber: ''
          });
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.savedProfile = null;
        this.hasBackendRecord = false;
        this.isEditing = true;
        this.cdr.detectChanges();
      }
    });
  }

  fetchFarmerClaims(): void {
    if (!this.claimsService) return;
    this.claimsLoading = true;
    this.claimsService.getMyClaims().subscribe({
      next: (claims) => {
        this.claimsLoading = false;
        this.settledClaims = claims || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.claimsLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openFeedbackForm(claimId: number): void {
    this.activeFeedbackForms[claimId] = {
      feedbackStatus: 'SATISFIED',
      feedbackRating: 5,
      feedbackComments: '',
      requestedAdditionalAmount: 0
    };
    this.cdr.detectChanges();
  }

  cancelFeedbackForm(claimId: number): void {
    delete this.activeFeedbackForms[claimId];
    this.cdr.detectChanges();
  }

  onSubmitFeedback(claimId: number): void {
    const form = this.activeFeedbackForms[claimId];
    if (!form || !this.claimsService) return;
    this.feedbackLoading[claimId] = true;
    this.claimsService.submitClaimFeedback(claimId, form).subscribe({
      next: (updated) => {
        this.feedbackLoading[claimId] = false;
        delete this.activeFeedbackForms[claimId];
        this.feedbackMessage[claimId] = { type: 'success', text: 'Thank you! Your feedback has been recorded.' };
        const idx = this.settledClaims.findIndex(c => c.claimId === updated.claimId);
        if (idx !== -1) {
          this.settledClaims[idx] = updated;
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.feedbackLoading[claimId] = false;
        this.feedbackMessage[claimId] = { type: 'error', text: err.error?.message || 'Failed to submit feedback.' };
        this.cdr.detectChanges();
      }
    });
  }

  private populateForm(profile: any): void {
    this.profileForm.patchValue({
      fullName: profile.fullName || '',
      phone: profile.phoneNumber || profile.phone || '',
      address: profile.addressLine || profile.address || '',
      village: profile.village || '',
      district: profile.district || '',
      state: profile.state || '',
      postalCode: profile.postalCode || '',
      experienceYears: profile.experienceYears ?? 5,
      bankAccountNumber: profile.bankAccountLastFour || profile.bankAccountNumber || '',
      bankIfscCode: profile.bankIfscCode || 'SBIN0001234'
    });
  }

  startEdit(): void {
    if (this.savedProfile) {
      this.populateForm(this.savedProfile);
    }
    this.successMessage = '';
    this.errorMessage = '';
    this.isEditing = true;
    this.cdr.detectChanges();
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.errorMessage = '';
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.profileForm.valid) {
      const formVal = this.profileForm.value;
      this.store.dispatch(saveProfile({ profile: formVal }));

      if (this.farmsService) {
        const isUpdate = !!this.savedProfile || this.hasBackendRecord;
        this.farmsService.saveProfile(formVal, isUpdate).subscribe({
          next: (saved) => {
            this.savedProfile = saved;
            this.hasBackendRecord = true;
            this.isEditing = false;
            this.successMessage = isUpdate && this.savedProfile
              ? 'Farmer KYC profile updated successfully.'
              : 'Farmer KYC profile created and saved.';
            this.errorMessage = '';
            this.cdr.detectChanges();
          },
          error: (err) => {
            const valErrs = err.error?.validationErrors
              ? Object.values(err.error.validationErrors).join(', ')
              : '';
            this.errorMessage = valErrs || err.error?.message || 'Failed to save farmer profile.';
            this.cdr.detectChanges();
          }
        });
      }
    } else {
      this.profileForm.markAllAsTouched();
    }
  }
}
