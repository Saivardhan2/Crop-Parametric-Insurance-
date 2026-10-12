import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideStore } from '@ngrx/store';
import { LandingComponent } from './landing-component';
import { loginReducer } from '../../../auth/state/login.reducer';
import { userReducer } from '../../../user/state/user.reducer';

describe('LandingComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LandingComponent],
      providers: [provideRouter([]), provideStore({ login: loginReducer, user: userReducer })]
    }).compileComponents();
  });

  it('should create the landing component and render hero content', () => {
    const fixture = TestBed.createComponent(LandingComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('IrrigaShield');
  });
});
