import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClaimsService } from './claims-service';

describe('ClaimsService', () => {
  let service: ClaimsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ClaimsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should submit an irrigation reading via POST /api/readings', () => {
    const mockClaim = {
      claimId: 1,
      policyId: 1,
      availabilityPercentage: 15,
      thresholdBreached: true,
      payoutPercentage: 50,
      compensationAmount: 40000,
      payoutStatus: 'PROCESSED',
      claimStatus: 'APPROVED'
    };
    service.submitReading({ policyId: 1, availabilityPercentage: 15 }).subscribe(res => {
      expect(res.claimStatus).toBe('APPROVED');
      expect(res.compensationAmount).toBe(40000);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/readings');
    expect(req.request.method).toBe('POST');
    req.flush(mockClaim);
  });

  it('should fetch farmer claims via GET /api/claims/my-claims', () => {
    service.getMyClaims().subscribe(claims => {
      expect(claims.length).toBe(1);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/claims/my-claims');
    expect(req.request.method).toBe('GET');
    req.flush([{ claimId: 1, policyId: 1, availabilityPercentage: 15, thresholdBreached: true, payoutPercentage: 50, compensationAmount: 40000, payoutStatus: 'PROCESSED', claimStatus: 'APPROVED' }]);
  });
});
