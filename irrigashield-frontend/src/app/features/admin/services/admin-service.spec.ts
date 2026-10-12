import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin-service';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch users and update user status', () => {
    service.getUsers().subscribe(users => {
      expect(users.length).toBe(1);
    });
    const getReq = httpMock.expectOne('http://localhost:8080/api/admin/users');
    expect(getReq.request.method).toBe('GET');
    getReq.flush([{ userId: 1, name: 'Admin', email: 'admin@test.com', role: 'ADMIN', status: 'ACTIVE' }]);

    service.updateUserStatus(1, 'INACTIVE').subscribe(updated => {
      expect(updated.status).toBe('INACTIVE');
    });
    const putReq = httpMock.expectOne('http://localhost:8080/api/admin/users/1/status');
    expect(putReq.request.method).toBe('PUT');
    putReq.flush({ userId: 1, name: 'Admin', email: 'admin@test.com', role: 'ADMIN', status: 'INACTIVE' });
  });

  it('should create an insurance plan and a payout slab', () => {
    service.createPlan({
      planName: 'Rabi Shield',
      description: 'Wheat protection',
      cropType: 'WHEAT',
      season: 'RABI',
      triggerType: 'SOIL_MOISTURE',
      maxCoverageAmount: 100000,
      basePremiumRate: 5
    }).subscribe(plan => {
      expect(plan.planName).toBe('Rabi Shield');
    });

    const planReq = httpMock.expectOne('http://localhost:8080/api/admin/plans');
    expect(planReq.request.method).toBe('POST');
    planReq.flush({ planId: 10, planName: 'Rabi Shield', maximumCoverage: 100000, basePremium: 5000 });

    service.createPayoutSlab(10, {
      lowerDeficitThreshold: 0,
      upperDeficitThreshold: 20,
      payoutPercentage: 40
    } as any).subscribe(slab => {
      expect(slab.payoutPercentage).toBe(40);
    });
    const slabReq = httpMock.expectOne('http://localhost:8080/api/admin/trigger-rules/10/payout-slabs');
    expect(slabReq.request.method).toBe('POST');
    slabReq.flush({ slabId: 1, minAvailabilityPct: 0, maxAvailabilityPct: 20, payoutPercentage: 40 });
  });
});
