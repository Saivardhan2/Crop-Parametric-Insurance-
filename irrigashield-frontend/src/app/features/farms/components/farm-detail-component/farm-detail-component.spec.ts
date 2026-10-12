import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngrx/store';
import { FarmDetailComponent } from './farm-detail-component';
import { farmsReducer } from '../../state/farms.reducer';
import { vi } from 'vitest';

describe('FarmDetailComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FarmDetailComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ farms: farmsReducer })
      ]
    }).compileComponents();
  });

  it('should create component and initialize crop and irrigation forms', () => {
    const fixture = TestBed.createComponent(FarmDetailComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.cropForm.valid).toBe(false);
    expect(component.irrigationForm.valid).toBe(true);
  });

  it('should dispatch addCrop and saveIrrigation actions on submit', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(FarmDetailComponent);
    const component = fixture.componentInstance;
    component.farmId = 1;
    component.cropForm.patchValue({ cropName: 'Sharbati Wheat' });

    component.onAddCrop();
    component.onSaveIrrigation();

    expect(dispatchSpy).toHaveBeenCalledTimes(2);
  });
});
