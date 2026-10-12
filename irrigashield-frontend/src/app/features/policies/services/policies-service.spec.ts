import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PoliciesService } from './policies-service';

describe('PoliciesService', () => {
  let service: PoliciesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PoliciesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch active plans and farmer policies', () => {
    service.getActivePlans().subscribe(plans => {
      expect(plans.length).toBe(1);
      expect(plans[0].maxCoverageAmount).toBe(100000);
    });
    const plansReq = httpMock.expectOne('http://localhost:8080/api/plans/active');
    expect(plansReq.request.method).toBe('GET');
    plansReq.flush([{ planId: 1, planName: 'Kharif Shield', maximumCoverage: 100000, basePremium: 5000 }]);

    service.getMyPolicies().subscribe(policies => {
      expect(policies[0].policyStatus).toBe('ACTIVE');
    });
    const polReq = httpMock.expectOne('http://localhost:8080/api/policies/my-policies');
    expect(polReq.request.method).toBe('GET');
    polReq.flush([{ policyId: 1, policyNumber: 'POL-101', status: 'ACTIVE', premiumAmount: 2500 }]);
  });

  it('should approve an underwriting application', () => {
    service.approveApplication(7, {
      approvedCoverageAmount: 80000,
      quotedPremiumAmount: 4000,
      remarks: 'Verified'
    }).subscribe(res => {
      expect(res.status).toBe('APPROVED');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/underwriting/applications/7/approve');
    expect(req.request.method).toBe('PUT');
    req.flush({ applicationId: 7, applicationStatus: 'APPROVED', recommendedCoverage: 80000, recommendedPremium: 4000 });
  });
});
