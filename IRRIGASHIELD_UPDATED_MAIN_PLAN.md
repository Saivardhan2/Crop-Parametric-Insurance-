# IrrigaShield: Master Project Plan & Architecture Blueprint
**Parametric Agricultural Micro-Insurance Platform**

> **Document Version:** 2.0.0 (Master Synchronized Plan)  
> **Baseline Artifacts:** `IrrigaShield_main_plan.xlsx` (All 15 Sheets) & `Capstone-IrrigaSheild` (Spring Boot Reactive Backend)  
> **Status:** Complete Master Reference & Implementation Blueprint  
> **Generated:** October 2026  

---

## Table of Contents
1. [Executive Summary & Architectural Modernization](#1-executive-summary--architectural-modernization)
2. [System Topography & Reactive Microservices Architecture](#2-system-topography--reactive-microservices-architecture)
3. [Security Architecture & Gateway Authentication](#3-security-architecture--gateway-authentication)
4. [Global Domain Enums, Constants & Conventions](#4-global-domain-enums-constants--conventions)
5. [Master End-to-End Lifecycle: All 75 Stages (`over_all`)](#5-master-end-to-end-lifecycle-all-75-stages-over_all)
6. [Persona-Driven Execution Plans](#6-persona-driven-execution-plans)
   - [6.1 Farmer Persona Plan (`farmer`)](#61-farmer-persona-plan-farmer)
   - [6.2 Underwriter Persona Plan (`underwriter`)](#62-underwriter-persona-plan-underwriter)
   - [6.3 Claims Officer Persona Plan (`claims_officer`)](#63-claims-officer-persona-plan-claims_officer)
   - [6.4 Data Provider Persona Plan (`data_provider`)](#64-data-provider-persona-plan-data_provider)
   - [6.5 Administrator Persona Plan (`admin`)](#65-administrator-persona-plan-admin)
7. [Domain Service Specifications](#7-domain-service-specifications)
   - [7.1 Policy & Underwriting Domain (`policy`)](#71-policy--underwriting-domain-policy)
   - [7.2 Parametric Telemetry Domain (`parametric_event`)](#72-parametric-telemetry-domain-parametric_event)
   - [7.3 Claims Processing Domain (`claims`)](#73-claims-processing-domain-claims)
   - [7.4 Payment & Payout Domain (`payment`)](#74-payment--payout-domain-payment)
8. [Database Architecture & Microservice Mappings (`Db_mappings`)](#8-database-architecture--microservice-mappings-db_mappings)
9. [Comprehensive Data Dictionary & Production Schemas (`schemas`)](#9-comprehensive-data-dictionary--production-schemas-schemas)
10. [Capstone Evaluation & Technical Competency Matrix (`Evaluation_Mapping`)](#10-capstone-evaluation--technical-competency-matrix-evaluation_mapping)
11. [Live Demonstration Script & End-to-End Walkthrough (`Demo_Journey`)](#11-live-demonstration-script--end-to-end-walkthrough-demo_journey)
12. [Architectural Principles & Governance Notes (`Notes`)](#12-architectural-principles--governance-notes-notes)
13. [Algorithmic Engines & Business Logic Specifications](#13-algorithmic-engines--business-logic-specifications)
   - [13.1 Underwriting Risk Calculator Engine](#131-underwriting-risk-calculator-engine)
   - [13.2 Parametric Deficit Settlement Engine](#132-parametric-deficit-settlement-engine)
14. [Frontend Integration Architecture (Angular 21 + NgRx)](#14-frontend-integration-architecture-angular-21--ngrx)
15. [Backend Improvements & Future Roadmap](#15-backend-improvements--future-roadmap)

---

## 1. Executive Summary & Architectural Modernization

### 1.1 Platform Mission
**IrrigaShield** is an enterprise-grade parametric micro-insurance platform that safeguards smallholder and commercial farmers against catastrophic yield loss caused by irrigation deficits, canal supply shortfalls, groundwater depletion, and equipment downtime.

Unlike traditional indemnity insurance—which relies on subjective damage assessments, adjusters traveling to remote fields, extensive paperwork, and claims disputes lasting months—IrrigaShield operates on **objective, verifiable parametric triggers**. When authorized telemetry providers submit water availability indices that breach contractually defined thresholds during critical crop growth stages, the platform **automatically validates eligibility, calculates indemnity compensation, and executes settlement without manual intervention**.

### 1.2 Evolution: Planned vs. Implemented Architecture
In the original concept plan (`IrrigaShield_main_plan.xlsx`), the system was conceived as 8 discrete synchronous microservices communicating with traditional blocking JPA/Hibernate databases. During backend development (`Capstone-IrrigaSheild`), this architecture was re-engineered into **4 high-performance reactive domain microservices** running on **Spring WebFlux, Project Reactor, Netty, and MySQL R2DBC**, coordinated by **Spring Cloud API Gateway** and **Netflix Eureka**.

| Dimension | Original Planned Design (`IrrigaShield_main_plan.xlsx`) | Implemented Backend Reality (`Capstone-IrrigaSheild`) | Engineering Rationale |
| :--- | :--- | :--- | :--- |
| **Concurrency & Runtime** | Synchronous Spring MVC, blocking thread-per-request | **Reactive Spring WebFlux, Project Reactor (`Mono`/`Flux`), Netty event loop** | High-throughput non-blocking I/O capable of handling thousands of concurrent telemetry sensor submissions with minimal memory footprint. |
| **Database Access** | Blocking Spring Data JPA / Hibernate | **Spring Data R2DBC (`asyncer-r2dbc-mysql`)** | True end-to-end reactive pipeline from HTTP request to database driver without thread starvation. |
| **Microservice Count** | 8 microservices (Auth, Farm, Underwriter, Policy, Event, Claims, Payment, Notification) | **4 Domain Services + 2 Platform Services** (`api-gateway`, `eureka-server`, `auth-service`, `farm-service`, `policy-service`, `event-claims-service`) | Consolidated tightly coupled subdomains (Underwriting into Policy; Events, Claims & Payouts into Event-Claims) eliminating redundant network hops and distributed 2PC transactions. |
| **Security & Identity** | Header-based JWT / RBAC | **HttpOnly Cookie (`IRRIGASHIELD_TOKEN`) + Gateway Header Injection (`X-User-Id`, `X-User-Email`, `X-User-Roles`)** | Immune to browser Cross-Site Scripting (XSS) token theft while microservices remain stateless, verifying incoming requests via `TrustedHeaderAuthenticationFilter`. |
| **Inter-Service Communication** | OpenFeign / Synchronous REST | **Reactive Spring `WebClient`** | Non-blocking reactive RPC for cross-service risk profile queries and policy state verification. |
| **Claim Settlement Workflow** | Multi-step manual claims queue with human adjuster reviews | **Straight-Through Processing (STP) Parametric Engine** | Instant, algorithmic compensation calculation and payout clearance directly upon telemetry deficit detection. |

---

## 2. System Topography & Reactive Microservices Architecture

### 2.1 Reactive Architecture Topology
```mermaid
flowchart TD
    Client["Frontend Web Client (Angular 21 + Tailwind)<br/>Port 4200 (Credentials: Include)"] -->|HTTP Cookie: IRRIGASHIELD_TOKEN| Gateway["API Gateway (Spring Cloud Gateway Reactive)<br/>Port 8080"]
    Gateway -.->|Service Discovery| Eureka["Eureka Service Registry<br/>Port 8761"]
    
    subgraph Platform Services [Platform & Infrastructure]
        Eureka
        Gateway
    end
    
    subgraph Core Domain Microservices [Reactive Domain Microservices (Netty + R2DBC)]
        Auth["auth-service (Port 8081)<br/>Users & Identity"]
        Farm["farm-service (Port 8082)<br/>Profiles, Farms, Crops & Irrigation"]
        Policy["policy-service (Port 8083)<br/>Plans, Trigger Rules, Applications & Underwriting"]
        EventClaims["event-claims-service (Port 8084)<br/>Telemetry, Parametric Engine, Claims & Payouts"]
    end
    
    subgraph Databases [Isolated R2DBC MySQL Databases]
        DB_Auth[("irrigashield_auth_db<br/>MySQL 3306")]
        DB_Farm[("irrigashield_farm_db<br/>MySQL 3306")]
        DB_Policy[("irrigashield_policy_db<br/>MySQL 3306")]
        DB_Claims[("irrigashield_event_claims_db<br/>MySQL 3306")]
    end
    
    Gateway -->|/api/auth/**, /api/users/**, /api/admin/users/**| Auth
    Gateway -->|/api/farmers/**, /api/farms/**| Farm
    Gateway -->|/api/plans/**, /api/applications/**, /api/policies/**, /api/admin/plans/**, /api/underwriting/**| Policy
    Gateway -->|/api/readings/**, /api/claims/**| EventClaims
    
    Auth --> DB_Auth
    Farm --> DB_Farm
    Policy --> DB_Policy
    EventClaims --> DB_Claims
    
    Policy -.->|WebClient: GET /api/farms/{farmId}/risk-profile| Farm
    EventClaims -.->|WebClient: GET /api/internal/policies/{policyId}/claim-configuration| Policy
```

### 2.2 Microservice Port & Registry Directory
| Service Name | Eureka Service ID | Port | Context Path | Primary Database | Key Responsibilities |
| :--- | :--- | :---: | :--- | :--- | :--- |
| **Eureka Server** | `EUREKA-SERVER` | `8761` | `/` | N/A | Central dynamic service registration and health heartbeats. |
| **API Gateway** | `API-GATEWAY` | `8080` | `/` | N/A | Single entrypoint, CORS termination, JWT cookie extraction, trusted header propagation, route dispatching. |
| **Auth Service** | `AUTH-SERVICE` | `8081` | `/` | `irrigashield_auth_db` | User registration, authentication, JWT cookie generation, admin user management. |
| **Farm Service** | `FARM-SERVICE` | `8082` | `/` | `irrigashield_farm_db` | Farmer KYC profiles, farm land parcels, crop phenology, irrigation equipment profiling, risk profile extraction. |
| **Policy Service** | `POLICY-SERVICE` | `8083` | `/` | `irrigashield_policy_db` | Insurance plans, trigger rules, payout slabs, policy applications, underwriting risk engine (`RiskCalculator`), quote acceptance, active policies. |
| **Event Claims Service** | `EVENT-CLAIMS-SERVICE` | `8084` | `/` | `irrigashield_event_claims_db` | Telemetry sensor ingestion, parametric deficit evaluation, automated claim generation, straight-through payout calculation and settlement. |

---

## 3. Security Architecture & Gateway Authentication

### 3.1 Security Model Principles
IrrigaShield uses a **zero-trust boundary security model**:
1. **Browser Security (XSS Defense):** Authentication tokens are stored strictly in an `HttpOnly`, `SameSite=Lax`, `Path=/` cookie named `IRRIGASHIELD_TOKEN`. JavaScript client code running in the browser cannot access the raw JWT, completely eliminating token theft via Cross-Site Scripting (XSS).
2. **Gateway Verification:** The `api-gateway` reactive filter (`JwtAuthenticationFilter`) intercepts every incoming HTTP request, validates the cryptographic signature of the cookie, extracts claims (`userId`, `email`, `role`), and strips any untrusted external `X-User-*` headers.
3. **Downstream Header Injection:** The Gateway injects trusted internal headers before forwarding the request to internal microservices:
   - `X-User-Id: <Long>` (e.g., `1`)
   - `X-User-Email: <String>` (e.g., `farmer@irrigashield.com`)
   - `X-User-Roles: <String>` (e.g., `ROLE_FARMER`)
4. **Service-Level Authorization:** Each downstream microservice applies `TrustedHeaderAuthenticationFilter` which parses these trusted headers and populates Spring's `ReactiveSecurityContextHolder` with an `AuthenticatedUser` principal. Method-level security is enforced via `@PreAuthorize("hasRole('...')")`.

### 3.2 Pre-Configured Bootstrap Demonstration Accounts
| Role | Email | Password | Allowed Capabilities |
| :--- | :--- | :--- | :--- |
| **FARMER** | `farmer@irrigashield.com` | `Farmer@123` | Farm registration, crop/irrigation setup, plan browsing, policy application, quote acceptance, claims tracking. |
| **UNDERWRITER** | `underwriter@irrigashield.com` | `Underwriter@123` | Queue inspection, farm risk profiling, algorithmic risk assessment, application approval/rejection. |
| **DATA_PROVIDER** | `dataprovider@irrigashield.com` | `Provider@123` | Sensor telemetry data submission (`POST /api/readings`). |
| **CLAIMS_OFFICER** | `claims@irrigashield.com` | `Claims@123` | Claims auditing, exception dispute management, manual payout escalation. |
| **ADMIN** | `admin@irrigashield.com` | `Admin@123` | User account creation, insurance plan configuration, trigger rule and payout slab management. |

---

## 4. Global Domain Enums, Constants & Conventions

### 4.1 System Enums
```typescript
// Platform Persona Roles
type Role = 'FARMER' | 'UNDERWRITER' | 'CLAIMS_OFFICER' | 'DATA_PROVIDER' | 'ADMIN';

// Farm & Land Ownership
type OwnershipType = 'OWNED' | 'LEASED' | 'SHARED';

// Crop Phenological Stages
type GrowthStage = 'SOWING' | 'GERMINATION' | 'VEGETATIVE' | 'FLOWERING' | 'FRUITING' | 'MATURITY';

// Irrigation Infrastructure & Hydrological Source
type IrrigationRequirement = 'LOW' | 'MEDIUM' | 'HIGH';
type WaterSource = 'BOREWELL' | 'CANAL' | 'WELL' | 'RIVER' | 'RESERVOIR' | 'TANKER' | 'RAINWATER';
type BackupWaterSource = WaterSource | 'NONE';
type IrrigationMethod = 'DRIP' | 'SPRINKLER' | 'FLOOD' | 'FURROW' | 'MANUAL';
type EquipmentCondition = 'GOOD' | 'FAIR' | 'POOR';
type SeasonalRisk = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
type MaintenanceFrequency = 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'NONE';

// Underwriting & Policy Application Lifecycle
type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
type UnderwritingDecision = 'APPROVED' | 'REJECTED';
type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED';
type PaymentStatus = 'PAID' | 'PENDING' | 'FAILED';

// Telemetry & Parametric Claims Settlement
type ClaimStatus = 'SETTLED' | 'NO_CLAIM';
type PayoutStatus = 'SUCCESS' | 'NOT_REQUIRED';
type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'VERY_HIGH';
```

### 4.2 Standard Reactive API Error Response Envelope (`ApiErrorResponse`)
All microservices adhere to a consistent error schema handled by `@RestControllerAdvice`:
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

## 5. Master End-to-End Lifecycle: All 75 Stages (`over_all`)

This master table correlates every single stage from the initial planning spreadsheet (`IrrigaShield_main_plan.xlsx`) with the active reactive microservices implementation in `Capstone-IrrigaSheild`.

| Stage | Persona | Action | Goal | Angular Component | Planned API (Excel) | Implemented Backend API | Microservice & Database Table | Status & Implementation Details |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Farmer | **Register** | Create farmer account | `Register Component` | `POST /auth/register` | `POST /api/auth/register` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Accepts RegisterRequest(name, email, password, role). Validates unique email, hashes password with BCrypt, stores user with status=ACTIVE, role=FARMER. |
| **2** | Farmer | **Login** | Authenticate into platform | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Authenticates credentials. Generates signed JWT and sets HttpOnly cookie IRRIGASHIELD_TOKEN (Path=/, MaxAge=24h, SameSite=Lax). Returns AuthResponse. |
| **3** | Farmer | **View Dashboard** | See farms, applications, policies, claims and notifications | `Farmer Dashboard` | `GET /farmers/me/dashboard` | `GET /api/farmers/profile/me + /api/farms/my-farms + /api/applications/my-applications` | `farm-service (8082) & policy-service (8083)`<br/>`irrigashield_farm_db & irrigashield_policy_db` | **[STREAMLINED]**<br/>Composite farmer dashboard aggregated on frontend via parallel reactive calls using NgRx effects across Farm and Policy services. |
| **4** | Farmer | **Create Farmer Profile** | Provide personal and bank details | `Farmer Profile Component` | `POST /farmers/profile` | `POST /api/farmers/profile` | `farm-service (Port 8082)`<br/>`irrigashield_farm_db.farmer_profiles` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Accepts FarmerProfileRequest (fullName, phoneNumber, addressLine, village, district, state, postalCode, bankAccountLastFour). Also supports GET/PUT /api/farmers/profile/me. |
| **5** | Farmer | **Register Farm** | Add insured farm details | `Farm Form Component` | `POST /farms` | `POST /api/farms` | `farm-service (Port 8082)`<br/>`irrigashield_farm_db.farms` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Accepts FarmRequest (farmName, surveyNumber, address, areaInAcres, soilType, ownershipType, lat/lng). Status set to ACTIVE. |
| **6** | Farmer | **Add Crop Details** | Record crop and growth-stage information | `Crop Form Component` | `POST /farms/{farmId}/crops` | `POST /api/farms/{farmId}/crops` | `farm-service (Port 8082)`<br/>`irrigashield_farm_db.crops` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Validates farm ownership. Accepts CropRequest (cropType, variety, sowingDate, expectedHarvestDate, growthStage, irrigationRequirement). |
| **7** | Farmer | **Add Irrigation Details** | Record water source and irrigation method | `Irrigation Form Component` | `POST /farms/{farmId}/irrigation` | `POST /api/farms/{farmId}/irrigation` | `farm-service (Port 8082)`<br/>`irrigashield_farm_db.irrigation_profiles` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Accepts IrrigationProfileRequest (waterSource, irrigationMethod, equipmentCondition, backupWaterSource, historicalFailureCount, averageWaterAvailabilityPct, seasonalRisk, maintenanceFrequency). |
| **8** | Farmer | **View Farms** | Review registered farms and crop details | `My Farms Component` | `GET /farms/my-farms` | `GET /api/farms/my-farms` | `farm-service (Port 8082)`<br/>`irrigashield_farm_db.farms` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Returns Flux<FarmResponse> for authenticated farmer resolved from X-User-Id header. |
| **9** | Farmer | **Browse Insurance Plans** | Compare coverage, trigger and premium rules | `Plans Component` | `GET /plans/active` | `GET /api/plans/active` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.insurance_plans` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Returns Flux<InsurancePlanResponse> containing planId, planName, description, minCoverage, maxCoverage, basePremium, status=ACTIVE. |
| **10** | Farmer | **Start Policy Application** | Begin IrrigaShield application | `Policy Application Component` | `POST /applications` | `POST /api/applications` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Accepts PolicyApplicationRequest (farmId, cropId, planId, requestedCoverage, proposedStartDate, proposedEndDate). Pre-checks eligibility via farm-service client. Creates application in DRAFT status. |
| **11** | Farmer | **Select Farm and Crop** | Associate application with insured exposure | `Policy Application Component` | `PUT /applications/{id}/farm-crop` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[CONSOLIDATED]**<br/>Associates/updates farm, crop, plan, coverage or dates in single reactive PUT endpoint. Status must be DRAFT. |
| **12** | Farmer | **Select Plan** | Choose desired insurance plan | `Plan Selection Component` | `PUT /applications/{id}/plan` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[CONSOLIDATED]**<br/>Consolidated into PUT /api/applications/{applicationId}. Farmer selects plan and coverage in unified wizard. |
| **13** | Farmer | **Review Application** | Verify application before submission | `Application Review Component` | `GET /applications/{id}` | `GET /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Returns complete application details, requested coverage, dates, and current lifecycle status. |
| **14** | Farmer | **Submit Application** | Send application for underwriting | `Application Review Component` | `POST /applications/{id}/submit` | `POST /api/applications/{applicationId}/submit` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Validates application is in DRAFT. Transitions status to SUBMITTED, sets submittedAt=now(). Emits case into underwriting pending pool. |
| **15** | Farmer | **Track Application** | View underwriting status and information requests | `Application Status Component` | `GET /applications/{id}/status` | `GET /api/applications/my-applications` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]**<br/>Farmer tracks all submitted applications and their live status (DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED). |
| **16** | Farmer | **Respond to Information Request** | Provide missing underwriting information | `Additional Information Component` | `POST /applications/{id}/additional-info` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[CONSOLIDATED]**<br/>Underwriter requests adjustments via case remarks; farmer updates application parameters directly. |
| **17** | Underwriter | **Login** | Authenticate into underwriting portal | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Underwriter authenticates with ROLE_UNDERWRITER. Receives secure HttpOnly cookie IRRIGASHIELD_TOKEN. |
| **18** | Underwriter | **View Pending Applications** | Review submitted applications | `Underwriter Dashboard` | `GET /underwriting/applications/pending` | `GET /api/underwriting/applications/pending` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]**<br/>Requires ROLE_UNDERWRITER. Returns all applications in SUBMITTED status awaiting underwriter review. |
| **19** | Underwriter | **View Application Details** | Review farmer, farm, crop and policy inputs | `Application Details Component` | `GET /underwriting/applications/{id}` | `POST /api/underwriting/applications/{applicationId}/start-review` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[IMPLEMENTED]**<br/>Assigns case to authenticated underwriter, transitions status to UNDER_REVIEW, creates UnderwritingCase record. |
| **20** | Underwriter Service | **Fetch Farm Risk Inputs** | Retrieve farm, crop and irrigation information | `Risk Review Component` | `GET /farms/{farmId}/risk-profile` | `GET /api/farms/{farmId}/risk-profile` | `farm-service (Port 8082)`<br/>`irrigashield_farm_db.farms, crops, irrigation_profiles` | **[IMPLEMENTED]**<br/>Called via reactive WebClient by policy-service or directly by Underwriter. Aggregates farm acreage, crops, soil, water source, and failure count. |
| **21** | Underwriter Service | **Calculate Risk Score** | Apply configurable rule-based risk assessment | `Risk Assessment Component` | `POST /underwriting/applications/{id}/assess-risk` | `POST /api/underwriting/applications/{applicationId}/assess` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[IMPLEMENTED]**<br/>Consolidated algorithmic risk engine. Executes RiskCalculator.assess(): availabilityScore + equipmentScore + failureScore + seasonalScore + backupScore = totalScore (0-100). Sets riskLevel (LOW, MODERATE, HIGH, VERY_HIGH). |
| **22** | Underwriter | **Review Risk Assessment** | Review factors, score and risk classification | `Risk Review Component` | `GET /underwriting/applications/{id}/assessment` | `GET /api/underwriting/my-cases` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[IMPLEMENTED]**<br/>Underwriter views all assigned cases with factor scores, risk level, recommended premium, and recommended coverage. |
| **23** | Underwriter Service | **Recommend Premium** | Calculate base premium and risk loading | `Premium Recommendation Component` | `POST /underwriting/applications/{id}/recommend-premium` | `POST /api/underwriting/applications/{applicationId}/assess` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[CONSOLIDATED]**<br/>Computed atomically during assessment: recommendedPremium = basePremium + (basePremium * riskLoadingPercentage). Loading: LOW=5%, MODERATE=15%, HIGH=30%, VERY_HIGH=50%. |
| **24** | Underwriter Service | **Recommend Policy Terms** | Recommend coverage, threshold and duration rules | `Terms Recommendation Component` | `POST /underwriting/applications/{id}/recommend-terms` | `POST /api/underwriting/applications/{applicationId}/assess` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[CONSOLIDATED]**<br/>Computed atomically during assessment: recommendedCoverage = min(max(requestedCoverage, minCoverage), maxCoverage). |
| **25** | Underwriter | **Request Additional Information** | Ask farmer for missing or inconsistent data | `Application Details Component` | `POST /underwriting/applications/{id}/request-info` | `PUT /api/underwriting/applications/{applicationId}/reject` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[CONSOLIDATED]**<br/>Remarks in DecisionRequest communicate revision/rejection requirements back to the applicant. |
| **26** | Underwriter | **Approve Application** | Approve application and quoted terms | `Underwriting Decision Component` | `PUT /underwriting/applications/{id}/approve` | `PUT /api/underwriting/applications/{applicationId}/approve` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases & policy_applications` | **[IMPLEMENTED]**<br/>Requires ROLE_UNDERWRITER. Accepts DecisionRequest(remarks). Marks case APPROVED, application APPROVED. Unlocks quote for farmer. |
| **27** | Underwriter | **Reject Application** | Reject ineligible or unacceptable risk | `Underwriting Decision Component` | `PUT /underwriting/applications/{id}/reject` | `PUT /api/underwriting/applications/{applicationId}/reject` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases & policy_applications` | **[IMPLEMENTED]**<br/>Requires ROLE_UNDERWRITER. Accepts DecisionRequest(remarks). Marks case REJECTED, application REJECTED. |
| **28** | Farmer | **View Underwriting Decision** | Review approval, coverage and quoted premium | `Application Status Component` | `GET /applications/{id}/decision` | `GET /api/policies/approved-quote/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Retrieves approved quote details (approved coverage, premium amount, validity dates) for farmer acceptance. |
| **29** | Farmer | **Accept Quote** | Accept approved premium and policy terms | `Quote Component` | `POST /applications/{id}/accept-quote` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies & policy_applications` | **[IMPLEMENTED]**<br/>Farmer accepts quote. Triggers atomic policy creation, simulated premium payment clearance, and sets policy status=ACTIVE. |
| **30** | Farmer | **Pay Premium** | Make simulated premium payment | `Premium Payment Component` | `POST /payments/premiums` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[STREAMLINED]**<br/>Atomic straight-through premium payment simulation executed directly upon quote acceptance; sets payment_status=PAID. |
| **31** | Farmer | **View Payment Status** | Check premium transaction result | `Payment Status Component` | `GET /payments/{paymentId}` | `GET /api/policies/approved-quote/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[STREAMLINED]**<br/>Payment status verified directly in PolicyResponse (paymentStatus=PAID). |
| **32** | Payment Service | **Confirm Premium Payment** | Notify Policy Service of successful payment | `Payment Status Component` | `POST /policies/{applicationId}/payment-confirmation` | `Internal Reactive Flow` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]**<br/>No cross-network payment-confirmation call required; premium payment clearance and policy creation are executed in an atomic reactive pipeline. |
| **33** | Policy Service | **Create Policy** | Create policy from approved application | `Policy Details Component` | `POST /policies` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]**<br/>Creates policy record with policyId, applicationId, farmerUserId, farmerEmail, farmId, cropId, planId, coverageAmount, premiumAmount, startDate, endDate. |
| **34** | Policy Service | **Activate Policy** | Activate policy after successful premium payment | `Policy Details Component` | `PUT /policies/{policyId}/activate` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]**<br/>Sets policy status immediately to ACTIVE upon quote acceptance and payment confirmation. |
| **35** | Farmer | **View Active Policy** | View coverage, exclusions and trigger rules | `Policy Details Component` | `GET /policies/{policyId}` | `GET /api/policies/my-policies` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Returns Flux<PolicyResponse> of all active and historical policies owned by farmer. |
| **36** | Farmer | **View Trigger Conditions** | Understand threshold, duration and payout slabs | `Policy Coverage Component` | `GET /policies/{policyId}/trigger-rules` | `GET /api/plans/{planId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.trigger_rules & payout_slabs` | **[IMPLEMENTED]**<br/>Farmer inspects associated plan trigger rules (threshold %, continuous hours) and payout slabs. |
| **37** | Data Provider | **Login** | Authenticate authorized external data provider | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Data provider authenticates with ROLE_DATA_PROVIDER. Cookie IRRIGASHIELD_TOKEN generated. |
| **38** | Data Provider | **Submit Irrigation Reading** | Send simulated verified irrigation availability data | `Data Provider Simulator` | `POST /parametric-data/readings` | `POST /api/readings` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[IMPLEMENTED]**<br/>Requires ROLE_DATA_PROVIDER. Accepts IrrigationReadingRequest(policyId, availabilityPercentage, recordedAt). Validates 0-100% and not in future. |
| **39** | Parametric Event Service | **Validate Reading** | Validate source, ranges, timestamp and farm mapping | `Data Validation View` | `POST /parametric-data/readings/{id}/validate` | `POST /api/readings (Internal Validation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]**<br/>Reading timestamp, range boundaries, and active status validated synchronously in reactive pipeline before DB write. |
| **40** | Parametric Event Service | **Find Active Policy** | Resolve policy applicable at reading timestamp | `Event Evaluation View` | `GET /policies/active?farmId={farmId}&at={timestamp}` | `GET /api/internal/policies/{policyId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[IMPLEMENTED]**<br/>Inter-service non-blocking WebClient call from event-claims-service. Validates policy exists, is ACTIVE, and reading timestamp falls between startDate and endDate. |
| **41** | Parametric Event Service | **Evaluate Threshold** | Compare availability index with policy threshold | `Event Evaluation View` | `POST /parametric-events/evaluate/{readingId}` | `POST /api/readings (Evaluation Pipeline)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]**<br/>Threshold evaluation: If availability >= 70.00%, returns NO_CLAIM / NOT_REQUIRED. If < 70.00%, enters parametric deficit settlement. |
| **42** | Parametric Event Service | **Track Continuous Disruption** | Maintain breach start, recovery and duration | `Disruption Status Component` | `PUT /parametric-events/trackers/{policyId}` | `POST /api/readings (Disruption Tracking)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]**<br/>Straight-through deficit evaluation directly captures and timestamps telemetry breaches on the active policy. |
| **43** | Parametric Event Service | **Evaluate Cumulative Shortage** | Evaluate shortage inside configured crop-stage window | `Disruption Status Component` | `POST /parametric-events/policies/{policyId}/evaluate-window` | `POST /api/readings (Shortage Evaluation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]**<br/>Phenological stage validation executed via active policy window check. |
| **44** | Parametric Event Service | **Create Verified Event** | Create event when all policy conditions are met | `Parametric Event Details` | `POST /parametric-events` | `POST /api/readings (Verified Event)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings & claims` | **[CONSOLIDATED]**<br/>Reading breach triggers verified event and directly instantiates claim in atomic reactive pipeline. |
| **45** | Parametric Event Service | **Prevent Duplicate Event** | Ensure idempotent event generation per disruption | `Parametric Event Details` | `GET /parametric-events/duplicates?policyId={policyId}` | `POST /api/readings (Idempotency Check)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[IMPLEMENTED]**<br/>Checks claimRepository.existsByPolicyIdAndStatus(policyId, 'SETTLED'). If true, returns HTTP 409 SETTLED_CLAIM_ALREADY_EXISTS to prevent double payout. |
| **46** | Farmer | **View Irrigation Data** | View provider readings for insured farm | `Irrigation Status Component` | `GET /parametric-data/farms/{farmId}/readings` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[STREAMLINED]**<br/>Farmer views readingId, availabilityPercentage, and breach metrics embedded in ClaimResponse. |
| **47** | Farmer | **View Parametric Event** | View verified trigger details | `Parametric Event Details` | `GET /parametric-events/{eventId}` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[STREAMLINED]**<br/>Farmer inspects parametric trigger event and calculation details in ClaimResponse. |
| **48** | Claims Service | **Auto Create Claim** | Create claim from verified parametric event | `Claim Details Component` | `POST /claims/from-event` | `POST /api/readings (Auto Claim Creation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Auto-creates Claim record linked to readingId, policyId, farmerUserId, farmId. |
| **49** | Claims Service | **Validate Claim Eligibility** | Verify active policy, coverage, source and event period | `Claim Validation Component` | `POST /claims/{claimId}/validate` | `POST /api/readings (Eligibility Validation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Validated against policy dates and authorization before claim record creation. |
| **50** | Claims Service | **Check Duplicate Claim** | Prevent repeated claim for same event | `Claim Validation Component` | `GET /claims/duplicate?eventId={eventId}` | `POST /api/readings (Duplicate Prevention)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Guaranteed via claimRepository idempotency check and unique constraint on reading_id. |
| **51** | Claims Service | **Calculate Compensation** | Apply policy payout slab and maximum limit | `Compensation Component` | `POST /claims/{claimId}/calculate-compensation` | `POST /api/readings (Compensation Calculation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[IMPLEMENTED]**<br/>Parametric deficit engine: <30% availability -> 50% payout; <50% availability -> 25% payout; <70% availability -> 10% payout. compensationAmount = coverageAmount * payoutPercentage / 100. |
| **52** | Claims Service | **Auto Approve Normal Claim** | Approve claim when all straight-through rules pass | `Claim Decision Component` | `PUT /claims/{claimId}/auto-approve` | `POST /api/readings (Auto Approve)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Straight-through parametric processing: claim auto-approved and marked SETTLED immediately upon deficit validation. |
| **53** | Claims Service | **Route Exception to Manual Review** | Flag anomalies and failed validations | `Claim Review Component` | `PUT /claims/{claimId}/manual-review` | `Exception Routing (Audit Roadmap)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]**<br/>Straight-through automation settles valid claims instantly. Anomaly/fraud flag exception queue documented for Phase 2. |
| **54** | Claims Officer | **Login** | Authenticate into claims portal | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Claims officer authenticates with ROLE_CLAIMS_OFFICER. Receives HttpOnly cookie. |
| **55** | Claims Officer | **View Review Queue** | See claims requiring manual review | `Claims Officer Dashboard` | `GET /claims/manual-review` | `GET /api/claims/manual-review` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]**<br/>Manual review queue for contested/disputed claims, prioritized in Phase 2 roadmap. |
| **56** | Claims Officer | **View Claim Details** | Review policy, event, readings and compensation | `Claim Review Component` | `GET /claims/{claimId}` | `GET /api/claims/{claimId}` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]**<br/>Detailed claims audit inspection endpoint for Claims Officer review. |
| **57** | Claims Officer | **Approve Exception Claim** | Approve eligible manually reviewed claim | `Claim Decision Component` | `PUT /claims/{claimId}/approve` | `PUT /api/claims/{claimId}/approve` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]**<br/>Manual approval override for contested claims. |
| **58** | Claims Officer | **Reject Exception Claim** | Reject failed or anomalous claim | `Claim Decision Component` | `PUT /claims/{claimId}/reject` | `PUT /api/claims/{claimId}/reject` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]**<br/>Manual rejection of anomalous or fraudulent claim submissions. |
| **59** | Claims Service | **Create Payout Instruction** | Send approved compensation for payment | `Settlement Component` | `POST /payouts/instructions` | `POST /api/readings (Settlement Pipeline)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Payout instruction generated and executed synchronously within reactive transaction. |
| **60** | Payment Service | **Process Claim Payout** | Simulate transfer of approved compensation | `Payout Status Component` | `POST /payouts` | `POST /api/readings (Payout Processing)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Payout simulation completes immediately: sets payout_status=SUCCESS. |
| **61** | Payment Service | **Report Payout Result** | Update claim with transaction status | `Payout Status Component` | `PUT /claims/{claimId}/payout-status` | `POST /api/readings (Payout Reporting)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Transaction status reflected in claim record without inter-service latency. |
| **62** | Claims Service | **Mark Claim Settled** | Close claim following successful payout | `Claim Details Component` | `PUT /claims/{claimId}/settle` | `POST /api/readings (Settlement Completion)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]**<br/>Claim record updated with status=SETTLED, settled_at=now(). |
| **63** | Farmer | **Track Claim** | View claim validation, approval and payout status | `Claims Dashboard` | `GET /claims/my-claims` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[IMPLEMENTED]**<br/>Requires ROLE_FARMER. Returns Flux<ClaimResponse> containing claimId, readingId, policyId, availability %, payout %, compensationAmount, payoutStatus, status, createdAt. |
| **64** | Farmer | **View Settlement** | View compensation and transaction details | `Settlement Details Component` | `GET /claims/{claimId}/settlement` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[STREAMLINED]**<br/>Compensation and payout amounts surfaced directly in ClaimResponse. |
| **65** | Notification Service | **Create Notification** | Store policy, trigger, claim and payment notification | `Notification Panel` | `POST /notifications` | `Domain Lifecycle Events` | `All Microservices`<br/>`Audit Logs / Status Fields` | **[CONSOLIDATED]**<br/>State transitions in application, policy, and claims provide real-time status. Dedicated WebSocket/SSE notification bus in roadmap. |
| **66** | Farmer | **View Notifications** | Read application, event, claim and payout updates | `Notification Panel` | `GET /notifications/me` | `Dashboard Live Status Polling / SSE` | `Frontend / Gateway`<br/>`Domain State` | **[STREAMLINED]**<br/>Farmer UI reflects live application, policy, and claim status immediately upon state change. |
| **67** | Admin | **Login** | Authenticate into administration portal | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Administrator authenticates with ROLE_ADMIN. Cookie IRRIGASHIELD_TOKEN generated. |
| **68** | Admin | **Manage Users and Roles** | Create users, assign roles and change status | `User Management Component` | `GET/POST/PUT /admin/users` | `POST /api/admin/users & GET /api/admin/users` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[IMPLEMENTED]**<br/>Requires ROLE_ADMIN. Create and list platform users across all roles (FARMER, UNDERWRITER, DATA_PROVIDER, CLAIMS_OFFICER, ADMIN). |
| **69** | Admin | **Configure Insurance Plans** | Manage coverage and base pricing rules | `Plan Management Component` | `GET/POST/PUT /admin/plans` | `POST /api/admin/plans & GET /api/admin/plans` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.insurance_plans` | **[IMPLEMENTED]**<br/>Requires ROLE_ADMIN. Accepts InsurancePlanRequest(planName, description, minCoverage, maxCoverage, basePremium, status). |
| **70** | Admin | **Configure Trigger Rules** | Manage crop-stage thresholds, durations and payout slabs | `Trigger Rule Component` | `GET/POST/PUT /admin/trigger-rules` | `POST /api/admin/plans/{planId}/trigger-rules & /payout-slabs` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.trigger_rules & payout_slabs` | **[IMPLEMENTED]**<br/>Requires ROLE_ADMIN. Configures crop-stage thresholds, continuous hours, rolling window days, and duration payout tiers. |
| **71** | Admin | **Manage Data Providers** | Authorize or suspend external providers | `Provider Management Component` | `GET/POST/PUT /admin/providers` | `POST /api/admin/users (role=DATA_PROVIDER)` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[CONSOLIDATED]**<br/>Data providers are managed and authorized through Admin User Management API. |
| **72** | Admin | **View Policy Statistics** | Monitor applications, active policies and premiums | `Admin Dashboard` | `GET /admin/policies/stats` | `Admin Dashboard / Health Actuator` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[ROADMAP]**<br/>Aggregated policy stats planned for Phase 2; operational metrics exposed via Spring Boot Actuator /actuator/metrics. |
| **73** | Admin | **View Event Statistics** | Monitor readings, disruptions and trigger events | `Admin Dashboard` | `GET /admin/parametric-events/stats` | `Admin Dashboard / Health Actuator` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[ROADMAP]**<br/>Event telemetry stats planned for Phase 2; telemetry throughput exposed via Actuator. |
| **74** | Admin | **View Claim Statistics** | Monitor claims, approvals and settlements | `Admin Dashboard` | `GET /admin/claims/stats` | `Admin Dashboard / Health Actuator` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]**<br/>Claims loss ratio statistics planned for Phase 2. |
| **75** | Admin | **View Payment Statistics** | Monitor premium and payout transactions | `Admin Dashboard` | `GET /admin/payments/stats` | `Admin Dashboard / Health Actuator` | `policy-service & event-claims-service`<br/>`irrigashield_policy_db & claims_db` | **[ROADMAP]**<br/>Financial premium vs payout settlement stats planned for Phase 2. |

---

## 6. Persona-Driven Execution Plans

### 6.1 Farmer Persona Plan (`farmer`)
The **Farmer** is the central consumer of parametric protection. Farmers self-register, manage multi-parcel land holdings, profile irrigation assets, apply for coverage, accept algorithmic quotes, and track automated straight-through claim settlements.

| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **1** | **Register** | Create farmer account | `Register Component` | `POST /auth/register` | `POST /api/auth/register` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **2** | **Login** | Authenticate into platform | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **3** | **View Dashboard** | See farms, applications, policies, claims and notifications | `Farmer Dashboard` | `GET /farmers/me/dashboard` | `GET /api/farmers/profile/me + /api/farms/my-farms + /api/applications/my-applications` | `farm-service (8082) & policy-service (8083)` | **[STREAMLINED]** |
| **4** | **Create Farmer Profile** | Provide personal and bank details | `Farmer Profile Component` | `POST /farmers/profile` | `POST /api/farmers/profile` | `farm-service (Port 8082)` | **[IMPLEMENTED]** |
| **5** | **Register Farm** | Add insured farm details | `Farm Form Component` | `POST /farms` | `POST /api/farms` | `farm-service (Port 8082)` | **[IMPLEMENTED]** |
| **6** | **Add Crop Details** | Record crop and growth-stage information | `Crop Form Component` | `POST /farms/{farmId}/crops` | `POST /api/farms/{farmId}/crops` | `farm-service (Port 8082)` | **[IMPLEMENTED]** |
| **7** | **Add Irrigation Details** | Record water source and irrigation method | `Irrigation Form Component` | `POST /farms/{farmId}/irrigation` | `POST /api/farms/{farmId}/irrigation` | `farm-service (Port 8082)` | **[IMPLEMENTED]** |
| **8** | **View Farms** | Review registered farms and crop details | `My Farms Component` | `GET /farms/my-farms` | `GET /api/farms/my-farms` | `farm-service (Port 8082)` | **[IMPLEMENTED]** |
| **9** | **Browse Insurance Plans** | Compare coverage, trigger and premium rules | `Plans Component` | `GET /plans/active` | `GET /api/plans/active` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **10** | **Start Policy Application** | Begin IrrigaShield application | `Policy Application Component` | `POST /applications` | `POST /api/applications` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **11** | **Select Farm and Crop** | Associate application with insured exposure | `Policy Application Component` | `PUT /applications/{id}/farm-crop` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)` | **[CONSOLIDATED]** |
| **12** | **Select Plan** | Choose desired insurance plan | `Plan Selection Component` | `PUT /applications/{id}/plan` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)` | **[CONSOLIDATED]** |
| **13** | **Review Application** | Verify application before submission | `Application Review Component` | `GET /applications/{id}` | `GET /api/applications/{applicationId}` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **14** | **Submit Application** | Send application for underwriting | `Application Review Component` | `POST /applications/{id}/submit` | `POST /api/applications/{applicationId}/submit` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **15** | **Track Application** | View underwriting status and information requests | `Application Status Component` | `GET /applications/{id}/status` | `GET /api/applications/my-applications` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **16** | **Respond to Information Request** | Provide missing underwriting information | `Additional Information Component` | `POST /applications/{id}/additional-info` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)` | **[CONSOLIDATED]** |
| **28** | **View Underwriting Decision** | Review approval, coverage and quoted premium | `Application Status Component` | `GET /applications/{id}/decision` | `GET /api/policies/approved-quote/{applicationId}` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **29** | **Accept Quote** | Accept approved premium and policy terms | `Quote Component` | `POST /applications/{id}/accept-quote` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **30** | **Pay Premium** | Make simulated premium payment | `Premium Payment Component` | `POST /payments/premiums` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)` | **[STREAMLINED]** |
| **31** | **View Payment Status** | Check premium transaction result | `Payment Status Component` | `GET /payments/{paymentId}` | `GET /api/policies/approved-quote/{applicationId}` | `policy-service (Port 8083)` | **[STREAMLINED]** |
| **35** | **View Active Policy** | View coverage, exclusions and trigger rules | `Policy Details Component` | `GET /policies/{policyId}` | `GET /api/policies/my-policies` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **36** | **View Trigger Conditions** | Understand threshold, duration and payout slabs | `Policy Coverage Component` | `GET /policies/{policyId}/trigger-rules` | `GET /api/plans/{planId}` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **46** | **View Irrigation Data** | View provider readings for insured farm | `Irrigation Status Component` | `GET /parametric-data/farms/{farmId}/readings` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)` | **[STREAMLINED]** |
| **47** | **View Parametric Event** | View verified trigger details | `Parametric Event Details` | `GET /parametric-events/{eventId}` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)` | **[STREAMLINED]** |
| **63** | **Track Claim** | View claim validation, approval and payout status | `Claims Dashboard` | `GET /claims/my-claims` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)` | **[IMPLEMENTED]** |
| **64** | **View Settlement** | View compensation and transaction details | `Settlement Details Component` | `GET /claims/{claimId}/settlement` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)` | **[STREAMLINED]** |
| **66** | **View Notifications** | Read application, event, claim and payout updates | `Notification Panel` | `GET /notifications/me` | `Dashboard Live Status Polling / SSE` | `Frontend / Gateway` | **[STREAMLINED]** |

### 6.2 Underwriter Persona Plan (`underwriter`)
The **Underwriter** evaluates incoming policy applications. In the updated architecture, Underwriting is integrated directly into `policy-service` under `/api/underwriting/**`. Underwriters inspect pending queues, trigger the automated risk scoring engine (`RiskCalculator`), review recommendations, and render binding decisions.

| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **17** | **Login** | Authenticate into underwriting portal | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **18** | **View Pending Applications** | Review submitted applications | `Underwriter Dashboard` | `GET /underwriting/applications/pending` | `GET /api/underwriting/applications/pending` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **19** | **View Application Details** | Review farmer, farm, crop and policy inputs | `Application Details Component` | `GET /underwriting/applications/{id}` | `POST /api/underwriting/applications/{applicationId}/start-review` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **22** | **Review Risk Assessment** | Review factors, score and risk classification | `Risk Review Component` | `GET /underwriting/applications/{id}/assessment` | `GET /api/underwriting/my-cases` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **25** | **Request Additional Information** | Ask farmer for missing or inconsistent data | `Application Details Component` | `POST /underwriting/applications/{id}/request-info` | `PUT /api/underwriting/applications/{applicationId}/reject` | `policy-service (Port 8083)` | **[CONSOLIDATED]** |
| **26** | **Approve Application** | Approve application and quoted terms | `Underwriting Decision Component` | `PUT /underwriting/applications/{id}/approve` | `PUT /api/underwriting/applications/{applicationId}/approve` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **27** | **Reject Application** | Reject ineligible or unacceptable risk | `Underwriting Decision Component` | `PUT /underwriting/applications/{id}/reject` | `PUT /api/underwriting/applications/{applicationId}/reject` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |

### 6.3 Claims Officer Persona Plan (`claims_officer`)
Under pure parametric insurance, valid claims settle automatically via Straight-Through Processing (STP) without human adjusters. The **Claims Officer** persona is reserved for auditing telemetry disputes, reviewing anomalous readings, and manual overrides.

| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **54** | **Login** | Authenticate into claims portal | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **55** | **View Review Queue** | See claims requiring manual review | `Claims Officer Dashboard` | `GET /claims/manual-review` | `GET /api/claims/manual-review` | `event-claims-service (Port 8084)` | **[ROADMAP]** |
| **56** | **View Claim Details** | Review policy, event, readings and compensation | `Claim Review Component` | `GET /claims/{claimId}` | `GET /api/claims/{claimId}` | `event-claims-service (Port 8084)` | **[ROADMAP]** |
| **57** | **Approve Exception Claim** | Approve eligible manually reviewed claim | `Claim Decision Component` | `PUT /claims/{claimId}/approve` | `PUT /api/claims/{claimId}/approve` | `event-claims-service (Port 8084)` | **[ROADMAP]** |
| **58** | **Reject Exception Claim** | Reject failed or anomalous claim | `Claim Decision Component` | `PUT /claims/{claimId}/reject` | `PUT /api/claims/{claimId}/reject` | `event-claims-service (Port 8084)` | **[ROADMAP]** |

### 6.4 Data Provider Persona Plan (`data_provider`)
The **Data Provider** is an authorized external telemetry authority (e.g., automated irrigation canal sensors, soil moisture telemetry, satellite SAR indices). They push verified readings via secured endpoints requiring `ROLE_DATA_PROVIDER`.

| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **37** | **Login** | Authenticate authorized external data provider | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **38** | **Submit Irrigation Reading** | Send simulated verified irrigation availability data | `Data Provider Simulator` | `POST /parametric-data/readings` | `POST /api/readings` | `event-claims-service (Port 8084)` | **[IMPLEMENTED]** |

### 6.5 Administrator Persona Plan (`admin`)
The **Admin** manages platform access, configures master insurance products, sets crop-specific trigger rules, defines duration payout slabs, and monitors operational health.

| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **67** | **Login** | Authenticate into administration portal | `Login Component` | `POST /auth/login` | `POST /api/auth/login` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **68** | **Manage Users and Roles** | Create users, assign roles and change status | `User Management Component` | `GET/POST/PUT /admin/users` | `POST /api/admin/users & GET /api/admin/users` | `auth-service (Port 8081)` | **[IMPLEMENTED]** |
| **69** | **Configure Insurance Plans** | Manage coverage and base pricing rules | `Plan Management Component` | `GET/POST/PUT /admin/plans` | `POST /api/admin/plans & GET /api/admin/plans` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **70** | **Configure Trigger Rules** | Manage crop-stage thresholds, durations and payout slabs | `Trigger Rule Component` | `GET/POST/PUT /admin/trigger-rules` | `POST /api/admin/plans/{planId}/trigger-rules & /payout-slabs` | `policy-service (Port 8083)` | **[IMPLEMENTED]** |
| **71** | **Manage Data Providers** | Authorize or suspend external providers | `Provider Management Component` | `GET/POST/PUT /admin/providers` | `POST /api/admin/users (role=DATA_PROVIDER)` | `auth-service (Port 8081)` | **[CONSOLIDATED]** |
| **72** | **View Policy Statistics** | Monitor applications, active policies and premiums | `Admin Dashboard` | `GET /admin/policies/stats` | `Admin Dashboard / Health Actuator` | `policy-service (Port 8083)` | **[ROADMAP]** |
| **73** | **View Event Statistics** | Monitor readings, disruptions and trigger events | `Admin Dashboard` | `GET /admin/parametric-events/stats` | `Admin Dashboard / Health Actuator` | `event-claims-service (Port 8084)` | **[ROADMAP]** |
| **74** | **View Claim Statistics** | Monitor claims, approvals and settlements | `Admin Dashboard` | `GET /admin/claims/stats` | `Admin Dashboard / Health Actuator` | `event-claims-service (Port 8084)` | **[ROADMAP]** |
| **75** | **View Payment Statistics** | Monitor premium and payout transactions | `Admin Dashboard` | `GET /admin/payments/stats` | `Admin Dashboard / Health Actuator` | `policy-service & event-claims-service` | **[ROADMAP]** |

---

## 7. Domain Service Specifications

### 7.1 Policy & Underwriting Domain (`policy`)
Covers master plan catalogue, crop phenology trigger rules, payout slabs, application submission, risk assessment, quote binding, and active policy issuance.

| Stage | Persona | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **9** | Farmer | **Browse Insurance Plans** | Compare coverage, trigger and premium rules | `Plans Component` | `GET /plans/active` | `GET /api/plans/active` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.insurance_plans` | **[IMPLEMENTED]** |
| **10** | Farmer | **Start Policy Application** | Begin IrrigaShield application | `Policy Application Component` | `POST /applications` | `POST /api/applications` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]** |
| **11** | Farmer | **Select Farm and Crop** | Associate application with insured exposure | `Policy Application Component` | `PUT /applications/{id}/farm-crop` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[CONSOLIDATED]** |
| **12** | Farmer | **Select Plan** | Choose desired insurance plan | `Plan Selection Component` | `PUT /applications/{id}/plan` | `PUT /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[CONSOLIDATED]** |
| **13** | Farmer | **Review Application** | Verify application before submission | `Application Review Component` | `GET /applications/{id}` | `GET /api/applications/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]** |
| **14** | Farmer | **Submit Application** | Send application for underwriting | `Application Review Component` | `POST /applications/{id}/submit` | `POST /api/applications/{applicationId}/submit` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]** |
| **15** | Farmer | **Track Application** | View underwriting status and information requests | `Application Status Component` | `GET /applications/{id}/status` | `GET /api/applications/my-applications` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policy_applications` | **[IMPLEMENTED]** |
| **28** | Farmer | **View Underwriting Decision** | Review approval, coverage and quoted premium | `Application Status Component` | `GET /applications/{id}/decision` | `GET /api/policies/approved-quote/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.underwriting_cases` | **[IMPLEMENTED]** |
| **29** | Farmer | **Accept Quote** | Accept approved premium and policy terms | `Quote Component` | `POST /applications/{id}/accept-quote` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies & policy_applications` | **[IMPLEMENTED]** |
| **32** | Payment Service | **Confirm Premium Payment** | Notify Policy Service of successful payment | `Payment Status Component` | `POST /policies/{applicationId}/payment-confirmation` | `Internal Reactive Flow` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]** |
| **33** | Policy Service | **Create Policy** | Create policy from approved application | `Policy Details Component` | `POST /policies` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]** |
| **34** | Policy Service | **Activate Policy** | Activate policy after successful premium payment | `Policy Details Component` | `PUT /policies/{policyId}/activate` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]** |
| **35** | Farmer | **View Active Policy** | View coverage, exclusions and trigger rules | `Policy Details Component` | `GET /policies/{policyId}` | `GET /api/policies/my-policies` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[IMPLEMENTED]** |
| **36** | Farmer | **View Trigger Conditions** | Understand threshold, duration and payout slabs | `Policy Coverage Component` | `GET /policies/{policyId}/trigger-rules` | `GET /api/plans/{planId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.trigger_rules & payout_slabs` | **[IMPLEMENTED]** |
| **40** | Parametric Event Service | **Find Active Policy** | Resolve policy applicable at reading timestamp | `Event Evaluation View` | `GET /policies/active?farmId={farmId}&at={timestamp}` | `GET /api/internal/policies/{policyId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[IMPLEMENTED]** |
| **69** | Admin | **Configure Insurance Plans** | Manage coverage and base pricing rules | `Plan Management Component` | `GET/POST/PUT /admin/plans` | `POST /api/admin/plans & GET /api/admin/plans` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.insurance_plans` | **[IMPLEMENTED]** |
| **70** | Admin | **Configure Trigger Rules** | Manage crop-stage thresholds, durations and payout slabs | `Trigger Rule Component` | `GET/POST/PUT /admin/trigger-rules` | `POST /api/admin/plans/{planId}/trigger-rules & /payout-slabs` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.trigger_rules & payout_slabs` | **[IMPLEMENTED]** |
| **72** | Admin | **View Policy Statistics** | Monitor applications, active policies and premiums | `Admin Dashboard` | `GET /admin/policies/stats` | `Admin Dashboard / Health Actuator` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[ROADMAP]** |

### 7.2 Parametric Telemetry Domain (`parametric_event`)
Covers telemetry ingestion from authorized data providers, range verification, policy lookup, threshold comparison, and parametric deficit event detection.

| Stage | Persona | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **38** | Data Provider | **Submit Irrigation Reading** | Send simulated verified irrigation availability data | `Data Provider Simulator` | `POST /parametric-data/readings` | `POST /api/readings` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[IMPLEMENTED]** |
| **39** | Parametric Event Service | **Validate Reading** | Validate source, ranges, timestamp and farm mapping | `Data Validation View` | `POST /parametric-data/readings/{id}/validate` | `POST /api/readings (Internal Validation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]** |
| **40** | Parametric Event Service | **Find Active Policy** | Resolve policy applicable at reading timestamp | `Event Evaluation View` | `GET /policies/active?farmId={farmId}&at={timestamp}` | `GET /api/internal/policies/{policyId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[IMPLEMENTED]** |
| **41** | Parametric Event Service | **Evaluate Threshold** | Compare availability index with policy threshold | `Event Evaluation View` | `POST /parametric-events/evaluate/{readingId}` | `POST /api/readings (Evaluation Pipeline)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]** |
| **42** | Parametric Event Service | **Track Continuous Disruption** | Maintain breach start, recovery and duration | `Disruption Status Component` | `PUT /parametric-events/trackers/{policyId}` | `POST /api/readings (Disruption Tracking)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]** |
| **43** | Parametric Event Service | **Evaluate Cumulative Shortage** | Evaluate shortage inside configured crop-stage window | `Disruption Status Component` | `POST /parametric-events/policies/{policyId}/evaluate-window` | `POST /api/readings (Shortage Evaluation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[CONSOLIDATED]** |
| **44** | Parametric Event Service | **Create Verified Event** | Create event when all policy conditions are met | `Parametric Event Details` | `POST /parametric-events` | `POST /api/readings (Verified Event)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings & claims` | **[CONSOLIDATED]** |
| **45** | Parametric Event Service | **Prevent Duplicate Event** | Ensure idempotent event generation per disruption | `Parametric Event Details` | `GET /parametric-events/duplicates?policyId={policyId}` | `POST /api/readings (Idempotency Check)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[IMPLEMENTED]** |
| **46** | Farmer | **View Irrigation Data** | View provider readings for insured farm | `Irrigation Status Component` | `GET /parametric-data/farms/{farmId}/readings` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[STREAMLINED]** |
| **47** | Farmer | **View Parametric Event** | View verified trigger details | `Parametric Event Details` | `GET /parametric-events/{eventId}` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[STREAMLINED]** |
| **71** | Admin | **Manage Data Providers** | Authorize or suspend external providers | `Provider Management Component` | `GET/POST/PUT /admin/providers` | `POST /api/admin/users (role=DATA_PROVIDER)` | `auth-service (Port 8081)`<br/>`irrigashield_auth_db.users` | **[CONSOLIDATED]** |
| **73** | Admin | **View Event Statistics** | Monitor readings, disruptions and trigger events | `Admin Dashboard` | `GET /admin/parametric-events/stats` | `Admin Dashboard / Health Actuator` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.irrigation_readings` | **[ROADMAP]** |

### 7.3 Claims Settlement Domain (`claims`)
Covers automated claim instantiation, Straight-Through Processing (STP) validation, graduated compensation tier calculation, and claim closure.

| Stage | Persona | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **48** | Claims Service | **Auto Create Claim** | Create claim from verified parametric event | `Claim Details Component` | `POST /claims/from-event` | `POST /api/readings (Auto Claim Creation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **49** | Claims Service | **Validate Claim Eligibility** | Verify active policy, coverage, source and event period | `Claim Validation Component` | `POST /claims/{claimId}/validate` | `POST /api/readings (Eligibility Validation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **50** | Claims Service | **Check Duplicate Claim** | Prevent repeated claim for same event | `Claim Validation Component` | `GET /claims/duplicate?eventId={eventId}` | `POST /api/readings (Duplicate Prevention)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **51** | Claims Service | **Calculate Compensation** | Apply policy payout slab and maximum limit | `Compensation Component` | `POST /claims/{claimId}/calculate-compensation` | `POST /api/readings (Compensation Calculation)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[IMPLEMENTED]** |
| **52** | Claims Service | **Auto Approve Normal Claim** | Approve claim when all straight-through rules pass | `Claim Decision Component` | `PUT /claims/{claimId}/auto-approve` | `POST /api/readings (Auto Approve)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **53** | Claims Service | **Route Exception to Manual Review** | Flag anomalies and failed validations | `Claim Review Component` | `PUT /claims/{claimId}/manual-review` | `Exception Routing (Audit Roadmap)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]** |
| **55** | Claims Officer | **View Review Queue** | See claims requiring manual review | `Claims Officer Dashboard` | `GET /claims/manual-review` | `GET /api/claims/manual-review` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]** |
| **56** | Claims Officer | **View Claim Details** | Review policy, event, readings and compensation | `Claim Review Component` | `GET /claims/{claimId}` | `GET /api/claims/{claimId}` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]** |
| **57** | Claims Officer | **Approve Exception Claim** | Approve eligible manually reviewed claim | `Claim Decision Component` | `PUT /claims/{claimId}/approve` | `PUT /api/claims/{claimId}/approve` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]** |
| **58** | Claims Officer | **Reject Exception Claim** | Reject failed or anomalous claim | `Claim Decision Component` | `PUT /claims/{claimId}/reject` | `PUT /api/claims/{claimId}/reject` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]** |
| **59** | Claims Service | **Create Payout Instruction** | Send approved compensation for payment | `Settlement Component` | `POST /payouts/instructions` | `POST /api/readings (Settlement Pipeline)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **61** | Payment Service | **Report Payout Result** | Update claim with transaction status | `Payout Status Component` | `PUT /claims/{claimId}/payout-status` | `POST /api/readings (Payout Reporting)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **62** | Claims Service | **Mark Claim Settled** | Close claim following successful payout | `Claim Details Component` | `PUT /claims/{claimId}/settle` | `POST /api/readings (Settlement Completion)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **63** | Farmer | **Track Claim** | View claim validation, approval and payout status | `Claims Dashboard` | `GET /claims/my-claims` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[IMPLEMENTED]** |
| **64** | Farmer | **View Settlement** | View compensation and transaction details | `Settlement Details Component` | `GET /claims/{claimId}/settlement` | `GET /api/claims/my-claims` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[STREAMLINED]** |
| **74** | Admin | **View Claim Statistics** | Monitor claims, approvals and settlements | `Admin Dashboard` | `GET /admin/claims/stats` | `Admin Dashboard / Health Actuator` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[ROADMAP]** |

### 7.4 Payment & Payout Domain (`payment`)
Covers simulated premium collection upon quote acceptance, automated payout instruction dispatching, and straight-through settlement execution.

| Stage | Persona | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **30** | Farmer | **Pay Premium** | Make simulated premium payment | `Premium Payment Component` | `POST /payments/premiums` | `POST /api/policies/approved-quote/{applicationId}/accept` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[STREAMLINED]** |
| **31** | Farmer | **View Payment Status** | Check premium transaction result | `Payment Status Component` | `GET /payments/{paymentId}` | `GET /api/policies/approved-quote/{applicationId}` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[STREAMLINED]** |
| **32** | Payment Service | **Confirm Premium Payment** | Notify Policy Service of successful payment | `Payment Status Component` | `POST /policies/{applicationId}/payment-confirmation` | `Internal Reactive Flow` | `policy-service (Port 8083)`<br/>`irrigashield_policy_db.policies` | **[CONSOLIDATED]** |
| **59** | Claims Service | **Create Payout Instruction** | Send approved compensation for payment | `Settlement Component` | `POST /payouts/instructions` | `POST /api/readings (Settlement Pipeline)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **60** | Payment Service | **Process Claim Payout** | Simulate transfer of approved compensation | `Payout Status Component` | `POST /payouts` | `POST /api/readings (Payout Processing)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **61** | Payment Service | **Report Payout Result** | Update claim with transaction status | `Payout Status Component` | `PUT /claims/{claimId}/payout-status` | `POST /api/readings (Payout Reporting)` | `event-claims-service (Port 8084)`<br/>`irrigashield_event_claims_db.claims` | **[CONSOLIDATED]** |
| **75** | Admin | **View Payment Statistics** | Monitor premium and payout transactions | `Admin Dashboard` | `GET /admin/payments/stats` | `Admin Dashboard / Health Actuator` | `policy-service & event-claims-service`<br/>`irrigashield_policy_db & claims_db` | **[ROADMAP]** |

---

## 8. Database Architecture & Microservice Mappings (`Db_mappings`)

### 8.1 Planned vs. Production Database Topology
The table below details how the 8 originally planned databases were consolidated into **4 production-grade reactive MySQL databases** running with Spring Data R2DBC.

| Service Name | Planned Database | Production R2DBC Database | Port | Production MySQL Tables | Architectural Status & Rationale |
| :--- | :--- | :--- | :---: | :--- | :--- |
| **Auth Service** | `auth_db` | `irrigashield_auth_db` | 8081 | `users` | **[ACTIVE]** Cleanly isolates platform identities, BCrypt credentials, and role assignments (`ROLE_FARMER`, `ROLE_UNDERWRITER`, `ROLE_DATA_PROVIDER`, `ROLE_CLAIMS_OFFICER`, `ROLE_ADMIN`). |
| **Farm Service** | `farm_db` | `irrigashield_farm_db` | 8082 | `farmer_profiles`, `farms`, `crops`, `irrigation_profiles` | **[ACTIVE]** Central repository for all agricultural, hydrological, and agronomic assets. Provides risk profile aggregation API (`/api/farms/{farmId}/risk-profile`). |
| **Policy Service** | `policy_db` + `underwriter_db` | `irrigashield_policy_db` | 8083 | `insurance_plans`, `trigger_rules`, `payout_slabs`, `policy_applications`, `underwriting_cases`, `policies` | **[CONSOLIDATED]** Unified policy lifecycle and underwriting engine into a single high-cohesion bounded context. Eliminates distributed transactions during application underwriting and policy activation. |
| **Event Claims Service** | `event_db` + `claims_db` + `payment_db` | `irrigashield_event_claims_db` | 8084 | `irrigation_readings`, `claims` | **[CONSOLIDATED]** Combines telemetry ingestion, parametric threshold breach evaluation, claim generation, and immediate settlement into an event-driven straight-through reactive pipeline. |
| **Payment Service** | `payment_db` | Embedded in `policy_db` & `event_claims_db` | Embedded | `policies.payment_status`, `claims.payout_status` | **[EMBEDDED]** Simulated financial transactions execute directly inside policy activation and claim settlement transactions. Standalone gateway reserved for Phase 3. |
| **Notification Service**| `notification_db` | State Fields across all services | N/A | State audit trails | **[ROADMAP]** Real-time domain status updates provide immediate visibility. Dedicated WebSocket/SSE push notification service planned for Phase 2. |

---

## 9. Comprehensive Data Dictionary & Production Schemas (`schemas`)

### 9.1 Table Evolution Matrix (All 23 Planned Tables vs 10 Production Tables)

| # | Planned Table (Excel) | Key Columns (Excel Plan) | Production Table (`Capstone-IrrigaSheild`) | Production Database | Schema Status & Mapping Notes |
| :-: | :--- | :--- | :--- | :--- | :--- |
| 1 | `users` | user_id (PK), name, email (UQ), password_hash, phone, role, status, created_at, updated_at | `users` | `irrigashield_auth_db` | [ACTIVE] Fully implemented. Role values: FARMER, UNDERWRITER, CLAIMS_OFFICER, DATA_PROVIDER, ADMIN. |
| 2 | `farmers` | farmer_id (PK), user_id, address, bank_account_masked, verification_status | `farmer_profiles` | `irrigashield_farm_db` | [ACTIVE] Implemented as farmer_profiles. Links to auth_user_id; stores KYC, address, bank_account_last_four, verification_status. |
| 3 | `farms` | farm_id (PK), farmer_id, farm_name, location, area_acres, latitude, longitude, status | `farms` | `irrigashield_farm_db` | [ACTIVE] Implemented as farms. Stores survey_number, address, area_in_acres, soil_type, ownership_type, lat/lng, status. |
| 4 | `crops` | crop_id (PK), farm_id, crop_type, variety, sowing_date, expected_harvest_date, current_growth_stage | `crops` | `irrigashield_farm_db` | [ACTIVE] Implemented as crops. Stores crop_type, variety, sowing_date, expected_harvest_date, growth_stage, irrigation_requirement. |
| 5 | `irrigation_profiles` | irrigation_id (PK), farm_id, water_source, irrigation_method, equipment_condition, historical_failures, seasonal_risk | `irrigation_profiles` | `irrigashield_farm_db` | [ACTIVE] Implemented. Stores water_source, irrigation_method, equipment_condition, backup_water_source, historical_failure_count, average_water_availability_pct, seasonal_risk, maintenance_frequency. |
| 6 | `applications` | application_id (PK), farmer_id, farm_id, crop_id, plan_id, status, submitted_at, accepted_quote_at | `policy_applications` | `irrigashield_policy_db` | [ACTIVE] Implemented as policy_applications. Stores farmer_user_id, farm_id, crop_id, plan_id, requested_coverage, proposed_start_date, proposed_end_date, status, submitted_at. |
| 7 | `risk_assessments` | assessment_id (PK), application_id, factor scores, overall_score, risk_level, assessed_at | `underwriting_cases` | `irrigashield_policy_db` | [CONSOLIDATED] Consolidated into underwriting_cases table: water_availability_score, equipment_condition_score, historical_failure_score, seasonal_risk_score, backup_source_score, overall_risk_score, risk_level. |
| 8 | `premium_recommendations` | recommendation_id (PK), application_id, base_premium, risk_loading, crop_factor, seasonal_factor, final_premium | `underwriting_cases` | `irrigashield_policy_db` | [CONSOLIDATED] Consolidated into underwriting_cases table: base_premium, risk_loading, recommended_premium, recommended_coverage. |
| 9 | `underwriting_decisions` | decision_id (PK), application_id, underwriter_id, decision, remarks, decided_at | `underwriting_cases` | `irrigashield_policy_db` | [CONSOLIDATED] Consolidated into underwriting_cases table: decision, remarks, status, review_started_at, review_completed_at. |
| 10 | `insurance_plans` | plan_id (PK), name, description, coverage_min, coverage_max, base_premium, active | `insurance_plans` | `irrigashield_policy_db` | [ACTIVE] Implemented. Stores plan_name, description, min_coverage_amount, max_coverage_amount, base_premium_rate, status. |
| 11 | `trigger_rules` | rule_id (PK), plan_id, crop_type, growth_stage, availability_threshold_pct, continuous_hours, window_days, cumulative_shortage_days | `trigger_rules` | `irrigashield_policy_db` | [ACTIVE] Implemented. Stores plan_id, crop_type, growth_stage, availability_threshold_pct, minimum_continuous_hours, rolling_window_days, required_shortage_days, status. |
| 12 | `payout_slabs` | slab_id (PK), rule_id, duration_from_hours, duration_to_hours, payout_percentage | `payout_slabs` | `irrigashield_policy_db` | [ACTIVE] Implemented. Stores trigger_rule_id, minimum_duration_hours, maximum_duration_hours, min_availability_pct, max_availability_pct, payout_percentage, status. |
| 13 | `policies` | policy_id (PK), policy_number (UQ), application_id, farmer_id, farm_id, crop_id, coverage_amount, premium_amount, start_date, end_date, status | `policies` | `irrigashield_policy_db` | [ACTIVE] Implemented. Stores application_id, farmer_user_id, farm_id, crop_id, plan_id, coverage_amount, premium_amount, start_date, end_date, payment_status, status, accepted_at. |
| 14 | `data_providers` | provider_id (PK), provider_code (UQ), name, source_type, api_key_hash, status | `users (role=DATA_PROVIDER)` | `irrigashield_auth_db` | [CONSOLIDATED] Managed as users with role=DATA_PROVIDER in irrigashield_auth_db. |
| 15 | `irrigation_readings` | reading_id (PK), provider_id, farm_id, availability_pct, water_availability_pct, pump_status, recorded_at, validation_status | `irrigation_readings` | `irrigashield_event_claims_db` | [ACTIVE] Implemented. Stores policy_id, farm_id, availability_percentage, recorded_at, submitted_by, created_at. |
| 16 | `disruption_trackers` | tracker_id (PK), policy_id, breach_started_at, latest_reading_at, duration_hours, cumulative_shortage_hours, lowest_availability, status | `irrigation_readings` | `irrigashield_event_claims_db` | [STREAMLINED] Streamlined directly into reactive reading evaluation pipeline; breach metrics recorded in reading and claim. |
| 17 | `parametric_events` | event_id (PK), event_reference (UQ), policy_id, farm_id, rule_id, started_at, triggered_at, duration_hours, event_status | `claims` | `irrigashield_event_claims_db` | [CONSOLIDATED] Parametric breach instantly instantiates a verified claim record; event details embedded directly. |
| 18 | `claims` | claim_id (PK), claim_number (UQ), policy_id, event_id (UQ), status, auto_created_at, claims_officer_id, remarks | `claims` | `irrigashield_event_claims_db` | [ACTIVE] Implemented. Stores reading_id, policy_id, event_date, farmer_user_id, farm_id, availability_percentage, payout_percentage, coverage_amount, compensation_amount, payout_status, status. Deduplicates per (policy_id, event_date). |
| 19 | `claim_validations` | validation_id (PK), claim_id, policy_active, source_authorized, threshold_met, duration_met, duplicate_found, result | `claims` | `irrigashield_event_claims_db` | [CONSOLIDATED] Validations executed in reactive business logic before claim persistence; duplicate check via claimRepository. |
| 20 | `compensations` | compensation_id (PK), claim_id, coverage_amount, payout_percentage, eligible_amount, approved_amount, calculated_at | `claims` | `irrigashield_event_claims_db` | [CONSOLIDATED] Stored directly as coverage_amount, payout_percentage, compensation_amount in claims table. |
| 21 | `payments` | payment_id (PK), reference_type, reference_id, farmer_id, amount, transaction_reference, status, payment_date | `policies & claims` | `irrigashield_policy_db & claims_db` | [EMBEDDED] Simulated payment reflected via policies.payment_status='PAID' and claims.payout_status='SUCCESS'. |
| 22 | `payouts` | payout_id (PK), claim_id, compensation_id, amount, transaction_reference, status, processed_at | `claims` | `irrigashield_event_claims_db` | [CONSOLIDATED] Stored directly in claims table as payout_status='SUCCESS', status='SETTLED', settled_at=now(). |
| 23 | `notifications` | notification_id (PK), user_id, title, message, notification_type, read_status, created_at | `Domain State Fields` | `All Services` | [ROADMAP] Live state exposed across application, policy, and claim records; dedicated notification bus in Phase 2 roadmap. |

### 9.2 Production DDL Schemas (Spring Data R2DBC)

#### Database: `irrigashield_auth_db`
```sql
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
);
```

#### Database: `irrigashield_farm_db`
```sql
CREATE TABLE IF NOT EXISTS farmer_profiles (
    farmer_id BIGINT NOT NULL AUTO_INCREMENT,
    auth_user_id BIGINT NOT NULL,
    email VARCHAR(150) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address_line VARCHAR(200) NOT NULL,
    village VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    bank_account_last_four VARCHAR(4),
    verification_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_farmer_profiles PRIMARY KEY (farmer_id),
    CONSTRAINT uk_farmer_profiles_auth_user UNIQUE (auth_user_id),
    CONSTRAINT uk_farmer_profiles_email UNIQUE (email),
    INDEX idx_farmer_profiles_district (district),
    INDEX idx_farmer_profiles_verification_status (verification_status)
);

CREATE TABLE IF NOT EXISTS farms (
    farm_id BIGINT NOT NULL AUTO_INCREMENT,
    farmer_id BIGINT NOT NULL,
    farm_name VARCHAR(100) NOT NULL,
    survey_number VARCHAR(50),
    address_line VARCHAR(200) NOT NULL,
    village VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(10) NOT NULL,
    area_in_acres DECIMAL(10, 2) NOT NULL,
    soil_type VARCHAR(50) NOT NULL,
    ownership_type VARCHAR(30) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_farms PRIMARY KEY (farm_id),
    CONSTRAINT fk_farms_farmer FOREIGN KEY (farmer_id) REFERENCES farmer_profiles (farmer_id),
    INDEX idx_farms_farmer_id (farmer_id),
    INDEX idx_farms_district (district),
    INDEX idx_farms_status (status)
);

CREATE TABLE IF NOT EXISTS crops (
    crop_id BIGINT NOT NULL AUTO_INCREMENT,
    farm_id BIGINT NOT NULL,
    crop_type VARCHAR(50) NOT NULL,
    variety VARCHAR(100),
    sowing_date DATE NOT NULL,
    expected_harvest_date DATE NOT NULL,
    growth_stage VARCHAR(30) NOT NULL,
    irrigation_requirement VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_crops PRIMARY KEY (crop_id),
    CONSTRAINT fk_crops_farm FOREIGN KEY (farm_id) REFERENCES farms (farm_id),
    INDEX idx_crops_farm_id (farm_id),
    INDEX idx_crops_crop_type (crop_type),
    INDEX idx_crops_status (status)
);

CREATE TABLE IF NOT EXISTS irrigation_profiles (
    irrigation_id BIGINT NOT NULL AUTO_INCREMENT,
    farm_id BIGINT NOT NULL,
    water_source VARCHAR(30) NOT NULL,
    irrigation_method VARCHAR(30) NOT NULL,
    equipment_condition VARCHAR(20) NOT NULL,
    backup_water_source VARCHAR(30),
    historical_failure_count INT NOT NULL DEFAULT 0,
    average_water_availability_pct DECIMAL(5, 2) NOT NULL,
    seasonal_risk VARCHAR(20) NOT NULL,
    maintenance_frequency VARCHAR(20) NOT NULL,
    last_maintenance_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_irrigation_profiles PRIMARY KEY (irrigation_id),
    CONSTRAINT uk_irrigation_profiles_farm UNIQUE (farm_id),
    CONSTRAINT fk_irrigation_profiles_farm FOREIGN KEY (farm_id) REFERENCES farms (farm_id),
    INDEX idx_irrigation_water_source (water_source),
    INDEX idx_irrigation_seasonal_risk (seasonal_risk)
);
```

#### Database: `irrigashield_policy_db`
```sql
CREATE TABLE IF NOT EXISTS insurance_plans (
    plan_id BIGINT NOT NULL AUTO_INCREMENT,
    plan_name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    min_coverage_amount DECIMAL(12, 2) NOT NULL,
    max_coverage_amount DECIMAL(12, 2) NOT NULL,
    base_premium_rate DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_insurance_plans PRIMARY KEY (plan_id),
    CONSTRAINT uk_insurance_plans_name UNIQUE (plan_name),
    INDEX idx_insurance_plans_status (status)
);

CREATE TABLE IF NOT EXISTS trigger_rules (
    trigger_rule_id BIGINT NOT NULL AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    crop_type VARCHAR(50) NOT NULL,
    growth_stage VARCHAR(30) NOT NULL,
    availability_threshold_pct DECIMAL(5, 2) NOT NULL,
    minimum_continuous_hours INT NOT NULL,
    rolling_window_days INT NOT NULL,
    required_shortage_days INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_trigger_rules PRIMARY KEY (trigger_rule_id),
    CONSTRAINT fk_trigger_rules_plan FOREIGN KEY (plan_id) REFERENCES insurance_plans (plan_id),
    CONSTRAINT uk_trigger_rule_plan_crop_stage UNIQUE (plan_id, crop_type, growth_stage),
    INDEX idx_trigger_rules_plan_id (plan_id),
    INDEX idx_trigger_rules_crop_type (crop_type),
    INDEX idx_trigger_rules_status (status)
);

CREATE TABLE IF NOT EXISTS payout_slabs (
    payout_slab_id BIGINT NOT NULL AUTO_INCREMENT,
    trigger_rule_id BIGINT NOT NULL,
    minimum_duration_hours INT DEFAULT 0,
    maximum_duration_hours INT,
    min_availability_pct DECIMAL(5, 2),
    max_availability_pct DECIMAL(5, 2),
    payout_percentage DECIMAL(5, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_payout_slabs PRIMARY KEY (payout_slab_id),
    CONSTRAINT fk_payout_slabs_trigger_rule FOREIGN KEY (trigger_rule_id) REFERENCES trigger_rules (trigger_rule_id),
    INDEX idx_payout_slabs_trigger_rule (trigger_rule_id),
    INDEX idx_payout_slabs_status (status)
);

CREATE TABLE IF NOT EXISTS policy_applications (
    application_id BIGINT NOT NULL AUTO_INCREMENT,
    farmer_user_id BIGINT NOT NULL,
    farmer_email VARCHAR(150) NOT NULL,
    farm_id BIGINT NOT NULL,
    crop_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    requested_coverage DECIMAL(12, 2) NOT NULL,
    proposed_start_date DATE NOT NULL,
    proposed_end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    submitted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_policy_applications PRIMARY KEY (application_id),
    CONSTRAINT fk_policy_applications_plan FOREIGN KEY (plan_id) REFERENCES insurance_plans (plan_id),
    INDEX idx_applications_farmer (farmer_user_id),
    INDEX idx_applications_farm (farm_id),
    INDEX idx_applications_status (status)
);

CREATE TABLE IF NOT EXISTS underwriting_cases (
    case_id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    underwriter_user_id BIGINT NOT NULL,
    underwriter_email VARCHAR(150) NOT NULL,
    water_availability_score INT,
    equipment_condition_score INT,
    historical_failure_score INT,
    seasonal_risk_score INT,
    backup_source_score INT,
    overall_risk_score INT,
    risk_level VARCHAR(20),
    base_premium DECIMAL(12, 2),
    risk_loading DECIMAL(12, 2),
    recommended_premium DECIMAL(12, 2),
    recommended_coverage DECIMAL(12, 2),
    decision VARCHAR(20),
    remarks VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    review_started_at TIMESTAMP NULL,
    review_completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_underwriting_cases PRIMARY KEY (case_id),
    CONSTRAINT uk_underwriting_application UNIQUE (application_id),
    CONSTRAINT fk_underwriting_application FOREIGN KEY (application_id) REFERENCES policy_applications (application_id),
    INDEX idx_underwriting_status (status),
    INDEX idx_underwriting_underwriter (underwriter_user_id)
);

CREATE TABLE IF NOT EXISTS policies (
    policy_id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL,
    farmer_user_id BIGINT NOT NULL,
    farmer_email VARCHAR(150) NOT NULL,
    farm_id BIGINT NOT NULL,
    crop_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    coverage_amount DECIMAL(12, 2) NOT NULL,
    premium_amount DECIMAL(12, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    accepted_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_policies PRIMARY KEY (policy_id),
    CONSTRAINT uk_policies_application UNIQUE (application_id),
    CONSTRAINT fk_policies_application FOREIGN KEY (application_id) REFERENCES policy_applications (application_id),
    CONSTRAINT fk_policies_plan FOREIGN KEY (plan_id) REFERENCES insurance_plans (plan_id),
    INDEX idx_policies_farmer (farmer_user_id),
    INDEX idx_policies_status (status)
);
```

#### Database: `irrigashield_event_claims_db`
```sql
CREATE TABLE IF NOT EXISTS irrigation_readings (
    reading_id BIGINT NOT NULL AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    availability_percentage DECIMAL(5, 2) NOT NULL,
    recorded_at TIMESTAMP NOT NULL,
    submitted_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_irrigation_readings PRIMARY KEY (reading_id),
    INDEX idx_readings_policy (policy_id),
    INDEX idx_readings_farm (farm_id)
);

CREATE TABLE IF NOT EXISTS claims (
    claim_id BIGINT NOT NULL AUTO_INCREMENT,
    reading_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    event_date DATE NOT NULL,
    farmer_user_id BIGINT NOT NULL,
    farm_id BIGINT NOT NULL,
    availability_percentage DECIMAL(5, 2) NOT NULL,
    payout_percentage DECIMAL(5, 2) NOT NULL,
    coverage_amount DECIMAL(12, 2) NOT NULL,
    compensation_amount DECIMAL(12, 2) NOT NULL,
    payout_status VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP NULL,
    CONSTRAINT pk_claims PRIMARY KEY (claim_id),
    CONSTRAINT fk_claims_reading FOREIGN KEY (reading_id) REFERENCES irrigation_readings (reading_id),
    CONSTRAINT uk_claims_reading UNIQUE (reading_id),
    INDEX idx_claims_policy (policy_id),
    INDEX idx_claims_policy_event (policy_id, event_date),
    INDEX idx_claims_farmer (farmer_user_id),
    INDEX idx_claims_status (status)
);
```

---

## 10. Capstone Evaluation & Technical Competency Matrix (`Evaluation_Mapping`)

This matrix maps every single evaluation category and weightage from the official Capstone Rubric (`Capstone Evaluation Java Fsd - Google Sheets.pdf`) directly to concrete architectural implementations and codebase locations in `Capstone-IrrigaSheild`.

| # | Rubric Category (PDF) | Metric & Weight | How IrrigaShield Demonstrates & Meets It | Evidence / Codebase Location | Actual Status |
| :-: | :--- | :--- | :--- | :--- | :---: |
| **1** | **Requirements & Planning** | **Understanding of Project Requirements** [Weight: 20] | Parametric agricultural insurance business workflow, 5 distinct personas (FARMER, UNDERWRITER, DATA_PROVIDER, CLAIMS_OFFICER, ADMIN), telemetry trigger-to-settlement lifecycle, and basis risk mitigation fully modeled. | `IRRIGASHIELD_UPDATED_MAIN_PLAN.md`, `API_DOCUMENTATION.md` | **[VERIFIED / COMPLETE]** |
| **2** | **Frontend Engineering** | **Frontend Development (Tailwind & Angular)** [Weight: 20] | Enterprise responsive UI using Angular 21 standalone components, Tailwind CSS styling, role-based dashboards, and intuitive workflow navigation. | `irrigashield-frontend/src/app/features/*` | **[VERIFIED / COMPLETE]** |
| **3** | **Frontend Engineering** | **Frontend State Management (NgRx)** [Weight: 10] | Single source of truth using NgRx store with strongly-typed actions, reducers, selectors, and side-effect handlers across auth, policy, and claims. | `irrigashield-frontend/src/app/features/*/state/*` | **[VERIFIED / COMPLETE]** |
| **4** | **Frontend Engineering** | **Validation (Frontend)** [Weight: 10] | Angular Reactive Forms with synchronous and asynchronous custom validators (PIN code regex, acreage bounds, date interval checks). | `irrigashield-frontend/src/app/shared/validators/*` | **[VERIFIED / COMPLETE]** |
| **5** | **Architecture & Microservices** | **API Gateway & Routing** [Weight: 10] | Spring Cloud Gateway Reactive running on Port 8080 with dynamic route dispatching, centralized CORS (`http://localhost:4200`), HttpOnly JWT cookie inspection, external header stripping, and internal trusted header propagation (`X-User-Id`, `X-User-Email`, `X-User-Roles`). | `api-gateway/src/main/resources/application.yaml`, `JwtAuthenticationFilter.java` | **[VERIFIED / COMPLETE]** |
| **6** | **Architecture & Microservices** | **Service Discovery (Eureka)** [Weight: 10] | Standalone Netflix Eureka Server registry on Port 8761 with client heartbeat monitoring, dynamic instance resolution, and load-balanced non-blocking `WebClient` RPC calls between microservices (`http://FARM-SERVICE`, `http://POLICY-SERVICE`). | `eureka-server/src/main/resources/application.yml`, `EurekaServerApplication.java` | **[VERIFIED / COMPLETE]** |
| **7** | **Backend Engineering** | **Backend Development (Java & Spring Boot)** [Weight: 20] | Reactive Spring Boot 3.5.6 microservices using Spring WebFlux, Project Reactor (`Mono`/`Flux`), Netty non-blocking I/O, clean Domain-Driven Design (DDD), and Spring Data R2DBC. Features 5-factor risk scoring engine (`RiskCalculator`) and Straight-Through Processing claims settlement. | `auth-service`, `farm-service`, `policy-service`, `event-claims-service` | **[VERIFIED / COMPLETE]** |
| **8** | **Backend Engineering** | **Validation (Backend)** [Weight: 10] | Comprehensive Jakarta Bean Validation (`@NotNull`, `@DecimalMin`, `@Size`, `@Pattern`) on all immutable record DTOs; domain invariant validation in service layer; centralized `@RestControllerAdvice` error envelope (`ApiErrorResponse`). | `dto/request/*`, `exception/GlobalExceptionHandler.java` across all services | **[VERIFIED / COMPLETE]** |
| **9** | **Data & Operations** | **Logging & Monitoring** [Weight: 10] | Centralized Logback logging with SLF4J, execution-time profiling via Aspect-Oriented Programming (`LoggingAspect`), and Spring Boot Actuator health endpoints (`/actuator/health`, `/actuator/info`). | `aspect/LoggingAspect.java`, `application.yaml` Actuator config | **[VERIFIED / COMPLETE]** |
| **10** | **Data & Operations** | **Database Design & Integration (DBMS)** [Weight: 20] | Production Database-per-service pattern with 4 isolated MySQL 8.0 databases (`irrigashield_auth_db`, `irrigashield_farm_db`, `irrigashield_policy_db`, `irrigashield_event_claims_db`), Spring Data R2DBC reactive driver (`asyncer-r2dbc-mysql`), primary/foreign keys, unique constraints, and optimized indexes (`idx_claims_policy_event`, `idx_crops_crop_type`, etc.). | `schema.sql` across all services | **[VERIFIED / COMPLETE]** |
| **11** | **Code Quality & Integration** | **Unit Testing & Code Coverage** [Weight: 10] | JUnit 5 and Mockito test suites, reactive stream verification via `StepVerifier`, HTTP slice testing via `WebTestClient`, and repository testing via `@DataR2dbcTest`. 100% test pass rate across `policy-service` (15/15) and `event-claims-service` (13/13). | `src/test/java/*` across all microservices | **[VERIFIED / COMPLETE]** |
| **12** | **Code Quality & Integration** | **Code Quality & Best Practices** [Weight: 10] | Strict adherence to SOLID principles, immutable Java Records for DTOs, Lombok boilerplate minimization, dedicated mapper classes, Role-Based Access Control (`@PreAuthorize`), and zero-trust internal header validation. | `util/*Mapper.java`, `security/TrustedHeaderAuthenticationFilter.java` | **[VERIFIED / COMPLETE]** |
| **13** | **Code Quality & Integration** | **Integration of Frontend & Backend** [Weight: 20] | Angular HTTP client with credentials (`withCredentials: true`), HttpOnly cookie authentication, CORS mapping, synchronized TypeScript/Java DTO models, and error response handling. | `features/auth/interceptors/auth.interceptor.ts`, Gateway CORS | **[VERIFIED / COMPLETE]** |
| **14** | **Final Assessment** | **Presentation & Communication** [Weight: 20] | Structured architectural defense, clear live system walkthrough from registration to automated deficit detection and payout settlement. | `Demo_Journey` script & Section 11 of this plan | **[VERIFIED / COMPLETE]** |
| **15** | **Final Assessment** | **User Journey & Explanation of Project** [Weight: 50] | Comprehensive end-to-end user persona workflows, agronomic risk modeling justification, parametric basis risk transparency, and architectural trade-off evaluations. | Sections 5, 6, 12, 13 of this plan | **[VERIFIED / COMPLETE]** |

---

## 11. Live Demonstration Script & End-to-End Walkthrough (`Demo_Journey`)

This 15-step script provides the precise demonstration workflow to showcase the live platform during technical evaluations.

| Step | Actor | Demo Action | Expected Business Result | Exact API Call & Verification Payload |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Farmer** | **Register and login** | FARMER JWT cookie generated and stored in browser session. | `POST /api/auth/register` then `POST /api/auth/login` (email: `farmer@irrigashield.com`, password: `Farmer@123`). Header `Set-Cookie: IRRIGASHIELD_TOKEN` returned. |
| **2** | **Farmer** | **Register farm, crop and irrigation details** | Farm risk inputs stored in `irrigashield_farm_db`. | `POST /api/farmers/profile`, `POST /api/farms` (10 acres), `POST /api/farms/{id}/crops` (WHEAT, VEGETATIVE), `POST /api/farms/{id}/irrigation` (CANAL, FAIR condition, 65% avg water). |
| **3** | **Farmer** | **Select plan and submit application** | Application created in `DRAFT` then transitioned to `SUBMITTED`. | `GET /api/plans/active`, `POST /api/applications` (requested coverage: 50,000), `POST /api/applications/{id}/submit`. Status becomes `SUBMITTED`. |
| **4** | **Underwriter** | **Run risk assessment** | Risk score (55) and classification (`HIGH`) displayed. | Login as `underwriter@irrigashield.com`. `GET /api/underwriting/applications/pending`. `POST /api/underwriting/applications/{id}/start-review`. `POST /api/underwriting/applications/{id}/assess`. Returns factor breakdown and recommended premium. |
| **5** | **Underwriter** | **Approve application** | Application marked `APPROVED` with binding quote. | `PUT /api/underwriting/applications/{id}/approve` with `DecisionRequest(remarks='Low historical failure rate')`. Case and application status become `APPROVED`. |
| **6** | **Farmer** | **Accept quote and pay premium** | Premium payment marked `PAID` instantly. | Switch back to Farmer. `GET /api/policies/approved-quote/{applicationId}`. `POST /api/policies/approved-quote/{applicationId}/accept`. Payment status clears as `PAID`. |
| **7** | **Policy Service** | **Activate policy** | Policy record created with status `ACTIVE`. | Response from step 6 returns active policy with `policyId`, agreed coverage (`50,000`), premium (`3,250`), `startDate`, `endDate`, and `status='ACTIVE'`. |
| **8** | **Data Provider** | **Submit normal telemetry reading** | Reading saved; no claim triggered. | Login as `dataprovider@irrigashield.com`. `POST /api/readings` with `policyId`, `availabilityPercentage: 85.00`. Response returns `claimCreated: false, status: 'NO_CLAIM', payoutStatus: 'NOT_REQUIRED'`. |
| **9** | **Data Provider** | **Submit below-threshold reading (Breach)** | Breach detected; enters parametric deficit settlement. | `POST /api/readings` with `policyId`, `availabilityPercentage: 25.00`. Threshold breach (< 70%) triggers automated straight-through settlement engine. |
| **10** | **Parametric Engine** | **Satisfy trigger rules** | Deficit validated; idempotency verified. | Engine queries `GET /api/internal/policies/{policyId}`, verifies active policy dates, checks that no prior settled claim exists on policy. |
| **11** | **Claims Service** | **Auto-create verified claim** | Claim record instantiated. | Claim record generated linked to `readingId`, `policyId`, `farmerUserId`, `farmId` with availability 25.00%. |
| **12** | **Claims Service** | **Calculate rule-based compensation** | 50% payout tier applied; compensation computed. | Availability < 30.00% triggers Tier 1 payout (50%). Compensation calculated: `50,000 * 50% = 25,000.00`. |
| **13** | **Claims Officer** | **Straight-Through Processing clearance** | Claim approved automatically without human queue. | Under normal straight-through parametric processing, claim transitions directly to settlement without manual intervention. |
| **14** | **Payment Service** | **Process simulated payout** | Claim marked `SETTLED` with payout `SUCCESS`. | Claim persisted with `payout_status='SUCCESS'`, `status='SETTLED'`, and `settled_at=now()`. |
| **15** | **Farmer** | **View claim, payout and notification** | Farmer verifies full indemnity settlement. | Farmer calls `GET /api/claims/my-claims`. Receives settled claim with compensation `25,000.00`, reading `25%`, and payout `SUCCESS`. Complete journey concluded! |

---

## 12. Architectural Principles & Governance Notes (`Notes`)

### 12.1 Core Architectural Principles

#### Final Project Description
> IrrigaShield is a reactive microservices parametric insurance platform protecting farmers from irrigation deficits. It receives readings from authorized external telemetry providers, evaluates policy-defined triggers, auto-creates claims, computes predefined compensation, and settles payouts straight-through without manual surveys.

#### Important Architecture Decision
> Consolidated 8 theoretical services into 4 reactive domain microservices. Underwriting is unified inside `policy-service`. Monitoring, trigger evaluation, claims calculation, and payout simulation are unified inside `event-claims-service`. This eliminates distributed transactions, network latency, and eventual consistency failures.

#### Data Collection Boundary
> IrrigaShield does not operate physical IoT sensors. The platform defines an authorized external data provider contract (`POST /api/readings`) secured by `ROLE_DATA_PROVIDER`. Sensors, SCADA, or satellite feeds authenticate and transmit structured telemetry indices.

#### Trigger Model
> Configurable parametric triggers define availability percentage thresholds (default 70.00%), continuous duration hours, and growth-stage windows. Payouts are graduated into mathematical tiers based on severity.

#### Claim Model
> Straight-Through Processing (STP) automates verified claim approval and immediate payout clearance. Anomaly exception queues for human Claims Officers are reserved for Phase 2 roadmap audits.

#### Capstone Limitation
> Physical IoT hardware, live central banking rail transfers, and actuarial machine learning models are represented via robust software simulators and deterministic rule engines.

#### Core Strength & Differentiator
> The true differentiator is the frictionless, end-to-end straight-through flow: trusted external telemetry directly generates verified claims and instant compensation with zero human latency or adjuster bias.

---

## 13. Algorithmic Engines & Business Logic Specifications

### 13.1 Underwriting Risk Calculator Engine (`RiskCalculator.java`)
The Underwriting Risk Engine evaluates the farm's agronomic and hydrological risk profile to compute risk loading and premium rates.

#### Factor Scoring Formula:
$$\text{Total Score} = S_{\text{avail}} + S_{\text{equip}} + S_{\text{fail}} + S_{\text{season}} + S_{\text{backup}}$$

| Factor | Condition / Metric | Points Contributed |
| :--- | :--- | :---: |
| **Water Availability ($S_{\text{avail}}$)** | $\ge 85\%$ | 0 pts |
| | $70\% \text{ to } 84.99\%$ | 10 pts |
| | $50\% \text{ to } 69.99\%$ | 20 pts |
| | $< 50\%$ (or missing) | 30 pts |
| **Equipment Condition ($S_{\text{equip}}$)** | `GOOD` | 0 pts |
| | `FAIR` | 10 pts |
| | `POOR` / unknown | 20 pts |
| **Historical Failures ($S_{\text{fail}}$)** | 0 historical failures | 0 pts |
| | 1 to 2 historical failures | 10 pts |
| | $> 2$ historical failures | 20 pts |
| **Seasonal Drought Risk ($S_{\text{season}}$)** | `LOW` | 0 pts |
| | `MEDIUM` | 10 pts |
| | `HIGH` | 15 pts |
| | `VERY_HIGH` | 20 pts |
| **Backup Water Source ($S_{\text{backup}}$)** | Valid secondary source present | 0 pts |
| | `NONE` or blank | 10 pts |

#### Risk Classification & Premium Loading Matrix:
| Total Risk Score | Risk Classification | Premium Loading (%) | Recommended Premium Formula |
| :---: | :---: | :---: | :--- |
| **0 to 25** | `LOW` | **+5%** | $\text{Base Premium} \times 1.05$ |
| **26 to 50** | `MODERATE` | **+15%** | $\text{Base Premium} \times 1.15$ |
| **51 to 75** | `HIGH` | **+30%** | $\text{Base Premium} \times 1.30$ |
| **76 to 100** | `VERY_HIGH` | **+50%** | $\text{Base Premium} \times 1.50$ |

#### Recommended Coverage Clamping:
$$\text{Recommended Coverage} = \min(\max(\text{Requested Coverage}, \text{Plan Min}), \text{Plan Max})$$

---

### 13.2 Parametric Deficit Settlement & Deduplication Engine (`EventClaimsServiceImpl.java`)
When telemetry is ingested (`POST /api/readings`), it is compared against the parametric trigger threshold dynamically configured in Policy Service:

#### Dynamic Evaluation & Settlement Workflow:
1. **Dynamic Trigger Threshold**: Reading availability percentage is evaluated against `config.triggerRule().availabilityThresholdPercentage()` (configured in Policy Service; defaults to 70.00% if no custom rule exists).
   - If Availability $\ge 	ext{Threshold} \implies$ Normal reading, returns `NO_CLAIM` / `NOT_REQUIRED`.
2. **Event-Based Deduplication**: If Availability $< 	ext{Threshold}$, the platform extracts `eventDate = recordedAt.toLocalDate()`.
   - Checks `claimRepository.existsByPolicyIdAndEventDateAndStatus(policyId, eventDate, "SETTLED")`.
   - If a settled claim already exists for this event date, rejects with HTTP 409 `EVENT_CLAIM_ALREADY_EXISTS`. Subsequent readings on distinct calendar dates trigger separate event claims.
3. **Configured Payout Slabs**: Payout percentage is matched against active slabs `[min_availability_pct, max_availability_pct)` for the trigger rule. If no custom slabs are configured, falls back to default severity tiers:

#### Default Severity Tier Table:
| Water Availability (%) | Trigger Status | Default Payout Tier (%) | Indemnity Compensation Formula | Payout & Claim Status |
| :---: | :---: | :---: | :--- | :---: |
| **$\ge 70.00\%$** | Normal (No Breach) | **0%** | $0.00$ | `NO_CLAIM` / `NOT_REQUIRED` |
| **$50.00\% \text{ to } 69.99\%$** | Minor Deficit (Tier 3) | **10.00%** | $\text{Coverage Amount} \times 0.10$ | `SETTLED` / `SUCCESS` |
| **$30.00\% \text{ to } 49.99\%$** | Moderate Deficit (Tier 2) | **25.00%** | $\text{Coverage Amount} \times 0.25$ | `SETTLED` / `SUCCESS` |
| **$< 30.00\%$** | Severe Drought (Tier 1) | **50.00%** | $\text{Coverage Amount} \times 0.50$ | `SETTLED` / `SUCCESS` |

4. **Aggregate Policy Coverage Limiting**:
   $$\text{Remaining Coverage} = \text{Policy Coverage Amount} - \sum \text{Settled Payouts}$$
   $$\text{Actual Indemnity Compensation} = \min(\text{Calculated Payout}, \text{Remaining Coverage})$$
   - If $\text{Remaining Coverage} \le 0$, rejects with HTTP 400 `COVERAGE_EXHAUSTED`.

---

## 14. Frontend Integration Architecture (Angular 21 + NgRx)

### 14.1 Enterprise Directory Structure
```
irrigashield-frontend/src/app/
├── core/
│   ├── auth/              # Global auth interceptor & cookie credential handling
│   ├── layout/            # Navbar, Sidebar, Footer, Role-based navigation
│   └── guards/            # RoleGuard, AuthGuard
├── features/
│   ├── auth/              # Login, Register, Profile components & state
│   ├── farmer/            # Farm creation, Crop form, Irrigation setup, My Farms
│   ├── underwriter/       # Review queue, Risk assessment view, Approval dialog
│   ├── claims/            # Claims list, Compensation breakdown, Payout receipts
│   └── admin/             # Plan management, User admin, Trigger rule forms
├── shared/
│   ├── components/        # Metric cards, Alert banners, Status badges
│   ├── directives/        # DigitOnly, DecimalFormat
│   └── validators/        # PostalCode, Acreage, DateRange validators
└── app.routes.ts          # Lazy-loaded standalone routes with CanActivate guards
```

### 14.2 HTTP Cookie Interceptor (`withCredentials: true`)
Because the backend uses HttpOnly cookies (`IRRIGASHIELD_TOKEN`), all Angular HTTP client requests must include credentials:
```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const cloned = req.clone({
    withCredentials: true
  });
  return next(cloned);
};
```

---

## 15. Backend Improvements & Future Roadmap

As documented in `BACKEND_IMPROVEMENTS_ROADMAP.md`, future enhancements will evolve IrrigaShield from capstone excellence to commercial production grade:

```mermaid
flowchart LR
    P1["Phase 1: Dual Auth<br/>(Bearer Token + Cookie)"] --> P2["Phase 2: Observability<br/>(Micrometer + Zipkin)"]
    P2 --> P3["Phase 3: Event Streaming<br/>(Apache Kafka Telemetry)"]
    P3 --> P4["Phase 4: Weather Oracles<br/>(Automated Satellite APIs)"]
```

1. **Phase 1: Dual Authentication (Bearer Token + Cookie):** Update `JwtAuthenticationFilter` in `api-gateway` to support both `Authorization: Bearer <token>` and `IRRIGASHIELD_TOKEN` cookie. Unlocks Swagger UI, SoapUI, and native mobile testing.
2. **Phase 2: Distributed Tracing & Circuit Breakers:** Integrate Spring Cloud Circuit Breaker (Resilience4j) on `WebClient` inter-service calls and Micrometer Tracing with Zipkin for distributed request tracing.
3. **Phase 3: High-Scale IoT Event Streaming:** Introduce Apache Kafka or RabbitMQ broker between external telemetry sources and `event-claims-service` to ingest thousands of sensor events per second asynchronously.
4. **Phase 4: External Weather Oracle Integration:** Connect OpenWeatherMap, Copernicus Sentinel-1 SAR, or ECMWF rainfall oracles to automate telemetry validation with satellite-derived soil moisture.

---

*Document compiled and validated against Capstone-IrrigaSheild code repository and IrrigaShield_main_plan.xlsx.*