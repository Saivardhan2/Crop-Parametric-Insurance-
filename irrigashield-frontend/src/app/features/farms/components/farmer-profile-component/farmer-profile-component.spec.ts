import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideStore, Store } from '@ngrx/store';
import { FarmerProfileComponent } from './farmer-profile-component';
import { farmsReducer } from '../../state/farms.reducer';
import { vi } from 'vitest';

describe('FarmerProfileComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FarmerProfileComponent],
      providers: [
        provideRouter([]),
        provideStore({ farms: farmsReducer })
      ]
    }).compileComponents();
  });

  it('should create component with invalid default profileForm', () => {
    const fixture = TestBed.createComponent(FarmerProfileComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.profileForm.valid).toBe(false);
  });

  it('should dispatch saveProfile when profileForm is valid', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(FarmerProfileComponent);
    const component = fixture.componentInstance;

    component.profileForm.patchValue({
      phone: '9876543210',
      address: 'Plot 12, Green Road',
      experienceYears: 5,
      bankAccountNumber: '123456789012',
      bankIfscCode: 'SBIN0001234'
    });
    component.onSubmit();

    expect(component.profileForm.valid).toBe(true);
    expect(dispatchSpy).toHaveBeenCalled();
  });
});
