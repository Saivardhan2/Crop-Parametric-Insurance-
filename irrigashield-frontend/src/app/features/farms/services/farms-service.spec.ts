import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FarmsService } from './farms-service';

describe('FarmsService', () => {
  let service: FarmsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(FarmsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch farmer profile via GET /api/farmers/profile/me', () => {
    service.getProfile().subscribe(profile => {
      expect(profile.phone).toBe('9876543210');
    });
    const getReq = httpMock.expectOne('http://localhost:8080/api/farmers/profile/me');
    expect(getReq.request.method).toBe('GET');
    getReq.flush({ farmerId: 1, authUserId: 2, phoneNumber: '9876543210', addressLine: 'Plot 1', village: 'Rampur', district: 'Indore', bankAccountLastFour: '1234' });
  });

  it('should fetch farms and add a crop', () => {
    service.getMyFarms().subscribe(farms => {
      expect(farms.length).toBe(1);
      expect(farms[0].farmName).toBe('Green Acres');
    });
    const farmsReq = httpMock.expectOne('http://localhost:8080/api/farms/my-farms');
    expect(farmsReq.request.method).toBe('GET');
    farmsReq.flush([{ farmId: 1, farmName: 'Green Acres', areaInAcres: 12, soilType: 'LOAMY', latitude: 22.7, longitude: 75.8 }]);

    service.addCrop(1, { cropType: 'WHEAT', cropName: 'Sharbati' }).subscribe(crop => {
      expect(crop.cropType).toBe('WHEAT');
    });
    const cropReq = httpMock.expectOne('http://localhost:8080/api/farms/1/crops');
    expect(cropReq.request.method).toBe('POST');
    cropReq.flush({ cropId: 5, farmId: 1, cropType: 'WHEAT', variety: 'Sharbati', sowingDate: '2026-10-01', expectedHarvestDate: '2027-02-01' });
  });
});
