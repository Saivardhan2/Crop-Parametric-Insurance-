import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngrx/store';
import { MyPoliciesComponent } from './my-policies-component';
import { policiesReducer } from '../../state/policies.reducer';
import { vi } from 'vitest';

describe('MyPoliciesComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyPoliciesComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ policies: policiesReducer })
      ]
    }).compileComponents();
  });

  it('should create component and dispatch loadMyPolicies on init', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(MyPoliciesComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(dispatchSpy).toHaveBeenCalled();
  });
});
