import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngrx/store';
import { PlansCatalogComponent } from './plans-catalog-component';
import { policiesReducer } from '../../state/policies.reducer';
import { farmsReducer } from '../../../farms/state/farms.reducer';
import { vi } from 'vitest';

describe('PlansCatalogComponent', () => {
  const mockPlan = {
    planId: 3,
    planName: 'Rabi Shield',
    description: 'Desc',
    cropType: 'WHEAT',
    season: 'RABI',
    triggerType: 'SOIL_MOISTURE',
    maxCoverageAmount: 90000,
    basePremiumRate: 5
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlansCatalogComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ policies: policiesReducer, farms: farmsReducer })
      ]
    }).compileComponents();
  });

  it('should open and close apply modal with selected plan', () => {
    const fixture = TestBed.createComponent(PlansCatalogComponent);
    const component = fixture.componentInstance;

    component.openApplyModal(mockPlan);
    expect(component.showApplyModal).toBe(true);
    expect(component.selectedPlan?.planId).toBe(3);

    component.closeModal();
    expect(component.showApplyModal).toBe(false);
    expect(component.selectedPlan).toBeNull();
  });

  it('should dispatch createApplication when applyForm is valid', () => {
    const store = TestBed.inject(Store);
    const router = TestBed.inject(Router);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const navSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    const fixture = TestBed.createComponent(PlansCatalogComponent);
    const component = fixture.componentInstance;

    component.openApplyModal(mockPlan);
    component.applyForm.setValue({ farmId: 1, cropId: 2, requestedCoverageAmount: 75000 });
    component.onApply();

    expect(dispatchSpy).toHaveBeenCalled();
    expect(navSpy).toHaveBeenCalledWith(['/farmer/applications']);
  });
});
