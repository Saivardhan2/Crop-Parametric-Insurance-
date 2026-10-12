import { Routes } from '@angular/router';
import { authGuard } from './guards/authGuard';
import { roleGuard } from './guards/roleGuard';

import { LandingComponent } from './features/common/components/landing-component/landing-component';
import { LoginComponent } from './features/auth/components/login-component/login-component';
import { RegisterComponent } from './features/auth/components/register-component/register-component';
import { FarmerProfileComponent } from './features/farms/components/farmer-profile-component/farmer-profile-component';
import { FarmListComponent } from './features/farms/components/farm-list-component/farm-list-component';
import { FarmDetailComponent } from './features/farms/components/farm-detail-component/farm-detail-component';
import { PlansCatalogComponent } from './features/policies/components/plans-catalog-component/plans-catalog-component';
import { ApplicationsComponent } from './features/policies/components/applications-component/applications-component';
import { MyPoliciesComponent } from './features/policies/components/my-policies-component/my-policies-component';
import { UnderwritingComponent } from './features/policies/components/underwriting-component/underwriting-component';
import { ClaimsViewComponent } from './features/claims/components/claims-view-component/claims-view-component';
import { DataProviderComponent } from './features/claims/components/data-provider-component/data-provider-component';
import { AdminUsersComponent } from './features/admin/components/admin-users-component/admin-users-component';
import { AdminPlansComponent } from './features/admin/components/admin-plans-component/admin-plans-component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Farmer routes
  {
    path: 'farmer/profile',
    component: FarmerProfileComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },
  {
    path: 'farmer/farms',
    component: FarmListComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },
  {
    path: 'farmer/farms/:farmId',
    component: FarmDetailComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },
  {
    path: 'farmer/plans',
    component: PlansCatalogComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },
  {
    path: 'farmer/applications',
    component: ApplicationsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },
  {
    path: 'farmer/policies',
    component: MyPoliciesComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },
  {
    path: 'farmer/claims',
    component: ClaimsViewComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] }
  },

  // Underwriter routes
  {
    path: 'underwriting',
    component: UnderwritingComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['UNDERWRITER', 'ADMIN'] }
  },

  // Data Provider routes
  {
    path: 'data-provider',
    component: DataProviderComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['DATA_PROVIDER', 'ADMIN'] }
  },

  // Claims Officer routes
  {
    path: 'claims-officer/claims',
    component: ClaimsViewComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['CLAIMS_OFFICER', 'ADMIN'] }
  },

  // Admin routes
  {
    path: 'admin/users',
    component: AdminUsersComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'admin/plans',
    component: AdminPlansComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },

  { path: '**', redirectTo: '' }
];
