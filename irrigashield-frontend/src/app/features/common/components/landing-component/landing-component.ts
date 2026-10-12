import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { isLoggedInSelector, userRoleSelector } from '../../../auth/state/login.selector';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, AsyncPipe],
  templateUrl: './landing-component.html',
  styleUrl: './landing-component.css'
})
export class LandingComponent {
  private store = inject(Store);
  isLoggedIn$ = this.store.select(isLoggedInSelector);
  userRole$ = this.store.select(userRoleSelector);
}
