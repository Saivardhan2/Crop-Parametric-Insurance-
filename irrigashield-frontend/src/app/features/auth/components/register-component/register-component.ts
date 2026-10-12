import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { signup } from '../../state/login.action';
import { signupStatusSelector } from '../../state/login.selector';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AsyncPipe],
  templateUrl: './register-component.html',
  styleUrl: './register-component.css'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private store = inject(Store);

  signupStatus$ = this.store.select(signupStatusSelector);

  registerForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/)
    ]]
  });

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.store.dispatch(signup({ userReq: this.registerForm.value }));
    } else {
      this.registerForm.markAllAsTouched();
    }
  }
}
