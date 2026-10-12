import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideStore, Store } from '@ngrx/store';
import { LoginComponent } from './login-component';
import { loginReducer } from '../../state/login.reducer';
import { vi } from 'vitest';

describe('LoginComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), provideStore({ login: loginReducer })]
    }).compileComponents();
  });

  it('should create the component with an invalid initial form', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.loginForm.valid).toBe(false);
  });

  it('should dispatch login action when form is valid and submitted', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;

    component.loginForm.setValue({ email: 'ramesh@farm.in', password: 'Password@123' });
    component.onSubmit();

    expect(component.loginForm.valid).toBe(true);
    expect(dispatchSpy).toHaveBeenCalled();
  });
});
