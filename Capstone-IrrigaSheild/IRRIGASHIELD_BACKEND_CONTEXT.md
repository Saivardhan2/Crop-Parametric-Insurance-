# IrrigaShield: Full Backend Specification & Frontend Integration Blueprint

This document contains the comprehensive reference manual for the **IrrigaShield** backend microservices platform, designed specifically to guide the implementation of frontend client applications (React, Angular, Vue, or Mobile).

---

## 1. System Overview & Architecture

**IrrigaShield** is a reactive microservices platform providing parametric micro-insurance against agricultural irrigation deficit and drought. Instead of lengthy manual insurance claim surveys, IrrigaShield continuously tracks farm-level telemetry / water availability indices. When conditions cross contractually defined parametric shortage thresholds, pre-calculated indemnity payouts are triggered automatically.

### Architecture Topography

All frontend traffic routes through the central **Spring Cloud API Gateway** running on port `8080`. The Gateway handles JWT authentication validation and strips untrusted headers before routing requests to individual domain microservices registered with Netflix Eureka.

```
+-------------------------------------------------------------------------------+
|                             Frontend Web Client                               |
|               (React / Vite / Tailwind / Axios credentials:true)              |
+-------------------------------------------------------------------------------+
                                        |  HTTP Cookie: IRRIGASHIELD_TOKEN
                                        v
+-------------------------------------------------------------------------------+
|                       API Gateway (Port 8080 / Reactive)                      |
|           • Validates IRRIGASHIELD_TOKEN HTTP-only cookie                     |
|           • Injects X-User-Id, X-User-Email, X-User-Roles                     |
+-------------------------------------------------------------------------------+
         |                     |                       |                   |
         | /api/auth/**        | /api/farmers/**       | /api/plans/**     | /api/readings/**
         | /api/users/**       | /api/farms/**         | /api/applications/| /api/claims/**
         |                     |                       | /api/policies/**  |
         v                     v                       v                   v
+-----------------+   +-----------------+   +--------------------+   +-------------------+
|  auth-service   |   |  farm-service   |   |   policy-service   |   |event-claims-serv..|
|   (Port 8081)   |   |   (Port 8082)   |   |    (Port 8083)     |   |    (Port 8084)    |
|   MySQL R2DBC   |   |   MySQL R2DBC   |   |    MySQL R2DBC     |   |    MySQL R2DBC    |
+-----------------+   +-----------------+   +--------------------+   +-------------------+
                                ^                     ^
                                |---- WebClient ------|
                                |     (Risk Profile)  |
                                                      |
                                                      |------ WebClient (Policy Details)
```

---

## 2. Authentication & Authorization Protocol

### 2.1 Cookie-Based Authentication
- The backend uses an **HTTP-only cookie** named `IRRIGASHIELD_TOKEN`.
- **Frontend Obligation**: All HTTP clients (e.g. Axios, Fetch API) MUST send `withCredentials: true` (or `credentials: 'include'`) for every request.
- The browser handles storing and forwarding this cookie automatically.
- No Bearer tokens need to be managed in local storage or memory.

### 2.2 User Roles & Permissions
Every user in the system is assigned a role that determines access:

| Role Name | Scope & Responsibilities |
| :--- | :--- |
| `FARMER` | Manages farmer profile, farms, crops, irrigation profiles; applies for insurance; reviews and accepts quotes; monitors claims. |
| `UNDERWRITER` | Reviews submitted insurance applications, triggers risk assessments, approves or rejects applications with custom remarks. |
| `CLAIMS_OFFICER` | Reviews automated claim logs, monitors settlement records. |
| `DATA_PROVIDER` | Ingests telemetry readings (IoT sensors, irrigation telemetry, water gauges). |
| `ADMIN` | Creates insurance plans, trigger rules, payout slabs, registers system users. |

### 2.3 Bootstrap Credentials
- **Default Admin Account**:
  - Email: `admin@irrigashield.com`
  - Password: `Admin12345`

---

## 3. Global Enums & Constants

Use these exact string constants across forms, filters, and dropdowns:

```typescript
// User Roles
export type Role = 'FARMER' | 'UNDERWRITER' | 'CLAIMS_OFFICER' | 'DATA_PROVIDER' | 'ADMIN';

// Farm Land Ownership
export type OwnershipType = 'OWNED' | 'LEASED' | 'SHARED';

// Crop Growth Stage
export type GrowthStage = 
  | 'SOWING' 
  | 'GERMINATION' 
  | 'VEGETATIVE' 
  | 'FLOWERING' 
  | 'FRUITING' 
  | 'MATURITY';

// Irrigation Requirement
export type IrrigationRequirement = 'LOW' | 'MEDIUM' | 'HIGH';

// Water Source
export type WaterSource = 
  | 'BOREWELL' 
  | 'CANAL' 
  | 'WELL' 
  | 'RIVER' 
  | 'RESERVOIR' 
  | 'TANKER' 
  | 'RAINWATER';

// Backup Water Source
export type BackupWaterSource = WaterSource | 'NONE';

// Irrigation Distribution Method
export type IrrigationMethod = 'DRIP' | 'SPRINKLER' | 'FLOOD' | 'FURROW' | 'MANUAL';

// Physical Equipment Condition
export type EquipmentCondition = 'GOOD' | 'FAIR' | 'POOR';

// Seasonal Water Shortage Risk
export type SeasonalRisk = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';

// Maintenance Schedule
export type MaintenanceFrequency = 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'NONE';

// Underwriting & Policy Application Status
export type ApplicationStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';

// Policy State
export type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED';

// Policy Payment State
export type PaymentStatus = 'PAID' | 'PENDING' | 'FAILED';

// Risk Rating Classification
export type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'VERY_HIGH';

// Claim Status
export type ClaimStatus = 'SETTLED' | 'NO_CLAIM';
export type PayoutStatus = 'SUCCESS' | 'NOT_REQUIRED';
```

---

## 4. End-to-End Endpoint Catalog

Base URL for all calls: `http://localhost:8080` (API Gateway)

### 4.1 Authentication Service (`auth-service`)

#### `POST /api/auth/register`
- **Access**: Public
- **Description**: Self-registration for farmers.
- **Request Body**:
  ```json
  {
    "name": "Ramesh Kumar",
    "email": "ramesh@example.com",
    "password": "Password123"
  }
  ```
  *(Password rule: min 8, max 64 chars, at least 1 uppercase, 1 lowercase, 1 number)*
- **Response** (`201 Created`):
  ```json
  {
    "userId": 2,
    "name": "Ramesh Kumar",
    "email": "ramesh@example.com",
    "role": "FARMER",
    "status": "ACTIVE"
  }
  ```

#### `POST /api/auth/login`
- **Access**: Public
- **Description**: Authenticates user and sets `IRRIGASHIELD_TOKEN` HTTP-only cookie.
- **Request Body**:
  ```json
  {
    "email": "ramesh@example.com",
    "password": "Password123"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "tokenType": "Cookie",
    "expiresIn": 3600,
    "user": {
      "userId": 2,
      "name": "Ramesh Kumar",
      "email": "ramesh@example.com",
      "role": "FARMER",
      "status": "ACTIVE"
    }
  }
  ```

#### `POST /api/auth/logout`
- **Access**: Authenticated
- **Description**: Clears `IRRIGASHIELD_TOKEN` cookie.
- **Response** (`204 No Content`)

#### `GET /api/users/me`
- **Access**: Authenticated (All roles)
- **Description**: Returns authenticated user's session profile.
- **Response** (`200 OK`):
  ```json
  {
    "userId": 2,
    "name": "Ramesh Kumar",
    "email": "ramesh@example.com",
    "role": "FARMER",
    "status": "ACTIVE"
  }
  ```

#### `POST /api/admin/users`
- **Access**: `ADMIN`
- **Description**: Provision internal accounts (Underwriters, Data Providers, Admins).
- **Request Body**:
  ```json
  {
    "name": "Anita Roy",
    "email": "anita.underwriter@irrigashield.com",
    "password": "SecurePassword1",
    "role": "UNDERWRITER"
  }
  ```
- **Response** (`201 Created`): UserResponse object.

#### `GET /api/admin/users`
- **Access**: `ADMIN`
- **Response** (`200 OK`): List of UserResponse objects.

---

### 4.2 Farm & Irrigation Service (`farm-service`)

#### `POST /api/farmers/profile`
- **Access**: `FARMER`
- **Description**: First-time profile initialization for the logged-in farmer.
- **Request Body**:
  ```json
  {
    "fullName": "Ramesh Kumar",
    "phoneNumber": "9876543210",
    "addressLine": "House 45, Main Road",
    "village": "Rampur",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "411001",
    "bankAccountLastFour": "1234"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "farmerId": 1,
    "authUserId": 2,
    "email": "ramesh@example.com",
    "fullName": "Ramesh Kumar",
    "phoneNumber": "9876543210",
    "addressLine": "House 45, Main Road",
    "village": "Rampur",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "411001",
    "bankAccountLastFour": "1234",
    "verificationStatus": "VERIFIED",
    "createdAt": "2026-03-29T10:00:00",
    "updatedAt": "2026-03-29T10:00:00"
  }
  ```

#### `GET /api/farmers/profile/me`
- **Access**: `FARMER`
- **Response** (`200 OK`): FarmerProfileResponse.

#### `PUT /api/farmers/profile/me`
- **Access**: `FARMER`
- **Request Body**: Same as FarmerProfileRequest.
- **Response** (`200 OK`): Updated FarmerProfileResponse.

---

#### `POST /api/farms`
- **Access**: `FARMER`
- **Description**: Register a new farm plot.
- **Request Body**:
  ```json
  {
    "farmName": "Green Valley Farm",
    "surveyNumber": "SY-104/2B",
    "addressLine": "Plot 12, Riverbed East",
    "village": "Rampur",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "411001",
    "areaInAcres": 15.50,
    "soilType": "Black Cotton Soil",
    "ownershipType": "OWNED",
    "latitude": 18.5204,
    "longitude": 73.8567
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "farmId": 10,
    "farmerId": 1,
    "farmName": "Green Valley Farm",
    "surveyNumber": "SY-104/2B",
    "addressLine": "Plot 12, Riverbed East",
    "village": "Rampur",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "411001",
    "areaInAcres": 15.50,
    "soilType": "Black Cotton Soil",
    "ownershipType": "OWNED",
    "latitude": 18.5204,
    "longitude": 73.8567,
    "status": "ACTIVE",
    "createdAt": "2026-03-29T10:15:00",
    "updatedAt": "2026-03-29T10:15:00"
  }
  ```

#### `GET /api/farms/my-farms`
- **Access**: `FARMER`
- **Response** (`200 OK`): Array of FarmResponse objects.

#### `GET /api/farms/{farmId}`
- **Access**: `FARMER`, `UNDERWRITER`, `ADMIN`
- **Response** (`200 OK`): FarmResponse object.

#### `PUT /api/farms/{farmId}`
- **Access**: `FARMER`
- **Request Body**: Same as FarmRequest.
- **Response** (`200 OK`): Updated FarmResponse object.

---

#### `POST /api/farms/{farmId}/crops`
- **Access**: `FARMER`
- **Description**: Add a crop cultivation cycle to a farm.
- **Request Body**:
  ```json
  {
    "cropType": "Sugarcane",
    "variety": "Co 86032",
    "sowingDate": "2026-01-15",
    "expectedHarvestDate": "2026-12-15",
    "growthStage": "VEGETATIVE",
    "irrigationRequirement": "HIGH"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "cropId": 101,
    "farmId": 10,
    "cropType": "Sugarcane",
    "variety": "Co 86032",
    "sowingDate": "2026-01-15",
    "expectedHarvestDate": "2026-12-15",
    "growthStage": "VEGETATIVE",
    "irrigationRequirement": "HIGH",
    "status": "ACTIVE",
    "createdAt": "2026-03-29T10:20:00",
    "updatedAt": "2026-03-29T10:20:00"
  }
  ```

#### `GET /api/farms/{farmId}/crops`
- **Access**: `FARMER`, `UNDERWRITER`, `ADMIN`
- **Response** (`200 OK`): Array of CropResponse objects.

#### `GET /api/farms/{farmId}/crops/{cropId}`
- **Access**: `FARMER`, `UNDERWRITER`, `ADMIN`
- **Response** (`200 OK`): CropResponse object.

#### `PUT /api/farms/{farmId}/crops/{cropId}`
- **Access**: `FARMER`
- **Request Body**: Same as CropRequest.
- **Response** (`200 OK`): Updated CropResponse object.

---

#### `POST /api/farms/{farmId}/irrigation`
- **Access**: `FARMER`
- **Description**: Establish irrigation infrastructure and reliability profile.
- **Request Body**:
  ```json
  {
    "waterSource": "BOREWELL",
    "irrigationMethod": "DRIP",
    "equipmentCondition": "GOOD",
    "backupWaterSource": "CANAL",
    "historicalFailureCount": 1,
    "averageWaterAvailabilityPercentage": 82.50,
    "seasonalRisk": "LOW",
    "maintenanceFrequency": "QUARTERLY",
    "lastMaintenanceDate": "2026-02-10"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "irrigationId": 50,
    "farmId": 10,
    "waterSource": "BOREWELL",
    "irrigationMethod": "DRIP",
    "equipmentCondition": "GOOD",
    "backupWaterSource": "CANAL",
    "historicalFailureCount": 1,
    "averageWaterAvailabilityPercentage": 82.50,
    "seasonalRisk": "LOW",
    "maintenanceFrequency": "QUARTERLY",
    "lastMaintenanceDate": "2026-02-10",
    "status": "ACTIVE",
    "createdAt": "2026-03-29T10:30:00",
    "updatedAt": "2026-03-29T10:30:00"
  }
  ```

#### `GET /api/farms/{farmId}/irrigation`
- **Access**: `FARMER`, `UNDERWRITER`, `ADMIN`
- **Response** (`200 OK`): IrrigationProfileResponse object.

#### `PUT /api/farms/{farmId}/irrigation`
- **Access**: `FARMER`
- **Request Body**: Same as IrrigationProfileRequest.
- **Response** (`200 OK`): Updated IrrigationProfileResponse.

---

#### `GET /api/farms/{farmId}/risk-profile`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Description**: Bundled telemetry view used by underwriters during risk evaluation.
- **Response** (`200 OK`):
  ```json
  {
    "farm": { "farmId": 10, "farmName": "Green Valley Farm", ... },
    "crops": [ { "cropId": 101, "cropType": "Sugarcane", ... } ],
    "irrigationProfile": { "waterSource": "BOREWELL", "equipmentCondition": "GOOD", ... }
  }
  ```

---

### 4.3 Insurance Policy & Underwriting Service (`policy-service`)

#### `GET /api/plans/active`
- **Access**: `FARMER`, `UNDERWRITER`, `ADMIN`
- **Description**: Browse active parametric insurance plans available for subscription.
- **Response** (`200 OK`):
  ```json
  [
    {
      "planId": 1,
      "planName": "Sugarcane Drought Shield",
      "description": "Parametric water-shortage coverage for vegetative and maturity stages.",
      "minimumCoverage": 50000.00,
      "maximumCoverage": 500000.00,
      "basePremium": 2500.00,
      "maximumPolicyDays": 180,
      "status": "ACTIVE",
      "createdBy": 1,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ]
  ```

#### `GET /api/plans/{planId}`
- **Access**: `FARMER`, `UNDERWRITER`, `ADMIN`
- **Response** (`200 OK`): InsurancePlanResponse object.

---

#### `POST /api/applications`
- **Access**: `FARMER`
- **Description**: Create an insurance policy application draft.
- **Request Body**:
  ```json
  {
    "farmId": 10,
    "cropId": 101,
    "planId": 1,
    "requestedCoverage": 200000.00,
    "proposedStartDate": "2026-04-01",
    "proposedEndDate": "2026-09-28"
  }
  ```
- **Response** (`201 Created`):
  ```json
  {
    "applicationId": 501,
    "farmerUserId": 2,
    "farmerEmail": "ramesh@example.com",
    "farmId": 10,
    "cropId": 101,
    "planId": 1,
    "requestedCoverage": 200000.00,
    "proposedStartDate": "2026-04-01",
    "proposedEndDate": "2026-09-28",
    "status": "DRAFT",
    "submittedAt": null,
    "createdAt": "2026-03-29T11:00:00",
    "updatedAt": "2026-03-29T11:00:00"
  }
  ```

#### `GET /api/applications/my-applications`
- **Access**: `FARMER`
- **Response** (`200 OK`): Array of PolicyApplicationResponse objects.

#### `GET /api/applications/{applicationId}`
- **Access**: `FARMER`
- **Response** (`200 OK`): PolicyApplicationResponse.

#### `PUT /api/applications/{applicationId}`
- **Access**: `FARMER`
- **Description**: Modify draft application before submission.
- **Request Body**: Same as PolicyApplicationRequest.
- **Response** (`200 OK`): Updated PolicyApplicationResponse.

#### `POST /api/applications/{applicationId}/submit`
- **Access**: `FARMER`
- **Description**: Transition application status from `DRAFT` to `SUBMITTED`, queuing it for underwriting.
- **Response** (`200 OK`): PolicyApplicationResponse with `status: "SUBMITTED"`.

---

#### Underwriting Engine Endpoints (`/api/underwriting/**`)

#### `GET /api/underwriting/applications/pending`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Description**: Underwriter queue of submitted applications awaiting review.
- **Response** (`200 OK`): Array of UnderwritingResponse objects.

#### `GET /api/underwriting/my-cases`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Description**: History of cases reviewed or assigned to the logged-in underwriter.
- **Response** (`200 OK`): Array of UnderwritingResponse objects.

#### `POST /api/underwriting/applications/{applicationId}/start-review`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Description**: Claims the application and initializes the underwriting case (`UNDER_REVIEW`).
- **Response** (`200 OK`): UnderwritingResponse.

#### `POST /api/underwriting/applications/{applicationId}/assess`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Description**: Triggers automated mathematical risk scoring (`RiskCalculator`) querying `farm-service` telemetry.
- **Response** (`200 OK`): UnderwritingResponse populated with:
  - `waterAvailabilityScore` (0 to 30)
  - `equipmentConditionScore` (0 to 20)
  - `historicalFailureScore` (0 to 20)
  - `seasonalRiskScore` (0 to 20)
  - `backupSourceScore` (0 to 10)
  - `overallRiskScore` (sum of scores)
  - `riskLevel` (`LOW`, `MODERATE`, `HIGH`, `VERY_HIGH`)
  - `basePremium`
  - `riskLoading`
  - `recommendedPremium`
  - `recommendedCoverage`

#### `PUT /api/underwriting/applications/{applicationId}/approve`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Request Body**:
  ```json
  {
    "remarks": "Risk evaluation satisfies parametric underwriting criteria. Good equipment and backup canal access."
  }
  ```
- **Response** (`200 OK`): UnderwritingResponse with `decision: "APPROVED"`, `applicationStatus: "APPROVED"`.

#### `PUT /api/underwriting/applications/{applicationId}/reject`
- **Access**: `UNDERWRITER`, `ADMIN`
- **Request Body**:
  ```json
  {
    "remarks": "Frequent pump failures and high water deficit risk exceed acceptable risk thresholds."
  }
  ```
- **Response** (`200 OK`): UnderwritingResponse with `decision: "REJECTED"`, `applicationStatus: "REJECTED"`.

---

#### Policy Issuance & Quote Acceptance (`/api/policies/**`)

#### `GET /api/policies/approved-quote/{applicationId}`
- **Access**: `FARMER`
- **Description**: Allows the farmer to inspect the final underwriter-approved coverage and premium offer.
- **Response** (`200 OK`):
  ```json
  {
    "policyId": null,
    "applicationId": 501,
    "farmerUserId": 2,
    "farmerEmail": "ramesh@example.com",
    "farmId": 10,
    "cropId": 101,
    "planId": 1,
    "planName": "Sugarcane Drought Shield",
    "coverageAmount": 200000.00,
    "premiumAmount": 2625.00,
    "startDate": "2026-04-01",
    "endDate": "2026-09-28",
    "paymentStatus": "PAID",
    "status": "APPROVED",
    "acceptedAt": null
  }
  ```

#### `POST /api/policies/approved-quote/{applicationId}/accept`
- **Access**: `FARMER`
- **Description**: Confirms quote acceptance, creates active insurance policy.
- **Response** (`200 OK`):
  ```json
  {
    "policyId": 1001,
    "applicationId": 501,
    "farmerUserId": 2,
    "farmerEmail": "ramesh@example.com",
    "farmId": 10,
    "cropId": 101,
    "planId": 1,
    "planName": "Sugarcane Drought Shield",
    "coverageAmount": 200000.00,
    "premiumAmount": 2625.00,
    "startDate": "2026-04-01",
    "endDate": "2026-09-28",
    "paymentStatus": "PAID",
    "status": "ACTIVE",
    "acceptedAt": "2026-03-29T12:00:00"
  }
  ```

#### `GET /api/policies/my-policies`
- **Access**: `FARMER`
- **Description**: Lists all active and historical policies held by the logged-in farmer.
- **Response** (`200 OK`): Array of PolicyResponse objects.

---

#### Admin Plan & Trigger Configuration (`/api/admin/**`)

#### `POST /api/admin/plans`
- **Access**: `ADMIN`
- **Request Body**:
  ```json
  {
    "planName": "Paddy Early Monsoon Shield",
    "description": "Coverage against delayed monsoon and dry spell during vegetative phase.",
    "minimumCoverage": 25000.00,
    "maximumCoverage": 300000.00,
    "basePremium": 1500.00,
    "maximumPolicyDays": 120
  }
  ```
- **Response** (`201 Created`): InsurancePlanResponse.

#### `GET /api/admin/plans`
- **Access**: `ADMIN`
- **Response** (`200 OK`): Array of all InsurancePlanResponse objects.

#### `POST /api/admin/plans/{planId}/trigger-rules`
- **Access**: `ADMIN`
- **Description**: Defines index trigger conditions.
- **Request Body**:
  ```json
  {
    "cropType": "Paddy",
    "growthStage": "VEGETATIVE",
    "availabilityThresholdPercentage": 70.00,
    "minimumContinuousHours": 48,
    "rollingWindowDays": 7,
    "requiredShortageDays": 3
  }
  ```
- **Response** (`201 Created`): TriggerRuleResponse.

#### `GET /api/admin/plans/{planId}/trigger-rules`
- **Access**: `ADMIN`
- **Response** (`200 OK`): Array of TriggerRuleResponse.

#### `POST /api/admin/trigger-rules/{triggerRuleId}/payout-slabs`
- **Access**: `ADMIN`
- **Description**: Adds duration-to-payout tiers.
- **Request Body**:
  ```json
  {
    "minimumDurationHours": 48,
    "maximumDurationHours": 72,
    "payoutPercentage": 25.00
  }
  ```
- **Response** (`201 Created`): PayoutSlabResponse.

#### `GET /api/admin/trigger-rules/{triggerRuleId}/payout-slabs`
- **Access**: `ADMIN`
- **Response** (`200 OK`): Array of PayoutSlabResponse.

---

### 4.4 Telemetry & Parametric Claims Service (`event-claims-service`)

#### `POST /api/readings`
- **Access**: `DATA_PROVIDER`, `ADMIN`
- **Description**: Ingests IoT water availability telemetry. Automatically triggers parametric settlement if threshold is breached.
- **Request Body**:
  ```json
  {
    "policyId": 1001,
    "availabilityPercentage": 24.50,
    "recordedAt": "2026-05-15T14:30:00"
  }
  ```
- **Response** (`201 Created`):
  *When threshold breached (< 70%):*
  ```json
  {
    "readingId": 8001,
    "claimId": 9001,
    "policyId": 1001,
    "farmId": 10,
    "availabilityPercentage": 24.50,
    "thresholdBreached": true,
    "payoutPercentage": 50.00,
    "compensationAmount": 100000.00,
    "payoutStatus": "SUCCESS",
    "claimStatus": "SETTLED",
    "recordedAt": "2026-05-15T14:30:00"
  }
  ```
  *When reading is normal (>= 70%):*
  ```json
  {
    "readingId": 8002,
    "claimId": null,
    "policyId": 1001,
    "farmId": 10,
    "availabilityPercentage": 85.00,
    "thresholdBreached": false,
    "payoutPercentage": 0.00,
    "compensationAmount": 0.00,
    "payoutStatus": "NOT_REQUIRED",
    "claimStatus": "NO_CLAIM",
    "recordedAt": "2026-05-16T09:00:00"
  }
  ```

#### `GET /api/claims/my-claims`
- **Access**: `FARMER`
- **Description**: Returns all automated claim payouts credited to the logged-in farmer.
- **Response** (`200 OK`): Array of ClaimResponse objects.

---

## 5. Risk Calculation & Claim Settlement Logic

### 5.1 Underwriting Risk Engine

When an underwriter clicks **"Assess"**, `RiskCalculator` computes scores across five weighted categories:

1. **Water Availability Score (Max 30)**:
   - $\ge 85\% \rightarrow 0$ points
   - $70\% - 84.99\% \rightarrow 10$ points
   - $50\% - 69.99\% \rightarrow 20$ points
   - $< 50\% \rightarrow 30$ points
2. **Equipment Condition Score (Max 20)**:
   - `GOOD` $\rightarrow 0$ points
   - `FAIR` $\rightarrow 10$ points
   - `POOR` $\rightarrow 20$ points
3. **Historical Failure Score (Max 20)**:
   - $0$ failures $\rightarrow 0$ points
   - $1 - 2$ failures $\rightarrow 10$ points
   - $> 2$ failures $\rightarrow 20$ points
4. **Seasonal Risk Score (Max 20)**:
   - `LOW` $\rightarrow 0$ points
   - `MEDIUM` $\rightarrow 10$ points
   - `HIGH` $\rightarrow 15$ points
   - `VERY_HIGH` $\rightarrow 20$ points
5. **Backup Water Source Score (Max 10)**:
   - Has valid backup source $\rightarrow 0$ points
   - `NONE` or blank $\rightarrow 10$ points

$$\text{Overall Score} = \text{Score}_{\text{avail}} + \text{Score}_{\text{equip}} + \text{Score}_{\text{fail}} + \text{Score}_{\text{season}} + \text{Score}_{\text{backup}}$$

#### Risk Tier & Premium Loading

| Total Score | Risk Level | Loading % | Recommended Premium Calculation |
| :---: | :---: | :---: | :--- |
| **0 – 25** | `LOW` | **5%** | $\text{Base Premium} + (\text{Base} \times 0.05)$ |
| **26 – 50** | `MODERATE` | **15%** | $\text{Base Premium} + (\text{Base} \times 0.15)$ |
| **51 – 75** | `HIGH` | **30%** | $\text{Base Premium} + (\text{Base} \times 0.30)$ |
| **76 – 100** | `VERY_HIGH` | **50%** | $\text{Base Premium} + (\text{Base} \times 0.50)$ |

Recommended Coverage is clamped within the plan's range:
$$\text{Recommended Coverage} = \max(\text{Min Coverage}, \min(\text{Max Coverage}, \text{Requested Coverage}))$$

### 5.2 Parametric Claim Tier Formula

When a sensor reading is submitted:
- If Availability $\ge 70\% \rightarrow$ **No claim** (`NO_CLAIM`).
- If Availability $< 70\%$ and policy has not already been settled:
  - $< 30\%$ availability $\rightarrow$ **50% Payout**
  - $30\% - 49.99\%$ availability $\rightarrow$ **25% Payout**
  - $50\% - 69.99\%$ availability $\rightarrow$ **10% Payout**
$$\text{Compensation Amount} = \text{Coverage Amount} \times \left(\frac{\text{Payout \%}}{100}\right)$$

---

## 6. Frontend UI Application Architecture

To provide a complete user experience for all stakeholders, organize the frontend into the following role-based layout:

### 6.1 Farmer Portal
1. **Onboarding / Profile Screen**:
   - Contact info, 6-digit postal code, 4-digit bank account reference.
2. **Farm Land & Crops Management**:
   - Farm list with acreage, soil type, and map coordinates.
   - Crop sub-table (type, variety, sowing/harvest dates, growth stage).
   - Irrigation setup modal (water source, method, pump condition, backup).
3. **Insurance Plans Explorer**:
   - Cards displaying active plans with coverage limits and base premium.
   - "Apply" modal with coverage amount slider and dates picker.
4. **My Applications & Quotes**:
   - Status tracker (`DRAFT` $\rightarrow$ `SUBMITTED` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `APPROVED`).
   - "Review & Accept Quote" banner showing final premium and coverage breakdown.
5. **My Active Policies**:
   - Cards of active policies, validity date countdown, and coverage details.
6. **Claims & Compensations Log**:
   - Real-time list of settled claims, water availability triggers, and payout amounts credited.

### 6.2 Underwriter Portal
1. **Pending Applications Queue**:
   - List of newly submitted farmer applications (`GET /api/underwriting/applications/pending`).
2. **Interactive Underwriting Dashboard**:
   - "Start Review" action to claim case.
   - "Run Assessment" button that fetches real-time risk profile from `farm-service` and displays the 5-point score breakdown.
   - Interactive gauge for `Overall Risk Score` and `Risk Level`.
   - Side-by-side comparison of Requested vs. Recommended Premium & Coverage.
   - Approval / Rejection form with mandatory remarks.
3. **Reviewed Cases History**:
   - Filterable table of past underwriting decisions.

### 6.3 Data Provider / IoT Simulator Portal
1. **Sensor Reading Ingestion Console**:
   - Form with Policy ID, simulated water availability %, and timestamp.
   - Live visual outcome box:
     - Normal Status Badge (`NO_CLAIM`)
     - Instant Trigger Alert (`SETTLED` claim with compensation amount).

### 6.4 Admin Portal
1. **User Management**:
   - Create and list internal accounts (`UNDERWRITER`, `DATA_PROVIDER`, `ADMIN`).
2. **Plan & Trigger Rule Builder**:
   - Create insurance plans with coverage boundaries.
   - Assign parametric trigger rules (availability %, shortage days, rolling windows) and payout slab tiers per crop and growth stage.

---

## 7. Global API Error Response Structure

All services return standardized error envelopes when HTTP errors occur:

```json
{
  "status": 409,
  "errorCode": "FARM_PROFILE_INCOMPLETE",
  "message": "Complete farm, crop, and irrigation details before applying",
  "path": "/api/applications/501/submit",
  "timestamp": "2026-03-29T12:30:00"
}
```

### Frequent Error Codes to Handle in UI
- `UNAUTHORIZED`: Authentication cookie missing or expired $\rightarrow$ redirect to `/login`.
- `FARM_PROFILE_INCOMPLETE`: Farmer has not yet filled out irrigation or crop info.
- `SETTLED_CLAIM_ALREADY_EXISTS`: Policy has already claimed its parametric compensation.
- `READING_OUTSIDE_POLICY_PERIOD`: Telemetry reading date falls outside policy start/end dates.
- `FUTURE_READING_NOT_ALLOWED`: Sensor reading timestamp is in the future.
- `REVIEW_ALREADY_STARTED`: Another underwriter has already claimed the application.
