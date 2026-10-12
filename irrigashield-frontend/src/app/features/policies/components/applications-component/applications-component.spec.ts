import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngrx/store';
import { of } from 'rxjs';
import { ApplicationsComponent } from './applications-component';
import { policiesReducer } from '../../state/policies.reducer';
import { PoliciesService } from '../../services/policies-service';
import { vi } from 'vitest';

describe('ApplicationsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ policies: policiesReducer })
      ]
    }).compileComponents();
  });

  it('should create component and dispatch loadMyApplications on init', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(ApplicationsComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(dispatchSpy).toHaveBeenCalled();
  });

  it('should submit application and accept quote via PoliciesService', () => {
    const policiesService = TestBed.inject(PoliciesService);
    const submitSpy = vi.spyOn(policiesService, 'submitApplication').mockReturnValue(of({ applicationId: 10 } as any));
    const acceptSpy = vi.spyOn(policiesService, 'acceptApprovedQuote').mockReturnValue(of({ policyId: 1 } as any));

    const fixture = TestBed.createComponent(ApplicationsComponent);
    const component = fixture.componentInstance;

    component.onSubmitApplication(10);
    component.onAcceptQuote(10);

    expect(submitSpy).toHaveBeenCalledWith(10);
    expect(acceptSpy).toHaveBeenCalledWith(10);
  });
});
