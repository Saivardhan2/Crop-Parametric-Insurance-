import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRoles: string[] = route.data['roles'] || [];
  if (expectedRoles.length > 0 && (!role || !expectedRoles.includes(role))) {
    router.navigate(['/']);
    return false;
  }

  return true;
};
