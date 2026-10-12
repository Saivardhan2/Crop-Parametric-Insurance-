import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore } from '@ngrx/store';
import { of } from 'rxjs';
import { DataProviderComponent } from './data-provider-component';
import { claimsReducer } from '../../state/claims.reducer';
import { ClaimsService } from '../../services/claims-service';
import { vi } from 'vitest';

describe('DataProviderComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataProviderComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ claims: claimsReducer })
      ]
    }).compileComponents();
  });

  it('should create component and validate readingForm bounds', () => {
    const fixture = TestBed.createComponent(DataProviderComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.readingForm.valid).toBe(true);

    component.readingForm.patchValue({ availabilityPercentage: 150 });
    expect(component.readingForm.valid).toBe(false);
  });

  it('should submit reading via ClaimsService when form is valid', () => {
    const claimsService = TestBed.inject(ClaimsService);
    const mockClaim = {
      claimId: 10,
      policyId: 1,
      availabilityPercentage: 20,
      thresholdBreached: true,
      payoutPercentage: 50,
      compensationAmount: 50000,
      payoutStatus: 'PROCESSED',
      claimStatus: 'APPROVED',
      recordedAt: '2026-10-10T10:00:00'
    };
    const submitSpy = vi.spyOn(claimsService, 'submitReading').mockReturnValue(of(mockClaim));

    const fixture = TestBed.createComponent(DataProviderComponent);
    const component = fixture.componentInstance;

    component.readingForm.setValue({ policyId: 1, availabilityPercentage: 20 });
    component.onSubmit();

    expect(submitSpy).toHaveBeenCalledWith({ policyId: 1, availabilityPercentage: 20 });
    expect(component.latestClaim?.claimId).toBe(10);
  });
});
