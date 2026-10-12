import { Component, OnInit, inject, ChangeDetectorRef, Optional } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { loadFarms, loadFarmsSuccess, createFarm, createFarmSuccess } from '../../state/farms.actions';
import { farmsListSelector } from '../../state/farms.selector';
import { FarmsService } from '../../services/farms-service';
import { PoliciesService } from '../../../policies/services/policies-service';
import { ClaimsService } from '../../../claims/services/claims-service';
import { FarmDTO } from '../../models/FarmDTO';

@Component({
  selector: 'app-farm-list',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './farm-list-component.html',
  styleUrl: './farm-list-component.css'
})
export class FarmListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  @Optional() private farmsService = inject(FarmsService, { optional: true });
  @Optional() private policiesService = inject(PoliciesService, { optional: true });
  @Optional() private claimsService = inject(ClaimsService, { optional: true });
  private cdr = inject(ChangeDetectorRef);

  farms$ = this.store.select(farmsListSelector);
  farmsList: FarmDTO[] = [];
  showAddModal = false;
  farmMessage = '';
  farmError = '';

  policiesCount = 0;
  applicationsCount = 0;
  claimsCount = 0;

  farmForm: FormGroup = this.fb.group({
    farmName: ['', [Validators.required, Validators.minLength(2)]],
    latitude: [23.0225, [Validators.required]],
    longitude: [72.5714, [Validators.required]],
    totalAreaHectares: [5.0, [Validators.required, Validators.min(0.1)]],
    soilType: ['SANDY_LOAM', [Validators.required]],
    waterSource: ['CANAL', [Validators.required]]
  });

  ngOnInit(): void {
    this.store.dispatch(loadFarms());
    this.refreshFarms();
    this.loadStats();
  }

  refreshFarms(): void {
    if (!this.farmsService) return;
    this.farmsService.getMyFarms().subscribe({
      next: (farms) => {
        this.farmsList = farms || [];
        this.store.dispatch(loadFarmsSuccess({ farms: this.farmsList }));
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });
  }

  loadStats(): void {
    if (this.policiesService) {
      this.policiesService.getMyPolicies().subscribe({
        next: (pols) => {
          this.policiesCount = (pols || []).filter(p => p.policyStatus === 'ACTIVE').length;
          this.cdr.detectChanges();
        },
        error: () => {}
      });

      this.policiesService.getMyApplications().subscribe({
        next: (apps) => {
          this.applicationsCount = (apps || []).length;
          this.cdr.detectChanges();
        },
        error: () => {}
      });
    }

    if (this.claimsService) {
      this.claimsService.getMyClaims().subscribe({
        next: (claims) => {
          this.claimsCount = (claims || []).length;
          this.cdr.detectChanges();
        },
        error: () => {}
      });
    }
  }

  openModal(): void {
    this.farmError = '';
    this.showAddModal = true;
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showAddModal = false;
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.farmForm.valid) {
      const val = this.farmForm.value;
      this.store.dispatch(createFarm({ farm: val }));
      this.showAddModal = false;

      if (this.farmsService) {
        this.farmsService.createFarm(val).subscribe({
          next: (created) => {
            this.farmsList = [created, ...this.farmsList.filter(f => f.farmId !== created.farmId)];
            this.store.dispatch(createFarmSuccess({ farm: created }));
            this.farmMessage = `Farm "${created.farmName}" registered successfully.`;
            this.farmError = '';
            this.cdr.detectChanges();
          },
          error: (err) => {
            const valErrs = err.error?.validationErrors
              ? Object.values(err.error.validationErrors).join(', ')
              : '';
            this.farmError = valErrs || err.error?.message || 'Failed to register farm.';
            this.cdr.detectChanges();
          }
        });
      }

      this.farmForm.reset({
        latitude: 23.0225,
        longitude: 72.5714,
        totalAreaHectares: 5.0,
        soilType: 'SANDY_LOAM',
        waterSource: 'CANAL'
      });
      this.cdr.detectChanges();
    }
  }
}
