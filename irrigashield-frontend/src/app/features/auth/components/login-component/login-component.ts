import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { login } from '../../state/login.action';
import { loginStatusSelector } from '../../state/login.selector';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, AsyncPipe],
  templateUrl: './login-component.html',
  styleUrl: './login-component.css'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private store = inject(Store);

  loginStatus$ = this.store.select(loginStatusSelector);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.store.dispatch(login({ jwtReq: this.loginForm.value }));
    } else {
      this.loginForm.markAllAsTouched();
    }
  }

  prefill(email: string, pass: string): void {
    this.loginForm.patchValue({ email, password: pass });
  }
}
