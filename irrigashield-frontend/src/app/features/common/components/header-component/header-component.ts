import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Store } from '@ngrx/store';
import { AsyncPipe } from '@angular/common';
import { isLoggedInSelector, userRoleSelector } from '../../../auth/state/login.selector';
import { currentUserSelector } from '../../../user/state/user.selector';
import { logout } from '../../../auth/state/login.action';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe],
  templateUrl: './header-component.html',
  styleUrl: './header-component.css'
})
export class HeaderComponent {
  private store = inject(Store);
  private router = inject(Router);

  isLoggedIn$ = this.store.select(isLoggedInSelector);
  userRole$ = this.store.select(userRoleSelector);
  currentUser$ = this.store.select(currentUserSelector);

  onLogout(): void {
    this.store.dispatch(logout());
  }
}
