import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AdminPlansComponent } from './admin-plans-component';
import { AdminService } from '../../services/admin-service';
import { vi } from 'vitest';

describe('AdminPlansComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPlansComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
  });

  it('should create component and load plans on init', () => {
    const adminService = TestBed.inject(AdminService);
    vi.spyOn(adminService, 'getPlans').mockReturnValue(of([{
      planId: 1,
      planName: 'Rabi Shield',
      description: 'Wheat plan',
      cropType: 'WHEAT',
      season: 'RABI',
      triggerType: 'SOIL_MOISTURE',
      maxCoverageAmount: 100000,
      basePremiumRate: 5
    }]));

    const fixture = TestBed.createComponent(AdminPlansComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.plans.length).toBe(1);
    expect(fixture.componentInstance.plans[0].planName).toBe('Rabi Shield');
  });

  it('should validate planForm and slabForm', () => {
    const fixture = TestBed.createComponent(AdminPlansComponent);
    const component = fixture.componentInstance;

    expect(component.planForm.valid).toBe(false);
    component.planForm.patchValue({
      planName: 'Kharif Rice Plan',
      description: 'Parametric coverage for rice',
      cropType: 'RICE',
      season: 'KHARIF',
      maxCoverageAmount: 120000,
      basePremiumRate: 4.5
    });
    expect(component.planForm.valid).toBe(true);
    expect(component.slabForm.valid).toBe(true);
  });
});
