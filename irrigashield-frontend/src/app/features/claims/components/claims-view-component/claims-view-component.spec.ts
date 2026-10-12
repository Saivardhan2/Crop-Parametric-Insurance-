import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideStore, Store } from '@ngrx/store';
import { ClaimsViewComponent } from './claims-view-component';
import { claimsReducer } from '../../state/claims.reducer';
import { vi } from 'vitest';

describe('ClaimsViewComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClaimsViewComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideStore({ claims: claimsReducer })
      ]
    }).compileComponents();
  });

  it('should create component and dispatch loadMyClaims on init', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(ClaimsViewComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect(dispatchSpy).toHaveBeenCalled();
  });
});
