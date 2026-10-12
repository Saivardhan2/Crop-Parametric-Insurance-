import { Component, OnInit, inject, ChangeDetectorRef, Optional } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { loadProfile, saveProfile } from '../../state/farms.actions';
import { farmerProfileSelector, farmsStatusSelector } from '../../state/farms.selector';
import { FarmsService } from '../../services/farms-service';

@Component({
  selector: 'app-farmer-profile',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './farmer-profile-component.html',
  styleUrl: './farmer-profile-component.css'
})
export class FarmerProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  @Optional() private farmsService = inject(FarmsService, { optional: true });
  private cdr = inject(ChangeDetectorRef);

  profile$ = this.store.select(farmerProfileSelector);
  status$ = this.store.select(farmsStatusSelector);

  savedProfile: any = null;
  hasBackendRecord = false;
  isEditing = false;
  loading = true;
  successMessage = '';
  errorMessage = '';

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
