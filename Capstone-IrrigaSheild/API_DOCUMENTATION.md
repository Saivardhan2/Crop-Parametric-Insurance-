# IrrigaShield Platform - Comprehensive API Documentation

**Version:** 1.0.0  
**Base Gateway URL:** `http://localhost:8080`  
**Protocol:** HTTP/1.1 REST (JSON)  
**Security Model:** HTTP-Only Cookie (`IRRIGASHIELD_TOKEN`) + Reactive JWT Gateway Propagation  

---

## Table of Contents
1. [Architecture & Gateway Routing Overview](#1-architecture--gateway-routing-overview)
2. [Authentication & Authorization Model](#2-authentication--authorization-model)
3. [Global Standards, Enums & Error Conventions](#3-global-standards-enums--error-conventions)
4. [Authentication & User Management APIs](#4-authentication--user-management-apis)
   - [POST /api/auth/register](#post-apiauthregister)
   - [POST /api/auth/login](#post-apiauthlogin)
   - [POST /api/auth/logout](#post-apiauthlogout)
   - [GET /api/users/me](#get-apiusersme)
   - [POST /api/admin/users](#post-apiadminusers)
   - [GET /api/admin/users](#get-apiadminusers)
5. [Farm & Irrigation Management APIs](#5-farm--irrigation-management-apis)
   - [POST /api/farmers/profile](#post-apifarmersprofile)
   - [GET /api/farmers/profile/me](#get-apifarmersprofileme)
   - [PUT /api/farmers/profile/me](#put-apifarmersprofileme)
   - [POST /api/farms](#post-apifarms)
   - [GET /api/farms/my-farms](#get-apifarmsmy-farms)
   - [GET /api/farms/{farmId}](#get-apifarmsfarmid)
   - [PUT /api/farms/{farmId}](#put-apifarmsfarmid)
   - [POST /api/farms/{farmId}/crops](#post-apifarmsfarmidcrops)
   - [GET /api/farms/{farmId}/crops](#get-apifarmsfarmidcrops)
   - [GET /api/farms/{farmId}/crops/{cropId}](#get-apifarmsfarmidcropscropid)
   - [PUT /api/farms/{farmId}/crops/{cropId}](#put-apifarmsfarmidcropscropid)
   - [POST /api/farms/{farmId}/irrigation](#post-apifarmsfarmidirrigation)
   - [GET /api/farms/{farmId}/irrigation](#get-apifarmsfarmidirrigation)
   - [PUT /api/farms/{farmId}/irrigation](#put-apifarmsfarmidirrigation)
   - [GET /api/farms/{farmId}/crops/{cropId}/application-validation](#get-apifarmsfarmidcropscropidapplication-validation)
   - [GET /api/farms/{farmId}/risk-profile](#get-apifarmsfarmidrisk-profile)
6. [Insurance Plans & Trigger Configuration APIs](#6-insurance-plans--trigger-configuration-apis)
   - [GET /api/plans/active](#get-apiplansactive)
   - [GET /api/plans/{planId}](#get-apiplansplanid)
   - [POST /api/admin/plans](#post-apiadminplans)
   - [GET /api/admin/plans](#get-apiadminplans)
   - [POST /api/admin/plans/{planId}/trigger-rules](#post-apiadminplansplanidtrigger-rules)
   - [GET /api/admin/plans/{planId}/trigger-rules](#get-apiadminplansplanidtrigger-rules)
   - [POST /api/admin/trigger-rules/{triggerRuleId}/payout-slabs](#post-apiadmintrigger-rulestriggerruleidpayout-slabs)
   - [GET /api/admin/trigger-rules/{triggerRuleId}/payout-slabs](#get-apiadmintrigger-rulestriggerruleidpayout-slabs)
7. [Policy Applications & Underwriting APIs](#7-policy-applications--underwriting-apis)
   - [POST /api/applications](#post-apiapplications)
   - [GET /api/applications/my-applications](#get-apiapplicationsmy-applications)
   - [GET /api/applications/{applicationId}](#get-apiapplicationsapplicationid)
   - [PUT /api/applications/{applicationId}](#put-apiapplicationsapplicationid)
   - [POST /api/applications/{applicationId}/submit](#post-apiapplicationsapplicationidsubmit)
   - [GET /api/underwriting/applications/pending](#get-apiunderwritingapplicationspending)
   - [GET /api/underwriting/my-cases](#get-apiunderwritingmy-cases)
   - [POST /api/underwriting/applications/{applicationId}/start-review](#post-apiunderwritingapplicationsapplicationidstart-review)
   - [POST /api/underwriting/applications/{applicationId}/assess](#post-apiunderwritingapplicationsapplicationidassess)
   - [PUT /api/underwriting/applications/{applicationId}/approve](#put-apiunderwritingapplicationsapplicationidapprove)
   - [PUT /api/underwriting/applications/{applicationId}/reject](#put-apiunderwritingapplicationsapplicationidreject)
8. [Policy Issuance & Active Policies APIs](#8-policy-issuance--active-policies-apis)
   - [GET /api/policies/approved-quote/{applicationId}](#get-apipoliciesapproved-quoteapplicationid)
   - [POST /api/policies/approved-quote/{applicationId}/accept](#post-apipoliciesapproved-quoteapplicationidaccept)
   - [GET /api/policies/my-policies](#get-apipoliciesmy-policies)
   - [GET /api/internal/policies/{policyId}](#get-apiinternalpoliciespolicyid)
9. [Telemetry Ingestion & Parametric Claims APIs](#9-telemetry-ingestion--parametric-claims-apis)
   - [POST /api/readings](#post-apireadings)
   - [GET /api/claims/my-claims](#get-apiclaimsmy-claims)
10. [Business Rules, Scoring & Settlement Engines](#10-business-rules-scoring--settlement-engines)
11. [Master Error Codes Reference](#11-master-error-codes-reference)

---

## 1. Architecture & Gateway Routing Overview

The platform uses a reactive microservice topography built on **Spring Cloud Gateway (WebFlux)**, **Netflix Eureka**, and **Spring Boot R2DBC** non-blocking persistence.

```
                              [Frontend Client / Angular / Postman / IoT]
                                                  |
                                                  | HTTP Cookie: IRRIGASHIELD_TOKEN
                                                  v
                     +-----------------------------------------------------------+
                     |                Spring Cloud API Gateway                   |
                     |                Port: 8080 (Reactive WebFlux)               |
                     |  • Cookie extraction & JWT verification                   |
                     |  • Injects: X-User-Id, X-User-Email, X-User-Roles         |
                     |  • Strips external identity headers for zero-trust spoof  |
                     +-----------------------------------------------------------+
                                   |          |            |           |
            +----------------------+          |            |           +-----------------------+
            |                                 |            |                                   |
            v                                 v            v                                   v
+-----------------------+        +--------------------+  +--------------------+    +-----------------------+
|     auth-service      |        |    farm-service    |  |   policy-service   |    | event-claims-service  |
|      Port: 8081       |        |     Port: 8082     |  |    Port: 8083      |    |      Port: 8084       |
| /api/auth/**          |        | /api/farmers/**    |  | /api/plans/**      |    | /api/readings/**      |
| /api/users/**         |        | /api/farms/**      |  | /api/admin/plans/**|    | /api/claims/**        |
| /api/admin/users/**   |        |                    |  | /api/applications/**|   |                       |
+-----------------------+        +--------------------+  | /api/underwriting/**|   +-----------------------+
                                           ^             | /api/policies/**   |                ^
                                           |             +--------------------+                |
                                           |                      |                            |
                                           +--- WebClient Call ---+                            |
                                           |    (Risk Profile)                                 |
                                           |                                                   |
                                           +----------------- WebClient (Active Policy) -------+
```

### Gateway Route Map

| Gateway Route ID | Predicate Path Patterns | Target Microservice Eureka ID | Default Port |
| :--- | :--- | :--- | :--- |
| `auth-service` | `/api/auth/**`, `/api/users/**`, `/api/admin/users/**` | `lb://AUTH-SERVICE` | `8081` |
| `farm-service` | `/api/farmers/**`, `/api/farms/**` | `lb://FARM-SERVICE` | `8082` |
| `policy-service` | `/api/plans/**`, `/api/applications/**`, `/api/policies/**`, `/api/internal/policies/**`, `/api/admin/plans/**`, `/api/admin/trigger-rules/**` | `lb://POLICY-SERVICE` | `8083` |
| `underwriter-service` | `/api/underwriting/**` | `lb://UNDERWRITER-SERVICE` (or `lb://POLICY-SERVICE`) | `8083` |
| `event-claims-service` | `/api/readings/**`, `/api/claims/**` | `lb://EVENT-CLAIMS-SERVICE` | `8084` |

### CORS Configuration
- **Allowed Origins:** `http://localhost:4200`
- **Allowed Methods:** `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- **Allowed Headers:** `*`
- **Allow Credentials:** `true`
- **Max Age:** `3600` seconds

---

## 2. Authentication & Authorization Model

### 2.1 Cookie Authentication Lifecycle
- **Cookie Name:** `IRRIGASHIELD_TOKEN`
- **Attributes:** `HttpOnly=true`, `SameSite=Lax`, `Path=/`, `Max-Age=3600` (1 hour)
- **Token Type:** Signed HMAC-SHA256 JWT
- **Public Endpoints (bypass token check):**
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/logout`
  - `GET /actuator/**`
  - All `OPTIONS` preflight requests

### 2.2 Roles & Permissions (RBAC)

| Role | Permitted Actions |
| :--- | :--- |
| `FARMER` | Profile registration, farm plot CRUD, crop management, irrigation profiles, view active plans, submit policy applications, review quotes, accept policies, view personal claims. |
| `UNDERWRITER` | View pending applications, start application reviews, run telemetry risk assessments, approve/reject applications with remarks. |
| `CLAIMS_OFFICER` | Review parametric claims, monitor payout audit logs, query internal policies. |
| `DATA_PROVIDER` | Ingest IoT moisture & irrigation readings (`POST /api/readings`), query internal policies. |
| `ADMIN` | Full superuser access: provision users across all roles, manage insurance plans, define parametric trigger rules and payout slabs. |

### 2.3 Bootstrap Accounts
- **Default Superadmin:**
  - Email: `admin@irrigashield.com`
  - Password: `Admin12345`
  - Role: `ADMIN`

---

## 3. Global Standards, Enums & Error Conventions

### 3.1 Global Enums

```typescript
// Roles
type Role = 'FARMER' | 'UNDERWRITER' | 'CLAIMS_OFFICER' | 'DATA_PROVIDER' | 'ADMIN';

// Land Ownership
type OwnershipType = 'OWNED' | 'LEASED' | 'SHARED';

// Crop Phenological Stages
type GrowthStage = 'SOWING' | 'GERMINATION' | 'VEGETATIVE' | 'FLOWERING' | 'FRUITING' | 'MATURITY';

// Irrigation Demand & Sources
type IrrigationRequirement = 'LOW' | 'MEDIUM' | 'HIGH';
type WaterSource = 'BOREWELL' | 'CANAL' | 'WELL' | 'RIVER' | 'RESERVOIR' | 'TANKER' | 'RAINWATER';
type BackupWaterSource = WaterSource | 'NONE';
type IrrigationMethod = 'DRIP' | 'SPRINKLER' | 'FLOOD' | 'FURROW' | 'MANUAL';
type EquipmentCondition = 'GOOD' | 'FAIR' | 'POOR';
type SeasonalRisk = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
type MaintenanceFrequency = 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'NONE';

// Workflow States
type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
type UnderwritingDecision = 'APPROVED' | 'REJECTED';
type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
type PaymentStatus = 'PAID' | 'PENDING' | 'FAILED';
type ClaimStatus = 'SETTLED' | 'NO_CLAIM';
type PayoutStatus = 'SUCCESS' | 'NOT_REQUIRED';
type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'VERY_HIGH';
```

### 3.2 Standard Error Response Payload (`ApiErrorResponse`)
Whenever any microservice encounters a client error or operational exception, it responds with the standardized JSON structure:

```json
{
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/farms",
  "timestamp": "2026-10-06T14:30:00",
  "validationErrors": {
    "postalCode": "Postal code must be a valid 6-digit PIN code",
    "areaInAcres": "Farm area must be greater than zero"
  }
}
```

---

## 4. Authentication & User Management APIs

### `POST /api/auth/register`
Self-service registration for farmers.

- **Access:** Public
- **Request Body:**
  ```json
  {
    "name": "Ramesh Patel",
    "email": "ramesh.patel@example.com",
    "password": "Password123"
  }
  ```
- **Validation Constraints:**
  - `name`: Not blank, 2–100 characters.
  - `email`: Not blank, valid RFC email, max 150 characters.
  - `password`: Not blank, 8–64 characters, regex `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$` (at least 1 uppercase, 1 lowercase, 1 digit).
- **Responses:**
  - `201 Created`:
    ```json
    {
      "userId": 2,
      "name": "Ramesh Patel",
      "email": "ramesh.patel@example.com",
      "role": "FARMER",
      "status": "ACTIVE"
    }
    ```
  - `409 Conflict`: `errorCode: "DUPLICATE_RESOURCE"` (Email already registered).
  - `400 Bad Request`: `errorCode: "VALIDATION_FAILED"`.

---

### `POST /api/auth/login`
Authenticates credentials and establishes session via HttpOnly cookie.

- **Access:** Public
- **Request Body:**
  ```json
  {
    "email": "ramesh.patel@example.com",
    "password": "Password123"
  }
  ```
- **Validation Constraints:**
  - `email`: Not blank, valid email.
  - `password`: Not blank.
- **Set-Cookie Header:**
  - `IRRIGASHIELD_TOKEN=<jwt-token>; Path=/; Max-Age=3600; HttpOnly; SameSite=Lax`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "tokenType": "Cookie",
      "expiresIn": 3600,
      "user": {
        "userId": 2,
        "name": "Ramesh Patel",
        "email": "ramesh.patel@example.com",
        "role": "FARMER",
        "status": "ACTIVE"
      }
    }
    ```
  - `401 Unauthorized`: `errorCode: "INVALID_CREDENTIALS"`.
  - `403 Forbidden`: `errorCode: "ACCOUNT_INACTIVE"`.

---

### `POST /api/auth/logout`
Destroys the authentication cookie session.

- **Access:** Public / Authenticated
- **Set-Cookie Header:**
  - `IRRIGASHIELD_TOKEN=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax`
- **Responses:**
  - `204 No Content`

---

### `GET /api/users/me`
Retrieves the logged-in user's profile and assigned roles from the current security context.

- **Access:** Authenticated (Any role)
- **Responses:**
  - `200 OK`:
    ```json
    {
      "userId": 2,
      "name": "Ramesh Patel",
      "email": "ramesh.patel@example.com",
      "role": "FARMER",
      "status": "ACTIVE"
    }
    ```
  - `401 Unauthorized`: `errorCode: "UNAUTHORIZED"`.

---

### `POST /api/admin/users`
Administrative provisioning of internal employees (Underwriters, Data Providers, Admins).

- **Access:** `ADMIN`
- **Request Body:**
  ```json
  {
    "name": "Dr. Sarah Rao",
    "email": "sarah.underwriter@irrigashield.com",
    "password": "SecurePassword1",
    "role": "UNDERWRITER"
  }
  ```
- **Validation Constraints:**
  - `name`: 2–100 characters.
  - `email`: valid email.
  - `password`: 8–64 characters (at least 1 upper, 1 lower, 1 digit).
  - `role`: One of `UNDERWRITER`, `CLAIMS_OFFICER`, `DATA_PROVIDER`, `ADMIN`.
- **Responses:**
  - `201 Created`: UserResponse object.
  - `409 Conflict`: `errorCode: "DUPLICATE_RESOURCE"`.

---

### `GET /api/admin/users`
Lists all user accounts currently registered in the system.

- **Access:** `ADMIN`
- **Responses:**
  - `200 OK`:
    ```json
    [
      {
        "userId": 1,
        "name": "Super Admin",
        "email": "admin@irrigashield.com",
        "role": "ADMIN",
        "status": "ACTIVE"
      },
      {
        "userId": 2,
        "name": "Ramesh Patel",
        "email": "ramesh.patel@example.com",
        "role": "FARMER",
        "status": "ACTIVE"
      }
    ]
    ```

---

## 5. Farm & Irrigation Management APIs

### `POST /api/farmers/profile`
Creates farmer verification details for the currently authenticated farmer.

- **Access:** `FARMER`
- **Request Body:**
  ```json
  {
    "fullName": "Ramesh Patel",
    "phoneNumber": "9876543210",
    "addressLine": "House 12, Gram Panchayat Rd",
    "village": "Baramati",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "413102",
    "bankAccountLastFour": "5678"
  }
  ```
- **Validation Constraints:**
  - `fullName`: 2–100 characters.
  - `phoneNumber`: Indian 10-digit format `^[6-9][0-9]{9}$`.
  - `postalCode`: 6-digit PIN code `^[1-9][0-9]{5}$`.
  - `bankAccountLastFour`: Exactly 4 digits or empty (`^$|^[0-9]{4}$`).
- **Responses:**
  - `201 Created`:
    ```json
    {
      "farmerId": 1,
      "authUserId": 2,
      "email": "ramesh.patel@example.com",
      "fullName": "Ramesh Patel",
      "phoneNumber": "9876543210",
      "addressLine": "House 12, Gram Panchayat Rd",
      "village": "Baramati",
      "district": "Pune",
      "state": "Maharashtra",
      "postalCode": "413102",
      "bankAccountLastFour": "5678",
      "verificationStatus": "VERIFIED",
      "createdAt": "2026-10-06T10:00:00",
      "updatedAt": "2026-10-06T10:00:00"
    }
    ```
  - `409 Conflict`: `errorCode: "PROFILE_ALREADY_EXISTS"`.

---

### `GET /api/farmers/profile/me`
Retrieves the logged-in farmer's saved profile.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: FarmerProfileResponse object.
  - `404 Not Found`: `errorCode: "FARMER_PROFILE_NOT_FOUND"`.

---

### `PUT /api/farmers/profile/me`
Updates contact and address details of the farmer profile.

- **Access:** `FARMER`
- **Request Body:** Same as FarmerProfileRequest.
- **Responses:**
  - `200 OK`: Updated FarmerProfileResponse object.

---

### `POST /api/farms`
Registers a new agricultural plot under the authenticated farmer.

- **Access:** `FARMER`
- **Request Body:**
  ```json
  {
    "farmName": "Krishna Valley Plot A",
    "surveyNumber": "SY-89/1A",
    "addressLine": "Near Nira Canal Branch",
    "village": "Baramati",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "413102",
    "areaInAcres": 12.50,
    "soilType": "Black Cotton Soil",
    "ownershipType": "OWNED",
    "latitude": 18.1512,
    "longitude": 74.5771
  }
  ```
- **Validation Constraints:**
  - `farmName`: 2–100 characters.
  - `areaInAcres`: Minimum `0.01`, max 2 decimal places.
  - `ownershipType`: Must match `^(OWNED|LEASED|SHARED)$`.
  - `latitude`: `-90.0` to `90.0`.
  - `longitude`: `-180.0` to `180.0`.
  - `postalCode`: Valid 6-digit PIN code.
- **Responses:**
  - `201 Created`:
    ```json
    {
      "farmId": 10,
      "farmerId": 1,
      "farmName": "Krishna Valley Plot A",
      "surveyNumber": "SY-89/1A",
      "addressLine": "Near Nira Canal Branch",
      "village": "Baramati",
      "district": "Pune",
      "state": "Maharashtra",
      "postalCode": "413102",
      "areaInAcres": 12.50,
      "soilType": "Black Cotton Soil",
      "ownershipType": "OWNED",
      "latitude": 18.1512,
      "longitude": 74.5771,
      "status": "ACTIVE",
      "createdAt": "2026-10-06T10:15:00",
      "updatedAt": "2026-10-06T10:15:00"
    }
    ```
  - `404 Not Found`: `errorCode: "FARMER_PROFILE_NOT_FOUND"` (profile must exist before adding farms).

---

### `GET /api/farms/my-farms`
Lists all farms registered by the authenticated farmer.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: Array of FarmResponse objects.

---

### `GET /api/farms/{farmId}`
Retrieves a specific farm by its identifier.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: FarmResponse object.
  - `404 Not Found`: `errorCode: "FARM_NOT_FOUND"`.

---

### `PUT /api/farms/{farmId}`
Updates farm details.

- **Access:** `FARMER`
- **Request Body:** Same as FarmRequest.
- **Responses:**
  - `200 OK`: Updated FarmResponse object.

---

### `POST /api/farms/{farmId}/crops`
Registers a crop cultivation cycle on a specific farm.

- **Access:** `FARMER`
- **Request Body:**
  ```json
  {
    "cropType": "Sugarcane",
    "variety": "Co 86032",
    "sowingDate": "2026-02-01",
    "expectedHarvestDate": "2026-12-15",
    "growthStage": "VEGETATIVE",
    "irrigationRequirement": "HIGH"
  }
  ```
- **Validation Constraints:**
  - `cropType`: 1–50 characters.
  - `sowingDate`: Must be past or present (`@PastOrPresent`).
  - `expectedHarvestDate`: Must be strictly in the future (`@Future`).
  - `growthStage`: `SOWING`, `GERMINATION`, `VEGETATIVE`, `FLOWERING`, `FRUITING`, `MATURITY`.
  - `irrigationRequirement`: `LOW`, `MEDIUM`, `HIGH`.
- **Responses:**
  - `201 Created`:
    ```json
    {
      "cropId": 101,
      "farmId": 10,
      "cropType": "Sugarcane",
      "variety": "Co 86032",
      "sowingDate": "2026-02-01",
      "expectedHarvestDate": "2026-12-15",
      "growthStage": "VEGETATIVE",
      "irrigationRequirement": "HIGH",
      "status": "ACTIVE",
      "createdAt": "2026-10-06T10:20:00",
      "updatedAt": "2026-10-06T10:20:00"
    }
    ```
  - `400 Bad Request`: `errorCode: "INVALID_HARVEST_DATE"` (harvest date before sowing date).

---

### `GET /api/farms/{farmId}/crops`
Lists all active crops registered for the specified farm.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: Array of CropResponse objects.

---

### `GET /api/farms/{farmId}/crops/{cropId}`
Fetches crop details.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: CropResponse object.
  - `404 Not Found`: `errorCode: "CROP_NOT_FOUND"`.

---

### `PUT /api/farms/{farmId}/crops/{cropId}`
Updates crop details.

- **Access:** `FARMER`
- **Request Body:** Same as CropRequest.
- **Responses:**
  - `200 OK`: Updated CropResponse object.

---

### `POST /api/farms/{farmId}/irrigation`
Configures the water infrastructure and historical reliability profile for a farm plot.

- **Access:** `FARMER`
- **Request Body:**
  ```json
  {
    "waterSource": "BOREWELL",
    "irrigationMethod": "DRIP",
    "equipmentCondition": "GOOD",
    "backupWaterSource": "CANAL",
    "historicalFailureCount": 1,
    "averageWaterAvailabilityPercentage": 85.50,
    "seasonalRisk": "LOW",
    "maintenanceFrequency": "QUARTERLY",
    "lastMaintenanceDate": "2026-09-15"
  }
  ```
- **Validation Constraints:**
  - `waterSource`: `BOREWELL`, `CANAL`, `WELL`, `RIVER`, `RESERVOIR`, `TANKER`, `RAINWATER`.
  - `irrigationMethod`: `DRIP`, `SPRINKLER`, `FLOOD`, `FURROW`, `MANUAL`.
  - `equipmentCondition`: `GOOD`, `FAIR`, `POOR`.
  - `backupWaterSource`: Any valid water source or `NONE`.
  - `historicalFailureCount`: $\ge 0$.
  - `averageWaterAvailabilityPercentage`: `0.0` to `100.0`.
  - `seasonalRisk`: `LOW`, `MEDIUM`, `HIGH`, `VERY_HIGH`.
  - `maintenanceFrequency`: `MONTHLY`, `QUARTERLY`, `HALF_YEARLY`, `YEARLY`, `NONE`.
  - `lastMaintenanceDate`: Past or present.
- **Responses:**
  - `201 Created`:
    ```json
    {
      "irrigationId": 50,
      "farmId": 10,
      "waterSource": "BOREWELL",
      "irrigationMethod": "DRIP",
      "equipmentCondition": "GOOD",
      "backupWaterSource": "CANAL",
      "historicalFailureCount": 1,
      "averageWaterAvailabilityPercentage": 85.50,
      "seasonalRisk": "LOW",
      "maintenanceFrequency": "QUARTERLY",
      "lastMaintenanceDate": "2026-09-15",
      "status": "ACTIVE",
      "createdAt": "2026-10-06T10:30:00",
      "updatedAt": "2026-10-06T10:30:00"
    }
    ```
  - `409 Conflict`: `errorCode: "IRRIGATION_PROFILE_EXISTS"`.

---

### `GET /api/farms/{farmId}/irrigation`
Retrieves irrigation profile of the specified farm.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: IrrigationProfileResponse object.
  - `404 Not Found`: `errorCode: "IRRIGATION_PROFILE_NOT_FOUND"`.

---

### `PUT /api/farms/{farmId}/irrigation`
Updates irrigation infrastructure details.

- **Access:** `FARMER`
- **Request Body:** Same as IrrigationProfileRequest.
- **Responses:**
  - `200 OK`: Updated IrrigationProfileResponse.

---

### `GET /api/farms/{farmId}/crops/{cropId}/application-validation`
Pre-flight check used by the frontend before policy submission to verify data completeness.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "farmId": 10,
      "cropId": 101,
      "farmerId": 1,
      "farmName": "Krishna Valley Plot A",
      "cropType": "Sugarcane",
      "growthStage": "VEGETATIVE",
      "irrigationProfileAvailable": true,
      "eligible": true
    }
    ```
  - `404 Not Found`: `errorCode: "FARM_NOT_FOUND"` or `"CROP_NOT_FOUND"`.

---

### `GET /api/farms/{farmId}/risk-profile`
Aggregates farm plot data, crop status, and irrigation infrastructure into a unified telemetry payload for the underwriting engine.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "farm": {
        "farmId": 10,
        "farmerId": 1,
        "farmName": "Krishna Valley Plot A",
        "district": "Pune",
        "state": "Maharashtra",
        "areaInAcres": 12.50,
        "soilType": "Black Cotton Soil",
        "ownershipType": "OWNED"
      },
      "crops": [
        {
          "cropId": 101,
          "cropType": "Sugarcane",
          "variety": "Co 86032",
          "growthStage": "VEGETATIVE",
          "irrigationRequirement": "HIGH"
        }
      ],
      "irrigationProfile": {
        "irrigationId": 50,
        "waterSource": "BOREWELL",
        "irrigationMethod": "DRIP",
        "equipmentCondition": "GOOD",
        "backupWaterSource": "CANAL",
        "historicalFailureCount": 1,
        "averageWaterAvailabilityPercentage": 85.50,
        "seasonalRisk": "LOW"
      }
    }
    ```

---

## 6. Insurance Plans & Trigger Configuration APIs

### `GET /api/plans/active`
Retrieves all publicly available active parametric insurance plans.

- **Access:** `FARMER`, `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`:
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

---

### `GET /api/plans/{planId}`
Retrieves a single insurance plan by ID.

- **Access:** `FARMER`, `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`: InsurancePlanResponse object.
  - `404 Not Found`: `errorCode: "PLAN_NOT_FOUND"`.

---

### `POST /api/admin/plans`
Creates a new parametric insurance product template.

- **Access:** `ADMIN`
- **Request Body:**
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
- **Validation Constraints:**
  - `planName`: 3–100 characters, unique.
  - `minimumCoverage`: $\ge 1000.00$.
  - `maximumCoverage`: $\ge 1000.00$. (Must be $\ge$ minimumCoverage).
  - `basePremium`: $\ge 1.00$.
  - `maximumPolicyDays`: $\ge 1$.
- **Responses:**
  - `201 Created`: InsurancePlanResponse object.
  - `409 Conflict`: `errorCode: "DUPLICATE_RESOURCE"`.

---

### `GET /api/admin/plans`
Retrieves all plans including inactive or archived plans.

- **Access:** `ADMIN`
- **Responses:**
  - `200 OK`: Array of InsurancePlanResponse.

---

### `POST /api/admin/plans/{planId}/trigger-rules`
Configures parametric index triggers for specific crops and growth stages within a plan.

- **Access:** `ADMIN`
- **Request Body:**
  ```json
  {
    "cropType": "Sugarcane",
    "growthStage": "VEGETATIVE",
    "availabilityThresholdPercentage": 70.00,
    "minimumContinuousHours": 48,
    "rollingWindowDays": 7,
    "requiredShortageDays": 3
  }
  ```
- **Validation Constraints:**
  - `availabilityThresholdPercentage`: `0.0` to `100.0`.
  - `minimumContinuousHours`: $\ge 1$.
  - `rollingWindowDays`: $\ge 1$.
  - `requiredShortageDays`: $\ge 1$ and $\le$ `rollingWindowDays`.
- **Responses:**
  - `201 Created`:
    ```json
    {
      "triggerRuleId": 10,
      "planId": 1,
      "cropType": "Sugarcane",
      "growthStage": "VEGETATIVE",
      "availabilityThresholdPercentage": 70.00,
      "minimumContinuousHours": 48,
      "rollingWindowDays": 7,
      "requiredShortageDays": 3,
      "status": "ACTIVE",
      "createdAt": "2026-10-06T11:00:00",
      "updatedAt": "2026-10-06T11:00:00"
    }
    ```
  - `400 Bad Request`: `errorCode: "INVALID_SHORTAGE_WINDOW"` (`requiredShortageDays` > `rollingWindowDays`).
  - `409 Conflict`: `errorCode: "TRIGGER_RULE_EXISTS"`.

---

### `GET /api/admin/plans/{planId}/trigger-rules`
Lists all trigger rules established under an insurance plan.

- **Access:** `ADMIN`
- **Responses:**
  - `200 OK`: Array of TriggerRuleResponse.

---

### `POST /api/admin/trigger-rules/{triggerRuleId}/payout-slabs`
Defines tiered indemnification payout percentages based on water shortage duration.

- **Access:** `ADMIN`
- **Request Body:**
  ```json
  {
    "minimumDurationHours": 48,
    "maximumDurationHours": 72,
    "payoutPercentage": 25.00
  }
  ```
- **Validation Constraints:**
  - `minimumDurationHours`: $\ge 1$.
  - `maximumDurationHours`: Optional; if present, must be $>$ `minimumDurationHours`.
  - `payoutPercentage`: `0.01` to `100.00`.
- **Responses:**
  - `201 Created`:
    ```json
    {
      "payoutSlabId": 201,
      "triggerRuleId": 10,
      "minimumDurationHours": 48,
      "maximumDurationHours": 72,
      "payoutPercentage": 25.00,
      "status": "ACTIVE",
      "createdAt": "2026-10-06T11:15:00",
      "updatedAt": "2026-10-06T11:15:00"
    }
    ```
  - `400 Bad Request`: `errorCode: "INVALID_DURATION_RANGE"` (`maximumDurationHours` $\le$ `minimumDurationHours`).
  - `409 Conflict`: `errorCode: "PAYOUT_SLAB_EXISTS"`.

---

### `GET /api/admin/trigger-rules/{triggerRuleId}/payout-slabs`
Lists all payout slabs under a trigger rule.

- **Access:** `ADMIN`
- **Responses:**
  - `200 OK`: Array of PayoutSlabResponse.

---

## 7. Policy Applications & Underwriting APIs

### `POST /api/applications`
Drafts a parametric policy application for a specific farm and crop.

- **Access:** `FARMER`
- **Request Body:**
  ```json
  {
    "farmId": 10,
    "cropId": 101,
    "planId": 1,
    "requestedCoverage": 200000.00,
    "proposedStartDate": "2026-11-01",
    "proposedEndDate": "2027-04-30"
  }
  ```
- **Validation Constraints:**
  - `farmId`, `cropId`, `planId`: Positive integer IDs.
  - `requestedCoverage`: $\ge 1000.00$ and within plan `[minimumCoverage, maximumCoverage]`.
  - `proposedStartDate`: Future or present date.
  - `proposedEndDate`: Strict future date. Coverage duration cannot exceed plan `maximumPolicyDays`.
- **Responses:**
  - `201 Created`:
    ```json
    {
      "applicationId": 501,
      "farmerUserId": 2,
      "farmerEmail": "ramesh.patel@example.com",
      "farmId": 10,
      "cropId": 101,
      "planId": 1,
      "requestedCoverage": 200000.00,
      "proposedStartDate": "2026-11-01",
      "proposedEndDate": "2027-04-30",
      "status": "DRAFT",
      "submittedAt": null,
      "createdAt": "2026-10-06T11:30:00",
      "updatedAt": "2026-10-06T11:30:00"
    }
    ```
  - `400 Bad Request`: `errorCode: "COVERAGE_OUT_OF_BOUNDS"` or `"POLICY_DURATION_EXCEEDED"`.
  - `404 Not Found`: `errorCode: "PLAN_NOT_FOUND"`.

---

### `GET /api/applications/my-applications`
Retrieves all policy applications submitted or drafted by the authenticated farmer.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: Array of PolicyApplicationResponse objects.

---

### `GET /api/applications/{applicationId}`
Retrieves application details.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: PolicyApplicationResponse object.
  - `404 Not Found`: `errorCode: "APPLICATION_NOT_FOUND"`.

---

### `PUT /api/applications/{applicationId}`
Updates an unsubmitted application while in `DRAFT` state.

- **Access:** `FARMER`
- **Request Body:** Same as PolicyApplicationRequest.
- **Responses:**
  - `200 OK`: Updated PolicyApplicationResponse.
  - `409 Conflict`: `errorCode: "APPLICATION_NOT_EDITABLE"` (if already submitted).

---

### `POST /api/applications/{applicationId}/submit`
Transitions application state from `DRAFT` to `SUBMITTED`, verifying farm eligibility and placing the application into the underwriter review queue.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "applicationId": 501,
      "farmerUserId": 2,
      "farmerEmail": "ramesh.patel@example.com",
      "farmId": 10,
      "cropId": 101,
      "planId": 1,
      "requestedCoverage": 200000.00,
      "proposedStartDate": "2026-11-01",
      "proposedEndDate": "2027-04-30",
      "status": "SUBMITTED",
      "submittedAt": "2026-10-06T11:45:00",
      "createdAt": "2026-10-06T11:30:00",
      "updatedAt": "2026-10-06T11:45:00"
    }
    ```
  - `409 Conflict`: `errorCode: "APPLICATION_ALREADY_SUBMITTED"`.
  - `409 Conflict`: `errorCode: "FARM_PROFILE_INCOMPLETE"` (if irrigation profile or crop is missing in `farm-service`).

---

### `GET /api/underwriting/applications/pending`
Underwriter work queue containing all submitted applications awaiting review.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`: Array of UnderwritingResponse objects with `status: "SUBMITTED"`.

---

### `GET /api/underwriting/my-cases`
Returns historical and in-progress cases claimed by the logged-in underwriter.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`: Array of UnderwritingResponse objects.

---

### `POST /api/underwriting/applications/{applicationId}/start-review`
Claims the application, creating or activating the underwriter case and changing state to `UNDER_REVIEW`.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "caseId": 301,
      "applicationId": 501,
      "underwriterUserId": 5,
      "underwriterEmail": "sarah.underwriter@irrigashield.com",
      "status": "UNDER_REVIEW",
      "reviewStartedAt": "2026-10-06T12:00:00"
    }
    ```
  - `409 Conflict`: `errorCode: "REVIEW_ALREADY_STARTED"` (claimed by another underwriter).
  - `409 Conflict`: `errorCode: "APPLICATION_NOT_SUBMITTED"`.

---

### `POST /api/underwriting/applications/{applicationId}/assess`
Executes automated mathematical telemetry risk scoring against `farm-service` infrastructure.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "caseId": 301,
      "applicationId": 501,
      "underwriterUserId": 5,
      "underwriterEmail": "sarah.underwriter@irrigashield.com",
      "waterAvailabilityScore": 0,
      "equipmentConditionScore": 0,
      "historicalFailureScore": 10,
      "seasonalRiskScore": 0,
      "backupSourceScore": 0,
      "overallRiskScore": 10,
      "riskLevel": "LOW",
      "basePremium": 2500.00,
      "riskLoading": 125.00,
      "recommendedPremium": 2625.00,
      "recommendedCoverage": 200000.00,
      "status": "ASSESSED",
      "reviewStartedAt": "2026-10-06T12:00:00"
    }
    ```
  - `409 Conflict`: `errorCode: "REVIEW_NOT_STARTED"`.
  - `403 Forbidden`: `errorCode: "CASE_NOT_ASSIGNED"`.

---

### `PUT /api/underwriting/applications/{applicationId}/approve`
Formally approves the application with mandatory underwriter rationale.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Request Body:**
  ```json
  {
    "remarks": "Low drought risk profile, reliable drip equipment with backup canal access."
  }
  ```
- **Validation Constraints:**
  - `remarks`: 5 to 500 characters.
- **Responses:**
  - `200 OK`: UnderwritingResponse with `decision: "APPROVED"`, `status: "COMPLETED"`, `applicationStatus: "APPROVED"`.
  - `409 Conflict`: `errorCode: "ASSESSMENT_REQUIRED"` (must execute `/assess` before approving).
  - `409 Conflict`: `errorCode: "CASE_ALREADY_COMPLETED"`.

---

### `PUT /api/underwriting/applications/{applicationId}/reject`
Formally rejects the application with mandatory rejection rationale.

- **Access:** `UNDERWRITER`, `ADMIN`
- **Request Body:**
  ```json
  {
    "remarks": "Excessive historical pump failure rate and high water table depletion risk."
  }
  ```
- **Responses:**
  - `200 OK`: UnderwritingResponse with `decision: "REJECTED"`, `status: "COMPLETED"`, `applicationStatus: "REJECTED"`.

---

## 8. Policy Issuance & Active Policies APIs

### `GET /api/policies/approved-quote/{applicationId}`
Enables the farmer to inspect the final underwriter-approved coverage and premium offer before committing.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "policyId": null,
      "applicationId": 501,
      "farmerUserId": 2,
      "farmerEmail": "ramesh.patel@example.com",
      "farmId": 10,
      "cropId": 101,
      "planId": 1,
      "planName": "Sugarcane Drought Shield",
      "coverageAmount": 200000.00,
      "premiumAmount": 2625.00,
      "startDate": "2026-11-01",
      "endDate": "2027-04-30",
      "paymentStatus": "PAID",
      "status": "APPROVED",
      "acceptedAt": null
    }
    ```
  - `409 Conflict`: `errorCode: "APPLICATION_NOT_APPROVED"`.

---

### `POST /api/policies/approved-quote/{applicationId}/accept`
Confirms quote acceptance, creates an active parametric policy contract, and binds coverage.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "policyId": 1001,
      "applicationId": 501,
      "farmerUserId": 2,
      "farmerEmail": "ramesh.patel@example.com",
      "farmId": 10,
      "cropId": 101,
      "planId": 1,
      "planName": "Sugarcane Drought Shield",
      "coverageAmount": 200000.00,
      "premiumAmount": 2625.00,
      "startDate": "2026-11-01",
      "endDate": "2027-04-30",
      "paymentStatus": "PAID",
      "status": "ACTIVE",
      "acceptedAt": "2026-10-06T12:30:00"
    }
    ```
  - `409 Conflict`: `errorCode: "POLICY_ALREADY_EXISTS"`.
  - `409 Conflict`: `errorCode: "APPLICATION_NOT_APPROVED"`.

---

### `GET /api/policies/my-policies`
Retrieves all policies held by the authenticated farmer.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: Array of PolicyResponse objects.

---

### `GET /api/internal/policies/{policyId}/claim-configuration`
Consolidated service-to-service endpoint consumed by `event-claims-service` during telemetry evaluation. Retrieves policy parameters, the active `TriggerRule` (with configured threshold), and all active `PayoutSlab` records for dynamic compensation calculation.

- **Access:** `DATA_PROVIDER`, `CLAIMS_OFFICER`, `ADMIN`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "policyId": 1001,
      "farmerUserId": 2,
      "farmId": 10,
      "cropId": 101,
      "planId": 1,
      "coverageAmount": 200000.00,
      "startDate": "2026-11-01",
      "endDate": "2027-04-30",
      "status": "ACTIVE",
      "triggerRule": {
        "triggerRuleId": 201,
        "planId": 1,
        "cropType": "SUGARCANE",
        "growthStage": "VEGETATIVE",
        "availabilityThresholdPercentage": 70.00,
        "minimumContinuousHours": 24,
        "rollingWindowDays": 7,
        "requiredShortageDays": 3,
        "status": "ACTIVE"
      },
      "payoutSlabs": [
        {
          "payoutSlabId": 301,
          "triggerRuleId": 201,
          "minimumDurationHours": 0,
          "maximumDurationHours": null,
          "minAvailabilityPct": 0.00,
          "maxAvailabilityPct": 30.00,
          "payoutPercentage": 50.00,
          "status": "ACTIVE"
        },
        {
          "payoutSlabId": 302,
          "triggerRuleId": 201,
          "minimumDurationHours": 0,
          "maximumDurationHours": null,
          "minAvailabilityPct": 30.00,
          "maxAvailabilityPct": 50.00,
          "payoutPercentage": 25.00,
          "status": "ACTIVE"
        },
        {
          "payoutSlabId": 303,
          "triggerRuleId": 201,
          "minimumDurationHours": 0,
          "maximumDurationHours": null,
          "minAvailabilityPct": 50.00,
          "maxAvailabilityPct": 70.00,
          "payoutPercentage": 10.00,
          "status": "ACTIVE"
        }
      ]
    }
    ```
  - `404 Not Found`: `errorCode: "ACTIVE_POLICY_NOT_FOUND"`.

---

### `GET /api/internal/policies/{policyId}`
Internal service-to-service endpoint for verifying active policy details and coverage amounts.

- **Access:** `DATA_PROVIDER`, `CLAIMS_OFFICER`, `ADMIN`
- **Responses:**
  - `200 OK`:
    ```json
    {
      "policyId": 1001,
      "farmerUserId": 2,
      "farmId": 10,
      "cropId": 101,
      "planId": 1,
      "coverageAmount": 200000.00,
      "startDate": "2026-11-01",
      "endDate": "2027-04-30",
      "status": "ACTIVE"
    }
    ```
  - `404 Not Found`: `errorCode: "ACTIVE_POLICY_NOT_FOUND"`.

---

## 9. Telemetry Ingestion & Parametric Claims APIs

### `POST /api/readings`
Ingests telemetry readings from IoT soil moisture sensors, weather stations, or canal water gauges. Automatically triggers indemnity compensation if parametric deficit thresholds are breached.

- **Access:** `DATA_PROVIDER`
- **Request Body:**
  ```json
  {
    "policyId": 1001,
    "availabilityPercentage": 24.50,
    "recordedAt": "2026-11-15T14:30:00"
  }
  ```
- **Validation Constraints:**
  - `policyId`: Required positive integer.
  - `availabilityPercentage`: Required decimal between `0.0` and `100.0`.
  - `recordedAt`: Optional (defaults to current server time). Must not be in the future.
- **Responses:**
  - `201 Created` *(Threshold Breached — $< 70\%$ availability)*:
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
      "recordedAt": "2026-11-15T14:30:00"
    }
    ```
  - `201 Created` *(Normal Reading — $\ge 70\%$ availability)*:
    ```json
    {
      "readingId": 8002,
      "claimId": null,
      "policyId": 1001,
      "farmId": 10,
      "availabilityPercentage": 82.00,
      "thresholdBreached": false,
      "payoutPercentage": 0.00,
      "compensationAmount": 0.00,
      "payoutStatus": "NOT_REQUIRED",
      "claimStatus": "NO_CLAIM",
      "recordedAt": "2026-11-16T09:00:00"
    }
    ```
  - `400 Bad Request`: `errorCode: "FUTURE_READING_NOT_ALLOWED"` (reading timestamp is in future).
  - `400 Bad Request`: `errorCode: "READING_OUTSIDE_POLICY_PERIOD"` (reading date is before policy start or after policy end).
  - `404 Not Found`: `errorCode: "ACTIVE_POLICY_NOT_FOUND"`.
  - `409 Conflict`: `errorCode: "EVENT_CLAIM_ALREADY_EXISTS"` (a settled claim already exists for this policy on this event date).
  - `400 Bad Request`: `errorCode: "COVERAGE_EXHAUSTED"` (total policy aggregate coverage has been fully paid out).

---

### `GET /api/claims/my-claims`
Retrieves settled parametric claims and payout logs credited to the authenticated farmer.

- **Access:** `FARMER`
- **Responses:**
  - `200 OK`: Array of ClaimResponse objects.

---

## 10. Business Rules, Scoring & Settlement Engines

### 10.1 Underwriting Risk Calculator Logic
When an underwriter evaluates an application via `POST /api/underwriting/applications/{applicationId}/assess`, the system executes deterministic scoring across 5 factors:

$$\text{Overall Score} = S_{\text{avail}} + S_{\text{equip}} + S_{\text{fail}} + S_{\text{season}} + S_{\text{backup}}$$

| Dimension | Measured Metric | Value Range | Points |
| :--- | :--- | :--- | :---: |
| **Water Availability ($S_{\text{avail}}$)** | Average availability % | $\ge 85\%$ <br> $70\% - 84.99\%$ <br> $50\% - 69.99\%$ <br> $< 50\%$ | 0 <br> 10 <br> 20 <br> 30 |
| **Equipment Condition ($S_{\text{equip}}$)** | Pump & delivery hardware | `GOOD` <br> `FAIR` <br> `POOR` | 0 <br> 10 <br> 20 |
| **Historical Failure ($S_{\text{fail}}$)** | Count of pump/source failures | $0$ <br> $1 - 2$ <br> $> 2$ | 0 <br> 10 <br> 20 |
| **Seasonal Drought Risk ($S_{\text{season}}$)** | Regional forecast risk tier | `LOW` <br> `MEDIUM` <br> `HIGH` <br> `VERY_HIGH` | 0 <br> 10 <br> 15 <br> 20 |
| **Backup Source ($S_{\text{backup}}$)** | Redundant water channel | Canal / Well / Tanker <br> `NONE` / Empty | 0 <br> 10 |

#### Premium Loading Matrix

| Overall Score | Risk Level | Premium Loading % | Final Recommended Premium Formula |
| :---: | :---: | :---: | :--- |
| **0 – 25** | `LOW` | **5%** | $\text{Base Premium} \times 1.05$ |
| **26 – 50** | `MODERATE` | **15%** | $\text{Base Premium} \times 1.15$ |
| **51 – 75** | `HIGH` | **30%** | $\text{Base Premium} \times 1.30$ |
| **76 – 100** | `VERY_HIGH` | **50%** | $\text{Base Premium} \times 1.50$ |

#### Recommended Coverage Clamping
$$\text{Recommended Coverage} = \min(\text{Max Coverage}, \max(\text{Min Coverage}, \text{Requested Coverage}))$$

---

### 10.2 Parametric Deficit Settlement Tiers
When IoT water telemetry is ingested:
- If Availability $\ge 70\% \implies$ Normal operation (`NO_CLAIM`).
- If Availability $< 70\%$ and policy has no existing settled claim:

| Water Availability % | Payout Tier % | Indemnity Calculation |
| :---: | :---: | :--- |
| $< 30\%$ | **50%** | $\text{Coverage Amount} \times 0.50$ |
| $30\% - 49.99\%$ | **25%** | $\text{Coverage Amount} \times 0.25$ |
| $50\% - 69.99\%$ | **10%** | $\text{Coverage Amount} \times 0.10$ |

---

## 11. Master Error Codes Reference

| Error Code | HTTP Status | Description / Root Cause |
| :--- | :---: | :--- |
| `UNAUTHORIZED` | `401` | Missing, expired, or invalid `IRRIGASHIELD_TOKEN` cookie. |
| `FORBIDDEN` | `403` | User does not possess the required role for this endpoint. |
| `INVALID_CREDENTIALS` | `401` | Provided email and password do not match any active user. |
| `ACCOUNT_INACTIVE` | `403` | The requested user account is suspended or inactive. |
| `DUPLICATE_RESOURCE` | `409` | Database unique constraint violation (e.g. duplicate email, unique plan name). |
| `VALIDATION_FAILED` | `400` | Input DTO failed `@Valid` Bean Validation constraints. |
| `FARMER_PROFILE_NOT_FOUND` | `404` | Farmer profile has not been initialized for the authenticated user. |
| `PROFILE_ALREADY_EXISTS` | `409` | Farmer profile is already registered for this user ID. |
| `FARM_NOT_FOUND` | `404` | Farm ID does not exist or does not belong to the calling farmer. |
| `CROP_NOT_FOUND` | `404` | Crop ID does not exist on the specified farm. |
| `INVALID_HARVEST_DATE` | `400` | Crop harvest date is before or equal to sowing date. |
| `IRRIGATION_PROFILE_EXISTS` | `409` | Irrigation profile already exists for this farm. Use `PUT` to update. |
| `IRRIGATION_PROFILE_NOT_FOUND`| `404` | Farm lacks an irrigation infrastructure profile. |
| `PLAN_NOT_FOUND` | `404` | Insurance plan ID not found in database. |
| `INVALID_SHORTAGE_WINDOW` | `400` | Trigger rule `requiredShortageDays` exceeds `rollingWindowDays`. |
| `TRIGGER_RULE_EXISTS` | `409` | Trigger rule already configured for this plan, crop, and growth stage. |
| `INVALID_DURATION_RANGE` | `400` | Payout slab `maximumDurationHours` is less than or equal to `minimumDurationHours`. |
| `PAYOUT_SLAB_EXISTS` | `409` | Payout slab for the specified duration tier already exists. |
| `COVERAGE_OUT_OF_BOUNDS` | `400` | Requested coverage is below plan minimum or exceeds plan maximum. |
| `POLICY_DURATION_EXCEEDED` | `400` | Application dates span more days than allowed by `maximumPolicyDays`. |
| `APPLICATION_NOT_FOUND` | `404` | Policy application ID does not exist. |
| `APPLICATION_NOT_EDITABLE` | `409` | Application is not in `DRAFT` status and cannot be modified. |
| `APPLICATION_ALREADY_SUBMITTED`| `409` | Application has already been submitted for underwriting. |
| `FARM_PROFILE_INCOMPLETE` | `409` | Farm, crop, or irrigation profile missing required setup before applying. |
| `REVIEW_ALREADY_STARTED` | `409` | Application is already claimed and under review by another underwriter. |
| `APPLICATION_NOT_SUBMITTED` | `409` | Application must be in `SUBMITTED` status before starting review. |
| `REVIEW_NOT_STARTED` | `409` | Underwriting review must be started (`/start-review`) before assessment. |
| `CASE_NOT_ASSIGNED` | `403` | Case is assigned to a different underwriter. |
| `CASE_ALREADY_COMPLETED` | `409` | Underwriting review has already been finalized with an approve/reject decision. |
| `ASSESSMENT_REQUIRED` | `409` | Automated telemetry risk scoring (`/assess`) must run before making a decision. |
| `APPLICATION_NOT_APPROVED` | `409` | Cannot view or accept quote for an application that has not been approved. |
| `QUOTE_INCOMPLETE` | `409` | Approved application lacks underwriting case pricing parameters. |
| `POLICY_ALREADY_EXISTS` | `409` | Active policy already created for this approved application. |
| `ACTIVE_POLICY_NOT_FOUND` | `404` | Active policy does not exist for the specified policy ID. |
| `FUTURE_READING_NOT_ALLOWED` | `400` | Sensor reading recorded timestamp is in the future. |
| `READING_OUTSIDE_POLICY_PERIOD`| `400` | Sensor reading recorded date falls outside the active policy start/end window. |
| `EVENT_CLAIM_ALREADY_EXISTS` | `409` | Parametric claim has already been settled for this policy on this event date. |
| `COVERAGE_EXHAUSTED` | `400` | Total policy aggregate coverage limit has already been fully paid out. |
| `INVALID_AVAILABILITY_RANGE` | `400` | Maximum availability percentage cannot be below minimum availability percentage. |
| `SETTLED_CLAIM_ALREADY_EXISTS` | `409` | Parametric claim has already been triggered and settled for this policy. |
| `INTERNAL_SERVER_ERROR` | `500` | Unhandled runtime server exception. |

