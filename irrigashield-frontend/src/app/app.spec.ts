import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideStore, Store } from '@ngrx/store';
import { App } from './app';
import { loginReducer } from './features/auth/state/login.reducer';
import { userReducer } from './features/user/state/user.reducer';
import { loadUser } from './features/user/state/user.actions';
import { vi } from 'vitest';

describe('App', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideStore({ login: loginReducer, user: userReducer })
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should dispatch loadUser on init when token exists in localStorage', () => {
    localStorage.setItem('token', 'mock-jwt-token');
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    expect(dispatchSpy).toHaveBeenCalledWith(loadUser());
    localStorage.clear();
  });
});
