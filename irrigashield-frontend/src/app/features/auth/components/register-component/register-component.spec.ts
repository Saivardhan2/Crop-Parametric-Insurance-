import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideStore, Store } from '@ngrx/store';
import { RegisterComponent } from './register-component';
import { loginReducer } from '../../state/login.reducer';
import { vi } from 'vitest';

describe('RegisterComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [provideRouter([]), provideStore({ login: loginReducer })]
    }).compileComponents();
  });

  it('should create the component and validate required registration fields', () => {
    const fixture = TestBed.createComponent(RegisterComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component.registerForm.valid).toBe(false);
  });

  it('should dispatch register action when form is valid', () => {
    const store = TestBed.inject(Store);
    const dispatchSpy = vi.spyOn(store, 'dispatch');
    const fixture = TestBed.createComponent(RegisterComponent);
    const component = fixture.componentInstance;

    component.registerForm.setValue({
      name: 'Ramesh Kumar',
      email: 'ramesh@farm.in',
      password: 'Password@123'
    });
    component.onSubmit();

    expect(component.registerForm.valid).toBe(true);
    expect(dispatchSpy).toHaveBeenCalled();
  });
});
