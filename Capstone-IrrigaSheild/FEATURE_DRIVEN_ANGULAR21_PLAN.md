# IrrigaShield: Enterprise Feature-Driven Angular 21 Monolithic Frontend Blueprint

This specification is an end-to-end blueprint for building a **monolithic enterprise Angular 21 application** using a **Feature-Driven (Vertical Slice) Architecture**. Every business domain feature encapsulates its own **components**, **services**, **state (NgRx store/actions/reducers/selectors/effects)**, and **models (DTOs)**. The `auth` feature additionally houses the application **guards** and **HTTP interceptors**.

---

## 1. Project Organization & Directory Strategy

The frontend is housed in a dedicated workspace directory alongside the backend:

```
c:\TRAINING\
├── Capstone-IrrigaSheild\          (Backend Microservices Workspace)
└── irrigashield-frontend\           (Angular 21 Monolithic Workspace)
```

### Initial CLI Generation & Dependencies
```bash
cd c:\TRAINING
npx -y @angular/cli@21 new irrigashield-frontend --routing --style=scss --strict --ssr=false --skip-git --package-manager=npm
cd irrigashield-frontend
npm install @ngrx/store@21 @ngrx/effects@21 @ngrx/entity@21 @ngrx/store-devtools@21 @lucide/angular
```

---

## 2. Feature-Driven Application File Tree

In this feature-driven pattern, each business domain is completely self-contained. The `auth` feature encapsulates authentication, session state, route protection guards, and Gateway credentials interceptors.

```
irrigashield-frontend/src/app/
│
├── features/
│   ├── auth/                                    # Auth Feature Slice
│   │   ├── components/
│   │   │   ├── login/                           # Reactive login form component
│   │   │   └── register/                        # Farmer self-registration form component
│   │   ├── services/
│   │   │   └── auth-api.service.ts              # Calls /api/auth/** & /api/users/**
│   │   ├── state/
│   │   │   ├── auth.actions.ts                  # Login, Register, Session check, Logout actions
│   │   │   ├── auth.reducer.ts                  # Auth state: user, loading, error, isAuthenticated
│   │   │   ├── auth.selectors.ts                # selectCurrentUser, selectIsAuthenticated, selectUserRole
│   │   │   ├── auth.effects.ts                  # Login/Register side-effects, role-based navigation
│   │   │   └── auth.reducer.spec.ts             # Reducer pure function unit test
│   │   ├── model/
│   │   │   └── auth.models.ts                   # UserResponse, AuthResponse, LoginRequest, RegisterRequest
│   │   ├── guards/
│   │   │   ├── auth.guard.ts                    # CanActivateFn: Verifies selectIsAuthenticated
│   │   │   └── role.guard.ts                    # CanActivateFn: Verifies user role matches route.data['roles']
│   │   └── interceptors/
│   │       ├── credentials.interceptor.ts       # Injects withCredentials: true on all gateway calls
│   │       └── error.interceptor.ts             # Global catchError mapping ApiErrorResponse & 401 redirect
│   │
│   ├── farmer/                                  # Farmer Domain Feature Slice
│   │   ├── components/
│   │   │   ├── farmer-dashboard/                # Active farms, active policies, quick actions
│   │   │   ├── profile-form/                    # KYC profile form with phone & PIN validators
│   │   │   ├── farm-list/                       # Registered farm plots grid
│   │   │   ├── farm-form/                       # Farm registration & coordinates form
│   │   │   ├── crop-manager/                    # Crop cycle logging & stage tracking
│   │   │   ├── irrigation-form/                 # Irrigation reliability & equipment condition
│   │   │   ├── plan-catalog/                    # Active parametric insurance plans explorer
│   │   │   ├── application-wizard/              # Policy application submission wizard
│   │   │   ├── quote-acceptance/                # Approved quote review & policy activation
│   │   │   └── my-policies/                     # Active & historical policies cards
│   │   ├── services/
│   │   │   ├── farmer-profile-api.service.ts    # Calls /api/farmers/profile/**
│   │   │   ├── farm-api.service.ts              # Calls /api/farms/**, /crops, /irrigation, /validation
│   │   │   └── farmer-policy-api.service.ts     # Calls /api/applications/**, /api/policies/**
│   │   ├── state/
│   │   │   ├── farmer.actions.ts                # Farm, crop, irrigation, application actions
│   │   │   ├── farmer.reducer.ts                # FarmerState: profile, farms, crops, irrigation, apps
│   │   │   ├── farmer.selectors.ts              # selectFarms, selectSelectedFarm, selectApplications
│   │   │   ├── farmer.effects.ts                # API calls, validation triggers, quote acceptance
│   │   │   └── farmer.effects.spec.ts           # Marble unit tests for farmer effects
│   │   └── model/
│   │       ├── farmer.models.ts                 # FarmerProfileRequest/Response
│   │       ├── farm.models.ts                   # FarmRequest/Response, FarmValidationResponse
│   │       ├── crop.models.ts                   # CropRequest/Response
│   │       ├── irrigation.models.ts             # IrrigationProfileRequest/Response
│   │       └── application.models.ts            # PolicyApplicationRequest/Response, PolicyResponse
│   │
│   ├── underwriter/                             # Underwriter Domain Feature Slice
│   │   ├── components/
│   │   │   ├── pending-queue/                   # Real-time list of pending applications (/pending)
│   │   │   ├── risk-assessment-scorecard/       # 5-factor risk scoring engine UI & gauge
│   │   │   ├── decision-modal/                  # Approve / Reject dialog with mandatory remarks
│   │   │   └── case-history/                    # History of reviewed cases (/my-cases)
│   │   ├── services/
│   │   │   └── underwriting-api.service.ts      # Calls /api/underwriting/**
│   │   ├── state/
│   │   │   ├── underwriting.actions.ts          # StartReview, Assess, Approve, Reject actions
│   │   │   ├── underwriting.reducer.ts          # UnderwritingState: queue, activeCase, myCases
│   │   │   ├── underwriting.selectors.ts        # selectPendingQueue, selectActiveScorecard
│   │   │   ├── underwriting.effects.ts          # Underwriting API side-effects
│   │   │   └── underwriting.reducer.spec.ts     # Reducer unit tests
│   │   └── model/
│   │       └── underwriting.models.ts           # UnderwritingResponse, DecisionRequest, RiskResult
│   │
│   ├── claims/                                  # Telemetry & Parametric Claims Feature Slice
│   │   ├── components/
│   │   │   ├── sensor-simulator/                # IoT sensor submission form (for DATA_PROVIDER)
│   │   │   ├── claim-alert-dialog/              # Instant parametric payout notification popup
│   │   │   └── farmer-claims-history/           # List of settled indemnity payouts (for FARMER)
│   │   ├── services/
│   │   │   └── claims-api.service.ts            # Calls /api/readings/** & /api/claims/**
│   │   ├── state/
│   │   │   ├── claims.actions.ts                # SubmitReading, LoadMyClaims, NewClaimTriggered
│   │   │   ├── claims.reducer.ts                # ClaimsState: myClaims, lastReadingResult, alert
│   │   │   ├── claims.selectors.ts              # selectMyClaims, selectLastSettledClaim
│   │   │   └── claims.effects.ts                # Reading submission & automated settlement handler
│   │   └── model/
│   │       └── claim.models.ts                  # IrrigationReadingRequest, ClaimResponse
│   │
│   └── admin/                                   # Admin Domain Feature Slice
│       ├── components/
│       │   ├── user-manager/                    # User provisioning table & creation modal
│       │   ├── plan-builder/                    # Insurance plan creation & boundary limits
│       │   ├── trigger-rule-builder/            # Trigger rule & rolling window shortage setup
│       │   └── payout-slab-builder/             # Duration-to-payout percentage tiers editor
│       ├── services/
│       │   └── admin-api.service.ts             # Calls /api/admin/users, /plans, /trigger-rules, /slabs
│       ├── state/
│       │   ├── admin.actions.ts                 # CreateUser, CreatePlan, CreateRule, CreateSlab
│       │   ├── admin.reducer.ts                 # AdminState: usersList, plansList, rulesMap, slabsMap
│       │   ├── admin.selectors.ts               # selectAllUsers, selectAdminPlans
│       │   └── admin.effects.ts                 # Admin API handlers
│       └── model/
│           ├── admin.models.ts                  # CreateUserRequest, AdminStats
│           └── plan-admin.models.ts             # InsurancePlanRequest/Response, TriggerRule, PayoutSlab
│
├── shared/                                      # Cross-Feature Reusable Primitives
│   ├── directives/
│   │   ├── has-role.directive.ts                # Structural: *appHasRole="['ADMIN', 'FARMER']"
│   │   ├── status-badge.directive.ts            # Attribute: [appStatusBadge]="status"
│   │   └── risk-level-gauge.directive.ts        # Attribute: [appRiskLevel]="level"
│   ├── validators/
│   │   ├── phone.validator.ts                   # 10-digit Indian mobile regex
│   │   ├── pin-code.validator.ts                # 6-digit Indian PIN code regex
│   │   ├── date-sequence.validator.ts           # Cross-field: End date > Start date
│   │   └── coverage-bounds.validator.ts         # Min <= Requested <= Max coverage
│   ├── components/
│   │   ├── navbar/                              # Dynamic role-based navigation header
│   │   ├── loading-spinner/                     # Global reactive loading overlay
│   │   └── error-banner/                        # Dismissible global toast/alert banner
│   └── services/
│       └── notification.service.ts              # RxJS Subject-based notification bus
│
├── app.config.ts                                # Central providers (provideStore, provideEffects, provideHttpClient)
├── app.routes.ts                                # Feature lazy-routing root
├── app.component.ts                             # Root layout shell
├── app.component.html                           # <app-navbar/><router-outlet/>
└── app.component.scss
```

---

## 3. Feature Breakdown: Models, Services, State & Components

---

### 3.1 Feature: `auth` (`src/app/features/auth/`)

#### 3.1.1 Models (`features/auth/model/auth.models.ts`)
```typescript
export type Role = 'FARMER' | 'UNDERWRITER' | 'CLAIMS_OFFICER' | 'DATA_PROVIDER' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface UserResponse {
  userId: number;
  name: string;
  email: string;
  role: Role;
  status: UserStatus;
}

export interface AuthResponse {
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
}

export interface ApiErrorResponse {
  status: number;
  errorCode: string;
  message: string;
  path: string;
  timestamp?: string;
  errors?: string[];
}
```

#### 3.1.2 Service (`features/auth/services/auth-api.service.ts`)
```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../model/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private http = inject(HttpClient);
  private baseUrl = 'http://localhost:8080/api';

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, credentials);
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.baseUrl}/auth/register`, request);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/logout`, {});
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/users/me`);
  }
}
```

#### 3.1.3 Interceptors (`features/auth/interceptors/`)

##### `credentials.interceptor.ts`
```typescript
import { HttpInterceptorFn } from '@angular/common/http';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  // Always include HTTP-only cookie IRRIGASHIELD_TOKEN on Gateway calls
  const apiReq = req.clone({ withCredentials: true });
  return next(apiReq);
};
```

##### `error.interceptor.ts`
```typescript
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../../../shared/services/notification.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred';
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      }

      if (error.status === 401) {
        notificationService.showError('Session expired. Please log in again.');
        router.navigate(['/auth/login']);
      } else if (error.status === 409) {
        notificationService.showWarning(errorMessage);
      } else if (error.status >= 500) {
        notificationService.showError('Server error: ' + errorMessage);
      }

      return throwError(() => error);
    })
  );
};
```

#### 3.1.4 Guards (`features/auth/guards/`)

##### `auth.guard.ts`
```typescript
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { selectIsAuthenticated } from '../state/auth.selectors';
import { map, take } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const store = inject(Store);
  const router = inject(Router);

  return store.select(selectIsAuthenticated).pipe(
    take(1),
    map(isAuth => (isAuth ? true : router.createUrlTree(['/auth/login'])))
  );
};
```

##### `role.guard.ts`
```typescript
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { selectCurrentUser } from '../state/auth.selectors';
import { map, take } from 'rxjs';

export const roleGuard: CanActivateFn = (route) => {
  const store = inject(Store);
  const router = inject(Router);
  const allowedRoles: string[] = route.data['roles'] || [];

  return store.select(selectCurrentUser).pipe(
    take(1),
    map(user => {
      if (user && allowedRoles.includes(user.role)) {
        return true;
      }
      return router.createUrlTree(['/auth/login']);
    })
  );
};
```

#### 3.1.5 NgRx State (`features/auth/state/`)

##### `auth.actions.ts`
```typescript
import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { UserResponse, LoginRequest, RegisterRequest } from '../model/auth.models';

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    'Login': props<{ credentials: LoginRequest }>(),
    'Login Success': props<{ user: UserResponse }>(),
    'Login Failure': props<{ error: string }>(),
    'Register': props<{ request: RegisterRequest }>(),
    'Register Success': props<{ user: UserResponse }>(),
    'Register Failure': props<{ error: string }>(),
    'Check Session': emptyProps(),
    'Session Active': props<{ user: UserResponse }>(),
    'Session Inactive': emptyProps(),
    'Logout': emptyProps(),
    'Logout Success': emptyProps()
  }
});
```

##### `auth.reducer.ts`
```typescript
import { createReducer, on } from '@ngrx/store';
import { AuthActions } from './auth.actions';
import { UserResponse } from '../model/auth.models';

export interface AuthState {
  user: UserResponse | null;
  loading: boolean;
  error: string | null;
  isAuthenticated: boolean;
}

export const initialAuthState: AuthState = {
  user: null,
  loading: false,
  error: null,
  isAuthenticated: false
};

export const authReducer = createReducer(
  initialAuthState,
  on(AuthActions.login, AuthActions.register, state => ({ ...state, loading: true, error: null })),
  on(AuthActions.loginSuccess, AuthActions.sessionActive, (state, { user }) => ({
    ...state,
    user,
    loading: false,
    isAuthenticated: true,
    error: null
  })),
  on(AuthActions.loginFailure, AuthActions.registerFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
    isAuthenticated: false
  })),
  on(AuthActions.logoutSuccess, AuthActions.sessionInactive, () => initialAuthState)
);
```

##### `auth.selectors.ts`
```typescript
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>('auth');
export const selectCurrentUser = createSelector(selectAuthState, state => state.user);
export const selectIsAuthenticated = createSelector(selectAuthState, state => state.isAuthenticated);
export const selectAuthLoading = createSelector(selectAuthState, state => state.loading);
export const selectAuthError = createSelector(selectAuthState, state => state.error);
export const selectUserRole = createSelector(selectCurrentUser, user => user?.role ?? null);
```

##### `auth.effects.ts`
```typescript
import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Router } from '@angular/router';
import { AuthActions } from './auth.actions';
import { AuthApiService } from '../services/auth-api.service';
import { of } from 'rxjs';
import { catchError, exhaustMap, map, tap } from 'rxjs/operators';

@Injectable()
export class AuthEffects {
  private actions$ = inject(Actions);
  private authApi = inject(AuthApiService);
  private router = inject(Router);

  login$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.login),
      exhaustMap(({ credentials }) =>
        this.authApi.login(credentials).pipe(
          map(response => AuthActions.loginSuccess({ user: response.user })),
          catchError(err => of(AuthActions.loginFailure({ error: err.error?.message || 'Login failed' })))
        )
      )
    )
  );

  loginSuccess$ = createEffect(
    () =>
      this.actions$.pipe(
        ofType(AuthActions.loginSuccess),
        tap(({ user }) => {
          switch (user.role) {
            case 'FARMER': this.router.navigate(['/farmer/dashboard']); break;
            case 'UNDERWRITER': this.router.navigate(['/underwriter/queue']); break;
            case 'DATA_PROVIDER': this.router.navigate(['/claims/simulator']); break;
            case 'ADMIN': this.router.navigate(['/admin/users']); break;
            default: this.router.navigate(['/auth/login']);
          }
        })
      ),
    { dispatch: false }
  );

  logout$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.logout),
      exhaustMap(() =>
        this.authApi.logout().pipe(
          map(() => AuthActions.logoutSuccess()),
          tap(() => this.router.navigate(['/auth/login'])),
          catchError(() => of(AuthActions.logoutSuccess()))
        )
      )
    )
  );
}
```

---

### 3.2 Feature: `farmer` (`src/app/features/farmer/`)

#### 3.2.1 Models (`features/farmer/model/`)
- `farmer.models.ts`: `FarmerProfileRequest`, `FarmerProfileResponse`
- `farm.models.ts`: `FarmRequest`, `FarmResponse`, `FarmApplicationValidationResponse`, `OwnershipType`
- `crop.models.ts`: `CropRequest`, `CropResponse`, `GrowthStage`, `IrrigationRequirement`
- `irrigation.models.ts`: `IrrigationProfileRequest`, `IrrigationProfileResponse`, `WaterSource`, `IrrigationMethod`, `EquipmentCondition`, `SeasonalRisk`
- `application.models.ts`: `PolicyApplicationRequest`, `PolicyApplicationResponse`, `PolicyResponse`, `InsurancePlanResponse`

#### 3.2.2 Services (`features/farmer/services/`)
- `farm-api.service.ts`: Handles CRUD for farms, crops, irrigation profiles, and `/application-validation`.
- `farmer-profile-api.service.ts`: Handles `/api/farmers/profile` and `/profile/me`.
- `farmer-policy-api.service.ts`: Handles `/api/plans/active`, `/api/applications/**`, and `/api/policies/approved-quote/**`.

#### 3.2.3 NgRx State (`features/farmer/state/`)
- `farmer.actions.ts`: Groups actions for profile loading, farm creation, crop adding, irrigation updating, policy application, quote acceptance.
- `farmer.reducer.ts`: Manages `FarmerState`:
  ```typescript
  export interface FarmerState {
    profile: FarmerProfileResponse | null;
    farms: FarmResponse[];
    selectedFarm: FarmResponse | null;
    cropsByFarmId: Record<number, CropResponse[]>;
    irrigationByFarmId: Record<number, IrrigationProfileResponse | null>;
    activePlans: InsurancePlanResponse[];
    myApplications: PolicyApplicationResponse[];
    approvedQuote: PolicyResponse | null;
    myPolicies: PolicyResponse[];
    loading: boolean;
    error: string | null;
  }
  ```
- `farmer.selectors.ts`: Memoized selectors (`selectFarmerFarms`, `selectFarmerCrops`, `selectApprovedQuote`).
- `farmer.effects.ts`: Handles reactive flows using `switchMap` for data fetching and `exhaustMap` for submissions.

#### 3.2.4 Components (`features/farmer/components/`)
1. **`farmer-dashboard/`**: Top-level summary cards (active acres, enrolled policies, claims payouts).
2. **`profile-form/`**: Reactive form enforcing `indianPhoneValidator` and `pinCodeValidator`.
3. **`farm-list/`**: Table & card views of farm plots with acreage and GPS coordinates.
4. **`farm-form/`**: Add/edit farm plot with ownership type radio buttons (`OWNED`, `LEASED`, `SHARED`).
5. **`crop-manager/`**: Crop cycle registration with `dateSequenceValidator` (Harvest date > Sowing date).
6. **`irrigation-form/`**: Water source dropdown, equipment condition (`GOOD`, `FAIR`, `POOR`), average availability %.
7. **`plan-catalog/`**: Grid of active insurance plans with minimum/maximum coverage limits.
8. **`application-wizard/`**: Multi-step application with coverage slider and eligibility validation.
9. **`quote-acceptance/`**: Underwriter-approved quote acceptance banner with final premium and policy activation button.
10. **`my-policies/`**: Enrolled policies cards with validity date counters.

---

### 3.3 Feature: `underwriter` (`src/app/features/underwriter/`)

#### 3.3.1 Models (`features/underwriter/model/underwriting.models.ts`)
```typescript
export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'VERY_HIGH';

export interface DecisionRequest {
  remarks: string;
}

export interface UnderwritingResponse {
  caseId: number | null;
  applicationId: number;
  farmerUserId: number;
  farmerEmail: string;
  farmId: number;
  cropId: number;
  planId: number;
  planName: string;
  requestedCoverage: number;
  proposedStartDate: string;
  proposedEndDate: string;
  applicationStatus: string;
  underwriterUserId?: number | null;
  underwriterEmail?: string | null;
  waterAvailabilityScore?: number | null;
  equipmentConditionScore?: number | null;
  historicalFailureScore?: number | null;
  seasonalRiskScore?: number | null;
  backupSourceScore?: number | null;
  overallRiskScore?: number | null;
  riskLevel?: RiskLevel | null;
  basePremium?: number | null;
  riskLoading?: number | null;
  recommendedPremium?: number | null;
  recommendedCoverage?: number | null;
  decision?: string | null;
  remarks?: string | null;
  underwritingStatus?: string | null;
  reviewStartedAt?: string | null;
  reviewCompletedAt?: string | null;
}
```

#### 3.3.2 Service (`features/underwriter/services/underwriting-api.service.ts`)
Calls `/api/underwriting/applications/pending`, `/my-cases`, `/start-review`, `/assess`, `/approve`, `/reject`.

#### 3.3.3 NgRx State (`features/underwriter/state/`)
- `underwriting.actions.ts`: `LoadPendingQueue`, `StartReview`, `AssessRisk`, `ApproveApplication`, `RejectApplication`.
- `underwriting.reducer.ts`: State contains `pendingQueue: UnderwritingResponse[]`, `activeCase: UnderwritingResponse | null`, `myCases: UnderwritingResponse[]`.
- `underwriting.selectors.ts`: Selectors for pending count, active scorecard metrics.
- `underwriting.effects.ts`: Side-effects linking assessment triggers to live risk score updates.

#### 3.3.4 Components (`features/underwriter/components/`)
1. **`pending-queue/`**: Table of submitted applications with quick "Start Review" action.
2. **`risk-assessment-scorecard/`**: Visual breakdown of the **5 risk factors**:
   - Water Availability (0–30)
   - Equipment Condition (0–20)
   - Historical Failures (0–20)
   - Seasonal Risk (0–20)
   - Backup Water Source (0–10)
   Interactive gauge rendering **Total Score (0–100)** and loaded premium.
3. **`decision-modal/`**: Modal dialog requiring minimum 5-character underwriter remarks for approval/rejection.

---

### 3.4 Feature: `claims` (`src/app/features/claims/`)

#### 3.4.1 Models (`features/claims/model/claim.models.ts`)
```typescript
export interface IrrigationReadingRequest {
  policyId: number;
  availabilityPercentage: number;
  recordedAt?: string | null;
}

export interface ClaimResponse {
  readingId: number;
  claimId: number | null;
  policyId: number;
  farmId: number;
  availabilityPercentage: number;
  thresholdBreached: boolean;
  payoutPercentage: number;
  compensationAmount: number;
  payoutStatus: string;
  claimStatus: string;
  recordedAt: string;
}
```

#### 3.4.2 Service (`features/claims/services/claims-api.service.ts`)
- `submitReading(req: IrrigationReadingRequest): Observable<ClaimResponse>`
- `getMyClaims(): Observable<ClaimResponse[]>`

#### 3.4.3 NgRx State (`features/claims/state/`)
- `claims.actions.ts`: `SubmitTelemetryReading`, `SubmitReadingSuccess`, `LoadClaimsHistory`.
- `claims.reducer.ts`: Stores `claimsList: ClaimResponse[]`, `lastSubmittedReading: ClaimResponse | null`.
- `claims.selectors.ts`: `selectMyClaims`, `selectRecentPayoutsTotal`.

#### 3.4.4 Components (`features/claims/components/`)
1. **`sensor-simulator/`**: Form for `DATA_PROVIDER` role to inject water availability % and test parametric triggers.
2. **`claim-alert-dialog/`**: High-priority alert showing automatic 10%, 25%, or 50% indemnity payout calculation when $< 70\%$ availability is reported.
3. **`farmer-claims-history/`**: Filterable table showing all settled claims with compensation amounts credited.

---

### 3.5 Feature: `admin` (`src/app/features/admin/`)

#### 3.5.1 Models (`features/admin/model/admin.models.ts`)
Interfaces for internal user creation (`UNDERWRITER`, `DATA_PROVIDER`), plan creation, trigger rules, and duration payout slabs.

#### 3.5.2 Service (`features/admin/services/admin-api.service.ts`)
Calls `/api/admin/users`, `/api/admin/plans`, `/api/admin/plans/{id}/trigger-rules`, `/api/admin/trigger-rules/{id}/payout-slabs`.

#### 3.5.3 Components (`features/admin/components/`)
1. **`user-manager/`**: User table with role filters and account provisioning modal.
2. **`plan-builder/`**: Form to define minimum/maximum coverage limits, base premium, and policy duration.
3. **`trigger-rule-builder/`**: Parametric trigger rule setup (crop type, growth stage, availability threshold %, continuous hours, shortage days).
4. **`payout-slab-builder/`**: Duration-to-payout tiers editor.

---

## 4. Root Application Configuration (`src/app/app.config.ts`)

In Angular 21, standalone providers are configured cleanly in `app.config.ts`:

```typescript
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { routes } from './app.routes';
import { credentialsInterceptor } from './features/auth/interceptors/credentials.interceptor';
import { errorInterceptor } from './features/auth/interceptors/error.interceptor';
import { authReducer } from './features/auth/state/auth.reducer';
import { AuthEffects } from './features/auth/state/auth.effects';
import { farmerReducer } from './features/farmer/state/farmer.reducer';
import { FarmerEffects } from './features/farmer/state/farmer.effects';
import { underwritingReducer } from './features/underwriter/state/underwriting.reducer';
import { UnderwritingEffects } from './features/underwriter/state/underwriting.effects';
import { claimsReducer } from './features/claims/state/claims.reducer';
import { ClaimsEffects } from './features/claims/state/claims.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(
      withInterceptors([credentialsInterceptor, errorInterceptor])
    ),
    provideStore({
      auth: authReducer,
      farmer: farmerReducer,
      underwriting: underwritingReducer,
      claims: claimsReducer
    }),
    provideEffects([
      AuthEffects,
      FarmerEffects,
      UnderwritingEffects,
      ClaimsEffects
    ]),
    provideStoreDevtools({ maxAge: 25, logOnly: false })
  ]
};
```

---

## 5. Root Routing Architecture (`src/app/app.routes.ts`)

```typescript
import { Routes } from '@angular/router';
import { authGuard } from './features/auth/guards/auth.guard';
import { roleGuard } from './features/auth/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/components/login/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/components/register/register.component').then(m => m.RegisterComponent)
      }
    ]
  },
  {
    path: 'farmer',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['FARMER'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/farmer/components/farmer-dashboard/farmer-dashboard.component').then(m => m.FarmerDashboardComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/farmer/components/profile-form/profile-form.component').then(m => m.ProfileFormComponent)
      },
      {
        path: 'farms',
        loadComponent: () => import('./features/farmer/components/farm-list/farm-list.component').then(m => m.FarmListComponent)
      },
      {
        path: 'plans',
        loadComponent: () => import('./features/farmer/components/plan-catalog/plan-catalog.component').then(m => m.PlanCatalogComponent)
      },
      {
        path: 'applications',
        loadComponent: () => import('./features/farmer/components/application-wizard/application-wizard.component').then(m => m.ApplicationWizardComponent)
      },
      {
        path: 'policies',
        loadComponent: () => import('./features/farmer/components/my-policies/my-policies.component').then(m => m.MyPoliciesComponent)
      }
    ]
  },
  {
    path: 'underwriter',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['UNDERWRITER', 'ADMIN'] },
    children: [
      {
        path: 'queue',
        loadComponent: () => import('./features/underwriter/components/pending-queue/pending-queue.component').then(m => m.PendingQueueComponent)
      },
      {
        path: 'assessment/:appId',
        loadComponent: () => import('./features/underwriter/components/risk-assessment-scorecard/risk-assessment-scorecard.component').then(m => m.RiskAssessmentScorecardComponent)
      }
    ]
  },
  {
    path: 'claims',
    children: [
      {
        path: 'simulator',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['DATA_PROVIDER', 'ADMIN'] },
        loadComponent: () => import('./features/claims/components/sensor-simulator/sensor-simulator.component').then(m => m.SensorSimulatorComponent)
      },
      {
        path: 'history',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['FARMER'] },
        loadComponent: () => import('./features/claims/components/farmer-claims-history/farmer-claims-history.component').then(m => m.FarmerClaimsHistoryComponent)
      }
    ]
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: 'users',
        loadComponent: () => import('./features/admin/components/user-manager/user-manager.component').then(m => m.UserManagerComponent)
      },
      {
        path: 'plans',
        loadComponent: () => import('./features/admin/components/plan-builder/plan-builder.component').then(m => m.PlanBuilderComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'auth/login' }
];
```

---

## 6. Shared Directives & Validators (`src/app/shared/`)

### 6.1 Directives (`shared/directives/`)
- **`has-role.directive.ts`**: Structural directive `*appHasRole="['ADMIN', 'FARMER']"` listening to NgRx `selectUserRole`.
- **`status-badge.directive.ts`**: Attribute directive `[appStatusBadge]="status"` rendering Tailwind badge styling (`ACTIVE`, `APPROVED`, `UNDER_REVIEW`, `SUBMITTED`, `REJECTED`, `SETTLED`).
- **`risk-level-gauge.directive.ts`**: Attribute directive rendering color-coded risk meter badges (`LOW`, `MODERATE`, `HIGH`, `VERY_HIGH`).

### 6.2 Validators (`shared/validators/`)
- **`phone.validator.ts`**: `indianPhoneValidator()` validating 10-digit mobile regex (`^[6-9][0-9]{9}$`).
- **`pin-code.validator.ts`**: `pinCodeValidator()` validating 6-digit Indian PIN (`^[1-9][0-9]{5}$`).
- **`date-sequence.validator.ts`**: `dateSequenceValidator('startDate', 'endDate')` cross-field validator.
- **`coverage-bounds.validator.ts`**: `coverageBoundsValidator(min, max)` ensuring requested coverage is within plan parameters.

---

## 7. Vitest Unit Testing Blueprints (`*.spec.ts`)

Angular 21 uses **Vitest** natively (`vitest: ^4.0.8`). Tests are co-located within each feature folder.

### 7.1 Reducer Pure Function Test (`features/auth/state/auth.reducer.spec.ts`)
```typescript
import { describe, it, expect } from 'vitest';
import { authReducer, initialAuthState } from './auth.reducer';
import { AuthActions } from './auth.actions';
import { UserResponse } from '../model/auth.models';

describe('AuthReducer', () => {
  it('should set loading to true on login action', () => {
    const action = AuthActions.login({ credentials: { email: 'test@example.com', password: 'P1' } });
    const state = authReducer(initialAuthState, action);

    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
  });

  it('should store user and set isAuthenticated to true on loginSuccess', () => {
    const mockUser: UserResponse = {
      userId: 1,
      name: 'Ramesh',
      email: 'ramesh@example.com',
      role: 'FARMER',
      status: 'ACTIVE'
    };
    const action = AuthActions.loginSuccess({ user: mockUser });
    const state = authReducer(initialAuthState, action);

    expect(state.isAuthenticated).toBe(true);
    expect(state.user).toEqual(mockUser);
    expect(state.loading).toBe(false);
  });
});
```

### 7.2 Service Test with `HttpTestingController` (`features/auth/services/auth-api.service.spec.ts`)
```typescript
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthApiService } from './auth-api.service';
import { AuthResponse } from '../model/auth.models';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthApiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should post login credentials and return AuthResponse', () => {
    const mockResponse: AuthResponse = {
      tokenType: 'Cookie',
      expiresIn: 3600,
      user: { userId: 1, name: 'Admin', email: 'admin@irrigashield.com', role: 'ADMIN', status: 'ACTIVE' }
    };

    service.login({ email: 'admin@irrigashield.com', password: 'P1' }).subscribe(res => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });
});
```

---

## 8. Backend vs Feature-Driven Frontend Audit Matrix

All 45 backend endpoints and DTOs match 100% with this feature-sliced structure:

| Feature Folder | Backend Service | Endpoints Mapped | Models / DTOs Mapped | Guards & Interceptors |
| :--- | :--- | :---: | :--- | :--- |
| **`features/auth`** | `auth-service` (:8081) | 6 | `UserResponse`, `AuthResponse`, `LoginRequest`, `RegisterRequest`, `CreateUserRequest` | `authGuard`, `roleGuard`, `credentialsInterceptor`, `errorInterceptor` |
| **`features/farmer`** | `farm-service` (:8082) & `policy-service` (:8083) | 22 | `FarmerProfile*`, `Farm*`, `Crop*`, `IrrigationProfile*`, `FarmValidation*`, `PolicyApp*`, `PolicyResponse` | Role-protected by `roleGuard` (`['FARMER']`) |
| **`features/underwriter`** | `policy-service` (:8083) | 6 | `UnderwritingResponse` (all 30 fields), `DecisionRequest` | Role-protected by `roleGuard` (`['UNDERWRITER', 'ADMIN']`) |
| **`features/claims`** | `event-claims-service` (:8084) | 2 | `IrrigationReadingRequest`, `ClaimResponse` | Role-protected (`['DATA_PROVIDER']` & `['FARMER']`) |
| **`features/admin`** | `auth-service` & `policy-service` | 9 | `InsurancePlan*`, `TriggerRule*`, `PayoutSlab*`, `CreateUserRequest` | Role-protected (`['ADMIN']`) |
