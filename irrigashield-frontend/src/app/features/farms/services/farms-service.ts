import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { FarmerProfileDTO } from '../models/FarmerProfileDTO';
import { FarmDTO } from '../models/FarmDTO';
import { CropDTO } from '../models/CropDTO';
import { IrrigationDTO } from '../models/IrrigationDTO';

@Injectable({
  providedIn: 'root'
})
export class FarmsService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  private localDateStr(offsetDays = 0): string {
    const d = new Date(Date.now() + offsetDays * 86400000);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/farmers/profile/me`).pipe(
      map(res => ({
        profileId: res.farmerId,
        farmerId: res.farmerId,
        userId: res.authUserId,
        fullName: res.fullName,
        phone: res.phoneNumber,
        phoneNumber: res.phoneNumber,
        addressLine: res.addressLine,
        village: res.village,
        district: res.district,
        state: res.state,
        postalCode: res.postalCode,
        bankAccountLastFour: res.bankAccountLastFour || '1234',
        address: `${res.addressLine}, ${res.village}, ${res.district}`,
        experienceYears: 5,
        bankAccountNumber: `XXXX-XXXX-${res.bankAccountLastFour || '1234'}`,
        bankIfscCode: 'SBIN0001234'
      })),
      catchError(() => of(null as any))
    );
  }

  saveProfile(profile: any, isUpdate = false): Observable<any> {
    const rawUser = localStorage.getItem('currentUser');
    const userObj = rawUser ? JSON.parse(rawUser) : {};
    const rawName = String(profile?.fullName || userObj?.name || 'Farmer User').trim();
    const fullName = rawName.length >= 2 ? rawName : 'Farmer User';

    const digitsOnly = String(profile?.phoneNumber || profile?.phone || '9876543210').replace(/[^0-9]/g, '');
    const last10 = digitsOnly.slice(-10);
    const validPhone = /^[6-9][0-9]{9}$/.test(last10) ? last10 : '9876543210';

    const bankDigits = String(profile?.bankAccountLastFour || profile?.bankAccountNumber || '1234').replace(/[^0-9]/g, '');
    const lastFour = bankDigits.length >= 4 ? bankDigits.slice(-4) : '1234';

    const rawPin = String(profile?.postalCode || '452001').replace(/[^0-9]/g, '');
    const validPin = /^[1-9][0-9]{5}$/.test(rawPin) ? rawPin : '452001';

    const payload = {
      fullName,
      phoneNumber: validPhone,
      addressLine: String(profile?.addressLine || profile?.address || 'Plot 42 Farm Road').trim() || 'Plot 42 Farm Road',
      village: String(profile?.village || 'Rampur').trim() || 'Rampur',
      district: String(profile?.district || 'Indore').trim() || 'Indore',
      state: String(profile?.state || 'Madhya Pradesh').trim() || 'Madhya Pradesh',
      postalCode: validPin,
      bankAccountLastFour: lastFour
    };

    const req$ = isUpdate
      ? this.http.put<any>(`${this.baseUrl}/farmers/profile/me`, payload).pipe(
          catchError((err) => err.status === 404 ? this.http.post<any>(`${this.baseUrl}/farmers/profile`, payload) : (() => { throw err; })())
        )
      : this.http.post<any>(`${this.baseUrl}/farmers/profile`, payload).pipe(
          catchError((err) => {
            if (err.status === 409) {
              return this.http.put<any>(`${this.baseUrl}/farmers/profile/me`, payload);
            }
            throw err;
          })
        );

    return req$.pipe(
      map(res => ({
        profileId: res.farmerId,
        farmerId: res.farmerId,
        userId: res.authUserId,
        fullName: res.fullName,
        phone: res.phoneNumber,
        phoneNumber: res.phoneNumber,
        addressLine: res.addressLine,
        village: res.village,
        district: res.district,
        state: res.state,
        postalCode: res.postalCode,
        bankAccountLastFour: res.bankAccountLastFour || lastFour,
        address: `${res.addressLine}, ${res.village}, ${res.district}`,
        experienceYears: profile?.experienceYears || 5,
        bankAccountNumber: `XXXX-XXXX-${res.bankAccountLastFour || lastFour}`,
        bankIfscCode: profile?.bankIfscCode || 'SBIN0001234'
      }))
    );
  }

  updateProfile(profile: any): Observable<FarmerProfileDTO> {
    return this.saveProfile(profile, true);
  }

  getMyFarms(): Observable<FarmDTO[]> {
    return this.http.get<any[]>(`${this.baseUrl}/farms/my-farms`).pipe(
      map(list => (list || []).map(res => ({
        farmId: res.farmId,
        farmName: res.farmName,
        totalAreaHectares: res.areaInAcres,
        soilType: res.soilType,
        waterSource: 'BOREWELL',
        latitude: res.latitude,
        longitude: res.longitude
      }))),
      catchError(() => of([] as FarmDTO[]))
    );
  }

  getFarm(farmId: number): Observable<FarmDTO> {
    return this.http.get<any>(`${this.baseUrl}/farms/${farmId}`).pipe(
      map(res => ({
        farmId: res.farmId,
        farmName: res.farmName,
        totalAreaHectares: res.areaInAcres,
        soilType: res.soilType,
        waterSource: 'BOREWELL',
        latitude: res.latitude,
        longitude: res.longitude
      }))
    );
  }

  createFarm(farm: any): Observable<FarmDTO> {
    const rawName = String(farm?.farmName || 'Green Valley Farm').trim();
    const safeName = rawName.length >= 2 ? rawName : `${rawName} Farm`;
    const rawArea = Number(farm?.totalAreaHectares || farm?.areaInAcres || 5.0);
    const safeArea = Math.max(0.1, Math.round((isNaN(rawArea) ? 5.0 : rawArea) * 100) / 100);
    const rawLat = Number(farm?.latitude ?? 22.7196);
    const safeLat = Math.min(90, Math.max(-90, isNaN(rawLat) ? 22.7196 : rawLat));
    const rawLng = Number(farm?.longitude ?? 75.8577);
    const safeLng = Math.min(180, Math.max(-180, isNaN(rawLng) ? 75.8577 : rawLng));

    const payload = {
      farmName: safeName,
      surveyNumber: farm?.surveyNumber || 'SURVEY-101',
      addressLine: farm?.addressLine || 'Farm Plot Near Canal',
      village: farm?.village || 'Rampur',
      district: farm?.district || 'Indore',
      state: farm?.state || 'Madhya Pradesh',
      postalCode: '452001',
      areaInAcres: safeArea,
      soilType: farm?.soilType || 'CLAY',
      ownershipType: farm?.ownershipType || 'OWNED',
      latitude: safeLat,
      longitude: safeLng
    };

    const postFarm$ = () => this.http.post<any>(`${this.baseUrl}/farms`, payload).pipe(
      map(res => ({
        farmId: res.farmId,
        farmName: res.farmName,
        totalAreaHectares: res.areaInAcres,
        soilType: res.soilType,
        waterSource: farm?.waterSource || 'BOREWELL',
        latitude: res.latitude,
        longitude: res.longitude
      }))
    );

    return postFarm$().pipe(
      catchError((err) => {
        if (err.status === 404) {
          return this.saveProfile({}).pipe(
            switchMap(() => postFarm$())
          );
        }
        throw err;
      })
    );
  }

  getCrops(farmId: number): Observable<CropDTO[]> {
    return this.http.get<any[]>(`${this.baseUrl}/farms/${farmId}/crops`).pipe(
      map(list => (list || []).map(res => ({
        cropId: res.cropId,
        farmId: res.farmId,
        cropName: res.variety || res.cropType,
        cropType: res.cropType,
        plantedAreaHectares: 2.5,
        sowingDate: res.sowingDate,
        expectedHarvestDate: res.expectedHarvestDate
      })))
    );
  }

  addCrop(farmId: number, crop: any): Observable<CropDTO> {
    const today = this.localDateStr(0);
    const defaultHarvest = this.localDateStr(90);

    const reqSowing = crop?.sowingDate && crop.sowingDate <= today ? crop.sowingDate : today;
    const reqHarvest = crop?.expectedHarvestDate && crop.expectedHarvestDate > today ? crop.expectedHarvestDate : defaultHarvest;

    const payload = {
      cropType: crop?.cropType || 'WHEAT',
      variety: (crop?.cropName || 'Sharbati Wheat').trim() || 'Sharbati Wheat',
      sowingDate: reqSowing,
      expectedHarvestDate: reqHarvest,
      growthStage: crop?.growthStage || 'VEGETATIVE',
      irrigationRequirement: crop?.irrigationRequirement || 'MEDIUM'
    };

    return this.http.post<any>(`${this.baseUrl}/farms/${farmId}/crops`, payload).pipe(
      map(res => ({
        cropId: res.cropId,
        farmId: res.farmId,
        cropName: res.variety || res.cropType,
        cropType: res.cropType,
        plantedAreaHectares: crop?.plantedAreaHectares || 2.5,
        sowingDate: res.sowingDate,
        expectedHarvestDate: res.expectedHarvestDate
      }))
    );
  }

  getIrrigation(farmId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/farms/${farmId}/irrigation`).pipe(
      map(res => ({
        irrigationId: res.irrigationProfileId || 1,
        irrigationProfileId: res.irrigationProfileId || 1,
        farmId: res.farmId || farmId,
        waterSource: res.waterSource || 'BOREWELL',
        irrigationMethod: res.irrigationMethod || 'DRIP',
        irrigationType: res.irrigationMethod || 'DRIP',
        equipmentCondition: res.equipmentCondition || 'GOOD',
        backupWaterSource: res.backupWaterSource || 'NONE',
        historicalFailureCount: res.historicalFailureCount ?? 0,
        averageWaterAvailabilityPercentage: res.averageWaterAvailabilityPercentage ?? 85.0,
        efficiencyPercentage: res.averageWaterAvailabilityPercentage ?? 85.0,
        seasonalRisk: res.seasonalRisk || 'LOW',
        maintenanceFrequency: res.maintenanceFrequency || 'MONTHLY',
        lastMaintenanceDate: res.lastMaintenanceDate || this.localDateStr(0),
        capacityLitersPerHour: 5000,
        automationLevel: 'AUTOMATED'
      })),
      catchError(() => of(null as any))
    );
  }

  saveIrrigation(farmId: number, irrigation: any, isUpdate = false): Observable<any> {
    const validMethods = ['DRIP', 'SPRINKLER', 'FLOOD', 'FURROW', 'MANUAL'];
    const rawMethod = String(irrigation?.irrigationMethod || irrigation?.irrigationType || 'DRIP').toUpperCase();
    const safeMethod = validMethods.includes(rawMethod) ? rawMethod : 'DRIP';

    const validSources = ['BOREWELL', 'CANAL', 'WELL', 'RIVER', 'RESERVOIR', 'TANKER', 'RAINWATER'];
    const rawSource = String(irrigation?.waterSource || 'BOREWELL').toUpperCase();
    const safeSource = validSources.includes(rawSource) ? rawSource : 'BOREWELL';

    const validBackup = [...validSources, 'NONE'];
    const rawBackup = String(irrigation?.backupWaterSource || 'NONE').toUpperCase();
    const safeBackup = validBackup.includes(rawBackup) ? rawBackup : 'NONE';

    const validCond = ['GOOD', 'FAIR', 'POOR'];
    const rawCond = String(irrigation?.equipmentCondition || 'GOOD').toUpperCase();
    const safeCond = validCond.includes(rawCond) ? rawCond : 'GOOD';

    const validRisk = ['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'];
    const rawRisk = String(irrigation?.seasonalRisk || 'LOW').toUpperCase();
    const safeRisk = validRisk.includes(rawRisk) ? rawRisk : 'LOW';

    const validMaint = ['MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY', 'NONE'];
    const rawMaint = String(irrigation?.maintenanceFrequency || 'MONTHLY').toUpperCase();
    const safeMaint = validMaint.includes(rawMaint) ? rawMaint : 'MONTHLY';

    const rawEff = Number(irrigation?.averageWaterAvailabilityPercentage ?? irrigation?.efficiencyPercentage ?? 85.0);
    const safeEff = Math.min(100, Math.max(0, isNaN(rawEff) ? 85.0 : rawEff));

    const rawFailures = Number(irrigation?.historicalFailureCount ?? 0);
    const safeFailures = Math.max(0, isNaN(rawFailures) ? 0 : Math.floor(rawFailures));

    const payload = {
      waterSource: safeSource,
      irrigationMethod: safeMethod,
      equipmentCondition: safeCond,
      backupWaterSource: safeBackup,
      historicalFailureCount: safeFailures,
      averageWaterAvailabilityPercentage: safeEff,
      seasonalRisk: safeRisk,
      maintenanceFrequency: safeMaint,
      lastMaintenanceDate: this.localDateStr(0)
    };

    const req$ = isUpdate
      ? this.http.put<any>(`${this.baseUrl}/farms/${farmId}/irrigation`, payload).pipe(
          catchError((err) => err.status === 404 ? this.http.post<any>(`${this.baseUrl}/farms/${farmId}/irrigation`, payload) : (() => { throw err; })())
        )
      : this.http.post<any>(`${this.baseUrl}/farms/${farmId}/irrigation`, payload).pipe(
          catchError((err) => {
            if (err.status === 409) {
              return this.http.put<any>(`${this.baseUrl}/farms/${farmId}/irrigation`, payload);
            }
            throw err;
          })
        );

    return req$.pipe(
      map(res => ({
        irrigationId: res.irrigationProfileId || 1,
        irrigationProfileId: res.irrigationProfileId || 1,
        farmId: res.farmId || farmId,
        waterSource: res.waterSource || safeSource,
        irrigationMethod: res.irrigationMethod || safeMethod,
        irrigationType: res.irrigationMethod || safeMethod,
        equipmentCondition: res.equipmentCondition || safeCond,
        backupWaterSource: res.backupWaterSource || safeBackup,
        historicalFailureCount: res.historicalFailureCount ?? safeFailures,
        averageWaterAvailabilityPercentage: res.averageWaterAvailabilityPercentage ?? safeEff,
        efficiencyPercentage: res.averageWaterAvailabilityPercentage ?? safeEff,
        seasonalRisk: res.seasonalRisk || safeRisk,
        maintenanceFrequency: res.maintenanceFrequency || safeMaint,
        lastMaintenanceDate: res.lastMaintenanceDate || payload.lastMaintenanceDate,
        capacityLitersPerHour: irrigation?.capacityLitersPerHour || 5000,
        automationLevel: irrigation?.automationLevel || 'AUTOMATED'
      }))
    );
  }
}
