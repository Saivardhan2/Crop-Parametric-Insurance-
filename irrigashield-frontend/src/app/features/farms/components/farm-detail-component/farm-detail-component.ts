import { Component, OnInit, inject, ChangeDetectorRef, Optional } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { loadCrops, addCrop, loadIrrigation, saveIrrigation } from '../../state/farms.actions';
import { selectedFarmCropsSelector, selectedFarmIrrigationSelector } from '../../state/farms.selector';
import { FarmsService } from '../../services/farms-service';
import { FarmDTO } from '../../models/FarmDTO';
import { CropDTO } from '../../models/CropDTO';

@Component({
  selector: 'app-farm-detail',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './farm-detail-component.html',
  styleUrl: './farm-detail-component.css'
})
export class FarmDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private store = inject(Store);
  @Optional() private farmsService = inject(FarmsService, { optional: true });
  private cdr = inject(ChangeDetectorRef);

  farmId!: number;
  farm: FarmDTO | null = null;
  crops$ = this.store.select(selectedFarmCropsSelector);
  irrigation$ = this.store.select(selectedFarmIrrigationSelector);

  cropsList: CropDTO[] = [];
  savedIrrigation: any = null;
  isEditingIrrigation = false;
  showCropModal = false;

  irrigationMessage = '';
  irrigationError = '';
  cropMessage = '';

  cropForm: FormGroup = this.fb.group({
    cropName: ['', [Validators.required]],
    cropType: ['WHEAT', [Validators.required]],
    growthStage: ['VEGETATIVE', [Validators.required]],
    irrigationRequirement: ['MEDIUM', [Validators.required]],
    sowingDate: [new Date().toISOString().split('T')[0], [Validators.required]],
    expectedHarvestDate: [new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0], [Validators.required]],
    plantedAreaHectares: [3.0, [Validators.required, Validators.min(0.1)]]
  });

  irrigationForm: FormGroup = this.fb.group({
    irrigationType: ['DRIP', [Validators.required]],
    waterSource: ['BOREWELL', [Validators.required]],
    backupWaterSource: ['CANAL', [Validators.required]],
    equipmentCondition: ['GOOD', [Validators.required]],
    capacityLitersPerHour: [5000, [Validators.required, Validators.min(100)]],
    efficiencyPercentage: [85.0, [Validators.required, Validators.min(1), Validators.max(100)]],
    seasonalRisk: ['LOW', [Validators.required]],
    maintenanceFrequency: ['MONTHLY', [Validators.required]],
    historicalFailureCount: [0, [Validators.required, Validators.min(0)]],
    automationLevel: ['AUTOMATED', [Validators.required]]
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('farmId');
    if (idParam) {
      this.farmId = +idParam;
      this.store.dispatch(loadCrops({ farmId: this.farmId }));
      this.store.dispatch(loadIrrigation({ farmId: this.farmId }));
      this.refreshFarmDetails();
    }
  }

  refreshFarmDetails(): void {
    if (!this.farmsService || !this.farmId) return;

    this.farmsService.getFarm(this.farmId).subscribe({
      next: (f) => {
        this.farm = f;
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });

    this.farmsService.getCrops(this.farmId).subscribe({
      next: (crops) => {
        this.cropsList = crops || [];
        this.cdr.detectChanges();
      },
      error: () => this.cdr.detectChanges()
    });

    this.farmsService.getIrrigation(this.farmId).subscribe({
      next: (irr) => {
        if (irr && irr.irrigationId) {
          this.savedIrrigation = irr;
          this.isEditingIrrigation = false;
          this.populateIrrigationForm(irr);
        } else {
          this.savedIrrigation = null;
          this.isEditingIrrigation = false;
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.savedIrrigation = null;
        this.isEditingIrrigation = false;
        this.cdr.detectChanges();
      }
    });
  }

  private populateIrrigationForm(irr: any): void {
    this.irrigationForm.patchValue({
      irrigationType: irr.irrigationMethod || irr.irrigationType || 'DRIP',
      waterSource: irr.waterSource || 'BOREWELL',
      backupWaterSource: irr.backupWaterSource || 'NONE',
      equipmentCondition: irr.equipmentCondition || 'GOOD',
      capacityLitersPerHour: irr.capacityLitersPerHour || 5000,
      efficiencyPercentage: irr.averageWaterAvailabilityPercentage ?? irr.efficiencyPercentage ?? 85.0,
      seasonalRisk: irr.seasonalRisk || 'LOW',
      maintenanceFrequency: irr.maintenanceFrequency || 'MONTHLY',
      historicalFailureCount: irr.historicalFailureCount ?? 0,
      automationLevel: irr.automationLevel || 'AUTOMATED'
    });
  }

  startCreateOrEditIrrigation(): void {
    if (this.savedIrrigation) {
      this.populateIrrigationForm(this.savedIrrigation);
    }
    this.irrigationMessage = '';
    this.irrigationError = '';
    this.isEditingIrrigation = true;
    this.cdr.detectChanges();
  }

  cancelIrrigationEdit(): void {
    this.isEditingIrrigation = false;
    this.irrigationError = '';
    this.cdr.detectChanges();
  }

  onAddCrop(): void {
    if (this.cropForm.valid) {
      const val = this.cropForm.value;
      this.store.dispatch(addCrop({
        farmId: this.farmId,
        crop: { farmId: this.farmId, ...val }
      }));

      if (this.farmsService) {
        this.farmsService.addCrop(this.farmId, val).subscribe({
          next: (added) => {
            this.cropsList = [...this.cropsList.filter(c => c.cropId !== added.cropId), added];
            this.cropMessage = `Crop "${added.cropName}" added successfully.`;
            this.cdr.detectChanges();
          },
          error: () => this.cdr.detectChanges()
        });
      }

      this.showCropModal = false;
      this.cropForm.reset({
        cropType: 'WHEAT',
        growthStage: 'VEGETATIVE',
        irrigationRequirement: 'MEDIUM',
        sowingDate: new Date().toISOString().split('T')[0],
        expectedHarvestDate: new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0],
        plantedAreaHectares: 3.0
      });
      this.cdr.detectChanges();
    }
  }

  onSaveIrrigation(): void {
    if (this.irrigationForm.valid) {
      const val = this.irrigationForm.value;
      const isUpdate = !!this.savedIrrigation;
      this.store.dispatch(saveIrrigation({
        farmId: this.farmId,
        irrigation: { farmId: this.farmId, ...val }
      }));

      if (this.farmsService) {
        this.farmsService.saveIrrigation(this.farmId, val, isUpdate).subscribe({
          next: (saved) => {
            this.savedIrrigation = saved;
            this.isEditingIrrigation = false;
            this.irrigationMessage = isUpdate
              ? 'Irrigation telemetry profile updated successfully.'
              : 'Irrigation telemetry profile created and saved.';
            this.irrigationError = '';
            this.cdr.detectChanges();
          },
          error: (err) => {
            const valErrs = err.error?.validationErrors
              ? Object.values(err.error.validationErrors).join(', ')
              : '';
            this.irrigationError = valErrs || err.error?.message || 'Failed to save irrigation profile.';
            this.cdr.detectChanges();
          }
        });
      }
    }
  }
}
