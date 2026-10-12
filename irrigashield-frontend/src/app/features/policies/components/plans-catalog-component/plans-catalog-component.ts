import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { loadActivePlans, createApplication } from '../../state/policies.actions';
import { activePlansSelector } from '../../state/policies.selector';
import { loadFarms, loadCrops } from '../../../farms/state/farms.actions';
import { farmsListSelector, selectedFarmCropsSelector } from '../../../farms/state/farms.selector';
import { FarmsService } from '../../../farms/services/farms-service';
import { PoliciesService } from '../../services/policies-service';
import { InsurancePlanDTO } from '../../models/InsurancePlanDTO';
import { FarmDTO } from '../../../farms/models/FarmDTO';
import { CropDTO } from '../../../farms/models/CropDTO';

@Component({
  selector: 'app-plans-catalog',
  standalone: true,
  imports: [ReactiveFormsModule, AsyncPipe],
  templateUrl: './plans-catalog-component.html',
  styleUrl: './plans-catalog-component.css'
})
export class PlansCatalogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private store = inject(Store);
  private router = inject(Router);
  private farmsService = inject(FarmsService);
  private policiesService = inject(PoliciesService);
  private cdr = inject(ChangeDetectorRef);

  plans$ = this.store.select(activePlansSelector);
  farms$ = this.store.select(farmsListSelector);
  crops$ = this.store.select(selectedFarmCropsSelector);

  plansList: InsurancePlanDTO[] = [];
  farmsList: FarmDTO[] = [];
  cropsList: CropDTO[] = [];

  showApplyModal = false;
  selectedPlan: InsurancePlanDTO | null = null;

  applyForm: FormGroup = this.fb.group({
    farmId: [null, [Validators.required]],
    cropId: [null, [Validators.required]],
    requestedCoverageAmount: [50000, [Validators.required, Validators.min(1000)]]
  });

  ngOnInit(): void {
    this.store.dispatch(loadActivePlans());
    this.store.dispatch(loadFarms());

    this.policiesService.getActivePlans().subscribe({
      next: (plans) => {
        this.plansList = plans || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });

    this.farmsService.getMyFarms().subscribe({
      next: (farms) => {
        this.farmsList = farms || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });

    this.applyForm.get('farmId')?.valueChanges.subscribe(farmId => {
      if (farmId) {
        this.store.dispatch(loadCrops({ farmId: +farmId }));
        this.farmsService.getCrops(+farmId).subscribe({
          next: (crops) => {
            this.cropsList = crops || [];
            this.cdr.detectChanges();
          },
          error: () => {
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  openApplyModal(plan: InsurancePlanDTO): void {
    this.selectedPlan = plan;
    this.applyForm.patchValue({
      requestedCoverageAmount: plan.maxCoverageAmount || 50000
    });
    this.showApplyModal = true;
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showApplyModal = false;
    this.selectedPlan = null;
    this.cdr.detectChanges();
  }

  onApply(): void {
    if (this.applyForm.valid && this.selectedPlan?.planId) {
      this.store.dispatch(createApplication({
        application: {
          planId: this.selectedPlan.planId,
          farmId: +this.applyForm.value.farmId,
          cropId: +this.applyForm.value.cropId,
          requestedCoverageAmount: +this.applyForm.value.requestedCoverageAmount
        }
      }));
      this.closeModal();
      this.router.navigate(['/farmer/applications']);
    }
  }
}
