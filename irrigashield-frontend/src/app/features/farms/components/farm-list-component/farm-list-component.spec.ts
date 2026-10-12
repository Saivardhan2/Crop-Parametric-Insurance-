import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngrx/store';
import { FarmListComponent } from './farm-list-component';
import { farmsReducer } from '../../state/farms.reducer';
import { vi } from 'vitest';

describe('FarmListComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FarmListComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ farms: farmsReducer })
      ]
    }).compileComponents();
  });

  it('should create the component and validate farmForm', () => {
    const fixture = TestBed.createComponent(FarmListComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.farmForm.valid).toBe(false);
  });

  it('should dispatch createFarm when valid farmForm is submitted', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(FarmListComponent);
    const component = fixture.componentInstance;

    component.openModal();
    component.farmForm.setValue({
      farmName: 'Sunrise Farm',
      latitude: 23.0225,
      longitude: 72.5714,
      totalAreaHectares: 15,
      soilType: 'SANDY_LOAM',
      waterSource: 'CANAL'
    });
    component.onSubmit();

    expect(dispatchSpy).toHaveBeenCalled();
    expect(component.showAddModal).toBe(false);
  });
});
