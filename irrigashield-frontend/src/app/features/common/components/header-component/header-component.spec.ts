import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideStore, Store } from '@ngrx/store';
import { HeaderComponent } from './header-component';
import { loginReducer } from '../../../auth/state/login.reducer';
import { userReducer } from '../../../user/state/user.reducer';
import { vi } from 'vitest';

describe('HeaderComponent', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideRouter([]),
        provideStore({ login: loginReducer, user: userReducer })
      ]
    }).compileComponents();
  });

  it('should create the header component', () => {
    const fixture = TestBed.createComponent(HeaderComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should dispatch logout action on onLogout', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(HeaderComponent);

    fixture.componentInstance.onLogout();

    expect(dispatchSpy).toHaveBeenCalledTimes(1);
  });
});
