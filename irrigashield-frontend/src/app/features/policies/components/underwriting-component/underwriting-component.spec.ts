import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore } from '@ngrx/store';
import { of } from 'rxjs';
import { UnderwritingComponent } from './underwriting-component';
import { policiesReducer } from '../../state/policies.reducer';
import { PoliciesService } from '../../services/policies-service';
import { vi } from 'vitest';

describe('UnderwritingComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnderwritingComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ policies: policiesReducer })
      ]
    }).compileComponents();
  });

  it('should filter in-progress and completed cases and compute approvalRate', () => {
    const fixture = TestBed.createComponent(UnderwritingComponent);
    const component = fixture.componentInstance;

    component.myCases = [
      { applicationId: 1, underwritingStatus: 'UNDER_REVIEW' },
      { applicationId: 2, underwritingStatus: 'APPROVED', decision: 'APPROVED' },
      { applicationId: 3, underwritingStatus: 'REJECTED', decision: 'REJECTED' }
    ];

    expect(component.inProgressCases.length).toBe(1);
    expect(component.completedCases.length).toBe(2);
    expect(component.approvalRate).toBe('50%');
  });

  it('should select application and submit approval review', () => {
    const policiesService = TestBed.inject(PoliciesService);
    const approveSpy = vi.spyOn(policiesService, 'approveApplication').mockReturnValue(of({ applicationId: 5, status: 'APPROVED' } as any));

    const fixture = TestBed.createComponent(UnderwritingComponent);
    const component = fixture.componentInstance;

    component.selectApplication({ applicationId: 5, requestedCoverageAmount: 60000 });
    component.onSubmitReview();

    expect(approveSpy).toHaveBeenCalled();
    expect(component.activeTab).toBe('history');
  });
});
