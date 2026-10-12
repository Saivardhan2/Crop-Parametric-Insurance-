# -*- coding: utf-8 -*-
"""
Builder for IRRIGASHIELD_UPDATED_MAIN_PLAN.md
Processes excel_dump.json and correlates with Capstone-IrrigaSheild code.
"""

import json
import os

with open('excel_dump.json', 'r', encoding='utf-8') as f:
    dump = json.load(f)

import generate_plan
STAGE_MAPPING = generate_plan.STAGE_MAPPING

doc = []

def add(text=""):
    doc.append(text)

add("# IrrigaShield: Master Project Plan & Architecture Blueprint")
add("**Parametric Agricultural Micro-Insurance Platform**")
add("")
add("> **Document Version:** 2.0.0 (Master Synchronized Plan)  ")
add("> **Baseline Artifacts:** `IrrigaShield_main_plan.xlsx` (All 15 Sheets) & `Capstone-IrrigaSheild` (Spring Boot Reactive Backend)  ")
add("> **Status:** Complete Master Reference & Implementation Blueprint  ")
add("> **Generated:** October 2026  ")
add("")
add("---")
add("")
add("## Table of Contents")
add("1. [Executive Summary & Architectural Modernization](#1-executive-summary--architectural-modernization)")
add("2. [System Topography & Reactive Microservices Architecture](#2-system-topography--reactive-microservices-architecture)")
add("3. [Security Architecture & Gateway Authentication](#3-security-architecture--gateway-authentication)")
add("4. [Global Domain Enums, Constants & Conventions](#4-global-domain-enums-constants--conventions)")
add("5. [Master End-to-End Lifecycle: All 75 Stages (`over_all`)](#5-master-end-to-end-lifecycle-all-75-stages-over_all)")
add("6. [Persona-Driven Execution Plans](#6-persona-driven-execution-plans)")
add("   - [6.1 Farmer Persona Plan (`farmer`)](#61-farmer-persona-plan-farmer)")
add("   - [6.2 Underwriter Persona Plan (`underwriter`)](#62-underwriter-persona-plan-underwriter)")
add("   - [6.3 Claims Officer Persona Plan (`claims_officer`)](#63-claims-officer-persona-plan-claims_officer)")
add("   - [6.4 Data Provider Persona Plan (`data_provider`)](#64-data-provider-persona-plan-data_provider)")
add("   - [6.5 Administrator Persona Plan (`admin`)](#65-administrator-persona-plan-admin)")
add("7. [Domain Service Specifications](#7-domain-service-specifications)")
add("   - [7.1 Policy & Underwriting Domain (`policy`)](#71-policy--underwriting-domain-policy)")
add("   - [7.2 Parametric Telemetry Domain (`parametric_event`)](#72-parametric-telemetry-domain-parametric_event)")
add("   - [7.3 Claims Processing Domain (`claims`)](#73-claims-processing-domain-claims)")
add("   - [7.4 Payment & Payout Domain (`payment`)](#74-payment--payout-domain-payment)")
add("8. [Database Architecture & Microservice Mappings (`Db_mappings`)](#8-database-architecture--microservice-mappings-db_mappings)")
add("9. [Comprehensive Data Dictionary & Production Schemas (`schemas`)](#9-comprehensive-data-dictionary--production-schemas-schemas)")
add("10. [Capstone Evaluation & Technical Competency Matrix (`Evaluation_Mapping`)](#10-capstone-evaluation--technical-competency-matrix-evaluation_mapping)")
add("11. [Live Demonstration Script & End-to-End Walkthrough (`Demo_Journey`)](#11-live-demonstration-script--end-to-end-walkthrough-demo_journey)")
add("12. [Architectural Principles & Governance Notes (`Notes`)](#12-architectural-principles--governance-notes-notes)")
add("13. [Algorithmic Engines & Business Logic Specifications](#13-algorithmic-engines--business-logic-specifications)")
add("   - [13.1 Underwriting Risk Calculator Engine](#131-underwriting-risk-calculator-engine)")
add("   - [13.2 Parametric Deficit Settlement Engine](#132-parametric-deficit-settlement-engine)")
add("14. [Frontend Integration Architecture (Angular 21 + NgRx)](#14-frontend-integration-architecture-angular-21--ngrx)")
add("15. [Backend Improvements & Future Roadmap](#15-backend-improvements--future-roadmap)")
add("")
add("---")
add("")

# 1. Executive Summary & Architectural Modernization
add("## 1. Executive Summary & Architectural Modernization")
add("")
add("### 1.1 Platform Mission")
add("**IrrigaShield** is an enterprise-grade parametric micro-insurance platform that safeguards smallholder and commercial farmers against catastrophic yield loss caused by irrigation deficits, canal supply shortfalls, groundwater depletion, and equipment downtime.")
add("")
add("Unlike traditional indemnity insurance—which relies on subjective damage assessments, adjusters traveling to remote fields, extensive paperwork, and claims disputes lasting months—IrrigaShield operates on **objective, verifiable parametric triggers**. When authorized telemetry providers submit water availability indices that breach contractually defined thresholds during critical crop growth stages, the platform **automatically validates eligibility, calculates indemnity compensation, and executes settlement without manual intervention**.")
add("")
add("### 1.2 Evolution: Planned vs. Implemented Architecture")
add("In the original concept plan (`IrrigaShield_main_plan.xlsx`), the system was conceived as 8 discrete synchronous microservices communicating with traditional blocking JPA/Hibernate databases. During backend development (`Capstone-IrrigaSheild`), this architecture was re-engineered into **4 high-performance reactive domain microservices** running on **Spring WebFlux, Project Reactor, Netty, and MySQL R2DBC**, coordinated by **Spring Cloud API Gateway** and **Netflix Eureka**.")
add("")
add("| Dimension | Original Planned Design (`IrrigaShield_main_plan.xlsx`) | Implemented Backend Reality (`Capstone-IrrigaSheild`) | Engineering Rationale |")
add("| :--- | :--- | :--- | :--- |")
add("| **Concurrency & Runtime** | Synchronous Spring MVC, blocking thread-per-request | **Reactive Spring WebFlux, Project Reactor (`Mono`/`Flux`), Netty event loop** | High-throughput non-blocking I/O capable of handling thousands of concurrent telemetry sensor submissions with minimal memory footprint. |")
add("| **Database Access** | Blocking Spring Data JPA / Hibernate | **Spring Data R2DBC (`asyncer-r2dbc-mysql`)** | True end-to-end reactive pipeline from HTTP request to database driver without thread starvation. |")
add("| **Microservice Count** | 8 microservices (Auth, Farm, Underwriter, Policy, Event, Claims, Payment, Notification) | **4 Domain Services + 2 Platform Services** (`api-gateway`, `eureka-server`, `auth-service`, `farm-service`, `policy-service`, `event-claims-service`) | Consolidated tightly coupled subdomains (Underwriting into Policy; Events, Claims & Payouts into Event-Claims) eliminating redundant network hops and distributed 2PC transactions. |")
add("| **Security & Identity** | Header-based JWT / RBAC | **HttpOnly Cookie (`IRRIGASHIELD_TOKEN`) + Gateway Header Injection (`X-User-Id`, `X-User-Email`, `X-User-Roles`)** | Immune to browser Cross-Site Scripting (XSS) token theft while microservices remain stateless, verifying incoming requests via `TrustedHeaderAuthenticationFilter`. |")
add("| **Inter-Service Communication** | OpenFeign / Synchronous REST | **Reactive Spring `WebClient`** | Non-blocking reactive RPC for cross-service risk profile queries and policy state verification. |")
add("| **Claim Settlement Workflow** | Multi-step manual claims queue with human adjuster reviews | **Straight-Through Processing (STP) Parametric Engine** | Instant, algorithmic compensation calculation and payout clearance directly upon telemetry deficit detection. |")
add("")
add("---")
add("")

# 2. System Topography
add("## 2. System Topography & Reactive Microservices Architecture")
add("")
add("### 2.1 Reactive Architecture Topology")
add("```mermaid")
add("flowchart TD")
add("    Client[\"Frontend Web Client (Angular 21 + Tailwind)<br/>Port 4200 (Credentials: Include)\"] -->|HTTP Cookie: IRRIGASHIELD_TOKEN| Gateway[\"API Gateway (Spring Cloud Gateway Reactive)<br/>Port 8080\"]")
add("    Gateway -.->|Service Discovery| Eureka[\"Eureka Service Registry<br/>Port 8761\"]")
add("    ")
add("    subgraph Platform Services [Platform & Infrastructure]")
add("        Eureka")
add("        Gateway")
add("    end")
add("    ")
add("    subgraph Core Domain Microservices [Reactive Domain Microservices (Netty + R2DBC)]")
add("        Auth[\"auth-service (Port 8081)<br/>Users & Identity\"]")
add("        Farm[\"farm-service (Port 8082)<br/>Profiles, Farms, Crops & Irrigation\"]")
add("        Policy[\"policy-service (Port 8083)<br/>Plans, Trigger Rules, Applications & Underwriting\"]")
add("        EventClaims[\"event-claims-service (Port 8084)<br/>Telemetry, Parametric Engine, Claims & Payouts\"]")
add("    end")
add("    ")
add("    subgraph Databases [Isolated R2DBC MySQL Databases]")
add("        DB_Auth[(\"irrigashield_auth_db<br/>MySQL 3306\")]")
add("        DB_Farm[(\"irrigashield_farm_db<br/>MySQL 3306\")]")
add("        DB_Policy[(\"irrigashield_policy_db<br/>MySQL 3306\")]")
add("        DB_Claims[(\"irrigashield_event_claims_db<br/>MySQL 3306\")]")
add("    end")
add("    ")
add("    Gateway -->|/api/auth/**, /api/users/**, /api/admin/users/**| Auth")
add("    Gateway -->|/api/farmers/**, /api/farms/**| Farm")
add("    Gateway -->|/api/plans/**, /api/applications/**, /api/policies/**, /api/admin/plans/**, /api/underwriting/**| Policy")
add("    Gateway -->|/api/readings/**, /api/claims/**| EventClaims")
add("    ")
add("    Auth --> DB_Auth")
add("    Farm --> DB_Farm")
add("    Policy --> DB_Policy")
add("    EventClaims --> DB_Claims")
add("    ")
add("    Policy -.->|WebClient: GET /api/farms/{farmId}/risk-profile| Farm")
add("    EventClaims -.->|WebClient: GET /api/internal/policies/{policyId}| Policy")
add("```")
add("")
add("### 2.2 Microservice Port & Registry Directory")
add("| Service Name | Eureka Service ID | Port | Context Path | Primary Database | Key Responsibilities |")
add("| :--- | :--- | :---: | :--- | :--- | :--- |")
add("| **Eureka Server** | `EUREKA-SERVER` | `8761` | `/` | N/A | Central dynamic service registration and health heartbeats. |")
add("| **API Gateway** | `API-GATEWAY` | `8080` | `/` | N/A | Single entrypoint, CORS termination, JWT cookie extraction, trusted header propagation, route dispatching. |")
add("| **Auth Service** | `AUTH-SERVICE` | `8081` | `/` | `irrigashield_auth_db` | User registration, authentication, JWT cookie generation, admin user management. |")
add("| **Farm Service** | `FARM-SERVICE` | `8082` | `/` | `irrigashield_farm_db` | Farmer KYC profiles, farm land parcels, crop phenology, irrigation equipment profiling, risk profile extraction. |")
add("| **Policy Service** | `POLICY-SERVICE` | `8083` | `/` | `irrigashield_policy_db` | Insurance plans, trigger rules, payout slabs, policy applications, underwriting risk engine (`RiskCalculator`), quote acceptance, active policies. |")
add("| **Event Claims Service** | `EVENT-CLAIMS-SERVICE` | `8084` | `/` | `irrigashield_event_claims_db` | Telemetry sensor ingestion, parametric deficit evaluation, automated claim generation, straight-through payout calculation and settlement. |")
add("")
add("---")
add("")

# 3. Security Architecture
add("## 3. Security Architecture & Gateway Authentication")
add("")
add("### 3.1 Security Model Principles")
add("IrrigaShield uses a **zero-trust boundary security model**:")
add("1. **Browser Security (XSS Defense):** Authentication tokens are stored strictly in an `HttpOnly`, `SameSite=Lax`, `Path=/` cookie named `IRRIGASHIELD_TOKEN`. JavaScript client code running in the browser cannot access the raw JWT, completely eliminating token theft via Cross-Site Scripting (XSS).")
add("2. **Gateway Verification:** The `api-gateway` reactive filter (`JwtAuthenticationFilter`) intercepts every incoming HTTP request, validates the cryptographic signature of the cookie, extracts claims (`userId`, `email`, `role`), and strips any untrusted external `X-User-*` headers.")
add("3. **Downstream Header Injection:** The Gateway injects trusted internal headers before forwarding the request to internal microservices:")
add("   - `X-User-Id: <Long>` (e.g., `1`)")
add("   - `X-User-Email: <String>` (e.g., `farmer@irrigashield.com`)")
add("   - `X-User-Roles: <String>` (e.g., `ROLE_FARMER`)")
add("4. **Service-Level Authorization:** Each downstream microservice applies `TrustedHeaderAuthenticationFilter` which parses these trusted headers and populates Spring's `ReactiveSecurityContextHolder` with an `AuthenticatedUser` principal. Method-level security is enforced via `@PreAuthorize(\"hasRole('...')\")`.")
add("")
add("### 3.2 Pre-Configured Bootstrap Demonstration Accounts")
add("| Role | Email | Password | Allowed Capabilities |")
add("| :--- | :--- | :--- | :--- |")
add("| **FARMER** | `farmer@irrigashield.com` | `Farmer@123` | Farm registration, crop/irrigation setup, plan browsing, policy application, quote acceptance, claims tracking. |")
add("| **UNDERWRITER** | `underwriter@irrigashield.com` | `Underwriter@123` | Queue inspection, farm risk profiling, algorithmic risk assessment, application approval/rejection. |")
add("| **DATA_PROVIDER** | `dataprovider@irrigashield.com` | `Provider@123` | Sensor telemetry data submission (`POST /api/readings`). |")
add("| **CLAIMS_OFFICER** | `claims@irrigashield.com` | `Claims@123` | Claims auditing, exception dispute management, manual payout escalation. |")
add("| **ADMIN** | `admin@irrigashield.com` | `Admin@123` | User account creation, insurance plan configuration, trigger rule and payout slab management. |")
add("")
add("---")
add("")

# 4. Global Domain Enums, Constants & Conventions
add("## 4. Global Domain Enums, Constants & Conventions")
add("")
add("### 4.1 System Enums")
add("```typescript")
add("// Platform Persona Roles")
add("type Role = 'FARMER' | 'UNDERWRITER' | 'CLAIMS_OFFICER' | 'DATA_PROVIDER' | 'ADMIN';")
add("")
add("// Farm & Land Ownership")
add("type OwnershipType = 'OWNED' | 'LEASED' | 'SHARED';")
add("")
add("// Crop Phenological Stages")
add("type GrowthStage = 'SOWING' | 'GERMINATION' | 'VEGETATIVE' | 'FLOWERING' | 'FRUITING' | 'MATURITY';")
add("")
add("// Irrigation Infrastructure & Hydrological Source")
add("type IrrigationRequirement = 'LOW' | 'MEDIUM' | 'HIGH';")
add("type WaterSource = 'BOREWELL' | 'CANAL' | 'WELL' | 'RIVER' | 'RESERVOIR' | 'TANKER' | 'RAINWATER';")
add("type BackupWaterSource = WaterSource | 'NONE';")
add("type IrrigationMethod = 'DRIP' | 'SPRINKLER' | 'FLOOD' | 'FURROW' | 'MANUAL';")
add("type EquipmentCondition = 'GOOD' | 'FAIR' | 'POOR';")
add("type SeasonalRisk = 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';")
add("type MaintenanceFrequency = 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'NONE';")
add("")
add("// Underwriting & Policy Application Lifecycle")
add("type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';")
add("type UnderwritingDecision = 'APPROVED' | 'REJECTED';")
add("type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED';")
add("type PaymentStatus = 'PAID' | 'PENDING' | 'FAILED';")
add("")
add("// Telemetry & Parametric Claims Settlement")
add("type ClaimStatus = 'SETTLED' | 'NO_CLAIM';")
add("type PayoutStatus = 'SUCCESS' | 'NOT_REQUIRED';")
add("type RiskLevel = 'LOW' | 'MODERATE' | 'HIGH' | 'VERY_HIGH';")
add("```")
add("")
add("### 4.2 Standard Reactive API Error Response Envelope (`ApiErrorResponse`)")
add("All microservices adhere to a consistent error schema handled by `@RestControllerAdvice`:")
add("```json")
add("{")
add('  "status": 400,')
add('  "errorCode": "VALIDATION_FAILED",')
add('  "message": "Request validation failed",')
add('  "path": "/api/farms",')
add('  "timestamp": "2026-10-06T14:30:00",')
add('  "validationErrors": {')
add('    "postalCode": "Postal code must be a valid 6-digit PIN code",')
add('    "areaInAcres": "Farm area must be greater than zero"')
add("  }")
add("}")
add("```")
add("")
add("---")
add("")

# Section 5: Master End-to-End Lifecycle: All 75 Stages
add("## 5. Master End-to-End Lifecycle: All 75 Stages (`over_all`)")
add("")
add("This master table correlates every single stage from the initial planning spreadsheet (`IrrigaShield_main_plan.xlsx`) with the active reactive microservices implementation in `Capstone-IrrigaSheild`.")
add("")
add("| Stage | Persona | Action | Goal | Angular Component | Planned API (Excel) | Implemented Backend API | Microservice & Database Table | Status & Implementation Details |")
add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |")

for r in dump['over_all'][1:]:
    stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
    m = STAGE_MAPPING.get(stage, {
        "backend_ep": planned_ep,
        "service": planned_svc,
        "db_table": planned_db,
        "status": "[PLANNED]",
        "notes": "Correlated from master plan."
    })
    
    clean_goal = goal.replace("|", "/")
    clean_notes = m["notes"].replace("|", "/")
    
    row_str = f"| **{stage}** | {persona} | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}`<br/>`{m['db_table']}` | **{m['status']}**<br/>{clean_notes} |"
    add(row_str)

add("")
add("---")
add("")

# Section 6: Persona-Driven Execution Plans
add("## 6. Persona-Driven Execution Plans")
add("")

# 6.1 Farmer
add("### 6.1 Farmer Persona Plan (`farmer`)")
add("The **Farmer** is the central consumer of parametric protection. Farmers self-register, manage multi-parcel land holdings, profile irrigation assets, apply for coverage, accept algorithmic quotes, and track automated straight-through claim settlements.")
add("")
add("| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |")
add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |")
for r in dump['farmer'][1:]:
    stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
    m = STAGE_MAPPING.get(stage, {
        "backend_ep": planned_ep,
        "service": planned_svc,
        "db_table": planned_db,
        "status": "[PLANNED]",
        "notes": ""
    })
    clean_goal = goal.replace("|", "/")
    row_str = f"| **{stage}** | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}` | **{m['status']}** |"
    add(row_str)
add("")

# 6.2 Underwriter
add("### 6.2 Underwriter Persona Plan (`underwriter`)")
add("The **Underwriter** evaluates incoming policy applications. In the updated architecture, Underwriting is integrated directly into `policy-service` under `/api/underwriting/**`. Underwriters inspect pending queues, trigger the automated risk scoring engine (`RiskCalculator`), review recommendations, and render binding decisions.")
add("")
add("| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |")
add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |")
for r in dump['underwriter'][1:]:
    stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
    m = STAGE_MAPPING.get(stage, {
        "backend_ep": planned_ep,
        "service": planned_svc,
        "db_table": planned_db,
        "status": "[PLANNED]",
        "notes": ""
    })
    clean_goal = goal.replace("|", "/")
    row_str = f"| **{stage}** | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}` | **{m['status']}** |"
    add(row_str)
add("")

# 6.3 Claims Officer
add("### 6.3 Claims Officer Persona Plan (`claims_officer`)")
add("Under pure parametric insurance, valid claims settle automatically via Straight-Through Processing (STP) without human adjusters. The **Claims Officer** persona is reserved for auditing telemetry disputes, reviewing anomalous readings, and manual overrides.")
add("")
add("| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |")
add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |")
for r in dump['claims_officer'][1:]:
    stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
    m = STAGE_MAPPING.get(stage, {
        "backend_ep": planned_ep,
        "service": planned_svc,
        "db_table": planned_db,
        "status": "[PLANNED]",
        "notes": ""
    })
    clean_goal = goal.replace("|", "/")
    row_str = f"| **{stage}** | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}` | **{m['status']}** |"
    add(row_str)
add("")

# 6.4 Data Provider
add("### 6.4 Data Provider Persona Plan (`data_provider`)")
add("The **Data Provider** is an authorized external telemetry authority (e.g., automated irrigation canal sensors, soil moisture telemetry, satellite SAR indices). They push verified readings via secured endpoints requiring `ROLE_DATA_PROVIDER`.")
add("")
add("| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |")
add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |")
for r in dump['data_provider'][1:]:
    stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
    m = STAGE_MAPPING.get(stage, {
        "backend_ep": planned_ep,
        "service": planned_svc,
        "db_table": planned_db,
        "status": "[PLANNED]",
        "notes": ""
    })
    clean_goal = goal.replace("|", "/")
    row_str = f"| **{stage}** | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}` | **{m['status']}** |"
    add(row_str)
add("")

# 6.5 Admin
add("### 6.5 Administrator Persona Plan (`admin`)")
add("The **Admin** manages platform access, configures master insurance products, sets crop-specific trigger rules, defines duration payout slabs, and monitors operational health.")
add("")
add("| Stage | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |")
add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |")
for r in dump['admin'][1:]:
    stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
    m = STAGE_MAPPING.get(stage, {
        "backend_ep": planned_ep,
        "service": planned_svc,
        "db_table": planned_db,
        "status": "[PLANNED]",
        "notes": ""
    })
    clean_goal = goal.replace("|", "/")
    row_str = f"| **{stage}** | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}` | **{m['status']}** |"
    add(row_str)
add("")
add("---")
add("")

# Section 7: Service Domain Bounded Contexts
add("## 7. Domain Service Specifications")
add("")

def render_domain_section(domain_idx, domain_key, domain_title, description):
    add(f"### 7.{domain_idx} {domain_title} (`{domain_key}`)")
    add(description)
    add("")
    add("| Stage | Persona | Action | Goal | Angular Component | Planned API | Implemented Backend API | Microservice & Table | Status |")
    add("| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |")
    for r in dump[domain_key][1:]:
        stage, persona, action, goal, page, planned_ep, planned_svc, planned_db = r
        m = STAGE_MAPPING.get(stage, {
            "backend_ep": planned_ep,
            "service": planned_svc,
            "db_table": planned_db,
            "status": "[PLANNED]",
            "notes": ""
        })
        clean_goal = goal.replace("|", "/")
        row_str = f"| **{stage}** | {persona} | **{action}** | {clean_goal} | `{page}` | `{planned_ep}` | `{m['backend_ep']}` | `{m['service']}`<br/>`{m['db_table']}` | **{m['status']}** |"
        add(row_str)
    add("")

render_domain_section(1, "policy", "Policy & Underwriting Domain", "Covers master plan catalogue, crop phenology trigger rules, payout slabs, application submission, risk assessment, quote binding, and active policy issuance.")
render_domain_section(2, "parametric_event", "Parametric Telemetry Domain", "Covers telemetry ingestion from authorized data providers, range verification, policy lookup, threshold comparison, and parametric deficit event detection.")
render_domain_section(3, "claims", "Claims Settlement Domain", "Covers automated claim instantiation, Straight-Through Processing (STP) validation, graduated compensation tier calculation, and claim closure.")
render_domain_section(4, "payment", "Payment & Payout Domain", "Covers simulated premium collection upon quote acceptance, automated payout instruction dispatching, and straight-through settlement execution.")

add("---")
add("")

# Section 8: Db_mappings
add("## 8. Database Architecture & Microservice Mappings (`Db_mappings`)")
add("")
add("### 8.1 Planned vs. Production Database Topology")
add("The table below details how the 8 originally planned databases were consolidated into **4 production-grade reactive MySQL databases** running with Spring Data R2DBC.")
add("")
add("| Service Name | Planned Database | Production R2DBC Database | Port | Production MySQL Tables | Architectural Status & Rationale |")
add("| :--- | :--- | :--- | :---: | :--- | :--- |")
add("| **Auth Service** | `auth_db` | `irrigashield_auth_db` | 8081 | `users` | **[ACTIVE]** Cleanly isolates platform identities, BCrypt credentials, and role assignments (`ROLE_FARMER`, `ROLE_UNDERWRITER`, `ROLE_DATA_PROVIDER`, `ROLE_CLAIMS_OFFICER`, `ROLE_ADMIN`). |")
add("| **Farm Service** | `farm_db` | `irrigashield_farm_db` | 8082 | `farmer_profiles`, `farms`, `crops`, `irrigation_profiles` | **[ACTIVE]** Central repository for all agricultural, hydrological, and agronomic assets. Provides risk profile aggregation API (`/api/farms/{farmId}/risk-profile`). |")
add("| **Policy Service** | `policy_db` + `underwriter_db` | `irrigashield_policy_db` | 8083 | `insurance_plans`, `trigger_rules`, `payout_slabs`, `policy_applications`, `underwriting_cases`, `policies` | **[CONSOLIDATED]** Unified policy lifecycle and underwriting engine into a single high-cohesion bounded context. Eliminates distributed transactions during application underwriting and policy activation. |")
add("| **Event Claims Service** | `event_db` + `claims_db` + `payment_db` | `irrigashield_event_claims_db` | 8084 | `irrigation_readings`, `claims` | **[CONSOLIDATED]** Combines telemetry ingestion, parametric threshold breach evaluation, claim generation, and immediate settlement into an event-driven straight-through reactive pipeline. |")
add("| **Payment Service** | `payment_db` | Embedded in `policy_db` & `event_claims_db` | Embedded | `policies.payment_status`, `claims.payout_status` | **[EMBEDDED]** Simulated financial transactions execute directly inside policy activation and claim settlement transactions. Standalone gateway reserved for Phase 3. |")
add("| **Notification Service**| `notification_db` | State Fields across all services | N/A | State audit trails | **[ROADMAP]** Real-time domain status updates provide immediate visibility. Dedicated WebSocket/SSE push notification service planned for Phase 2. |")
add("")
add("---")
add("")

# Section 9: Schemas
add("## 9. Comprehensive Data Dictionary & Production Schemas (`schemas`)")
add("")
add("### 9.1 Table Evolution Matrix (All 23 Planned Tables vs 10 Production Tables)")
add("")
add("| # | Planned Table (Excel) | Key Columns (Excel Plan) | Production Table (`Capstone-IrrigaSheild`) | Production Database | Schema Status & Mapping Notes |")
add("| :-: | :--- | :--- | :--- | :--- | :--- |")

SCHEMA_TABLE_MAPPING = [
    ("1", "users", "user_id (PK), name, email (UQ), password_hash, phone, role, status, created_at, updated_at", "users", "irrigashield_auth_db", "[ACTIVE] Fully implemented. Role values: FARMER, UNDERWRITER, CLAIMS_OFFICER, DATA_PROVIDER, ADMIN."),
    ("2", "farmers", "farmer_id (PK), user_id, address, bank_account_masked, verification_status", "farmer_profiles", "irrigashield_farm_db", "[ACTIVE] Implemented as farmer_profiles. Links to auth_user_id; stores KYC, address, bank_account_last_four, verification_status."),
    ("3", "farms", "farm_id (PK), farmer_id, farm_name, location, area_acres, latitude, longitude, status", "farms", "irrigashield_farm_db", "[ACTIVE] Implemented as farms. Stores survey_number, address, area_in_acres, soil_type, ownership_type, lat/lng, status."),
    ("4", "crops", "crop_id (PK), farm_id, crop_type, variety, sowing_date, expected_harvest_date, current_growth_stage", "crops", "irrigashield_farm_db", "[ACTIVE] Implemented as crops. Stores crop_type, variety, sowing_date, expected_harvest_date, growth_stage, irrigation_requirement."),
    ("5", "irrigation_profiles", "irrigation_id (PK), farm_id, water_source, irrigation_method, equipment_condition, historical_failures, seasonal_risk", "irrigation_profiles", "irrigashield_farm_db", "[ACTIVE] Implemented. Stores water_source, irrigation_method, equipment_condition, backup_water_source, historical_failure_count, average_water_availability_pct, seasonal_risk, maintenance_frequency."),
    ("6", "applications", "application_id (PK), farmer_id, farm_id, crop_id, plan_id, status, submitted_at, accepted_quote_at", "policy_applications", "irrigashield_policy_db", "[ACTIVE] Implemented as policy_applications. Stores farmer_user_id, farm_id, crop_id, plan_id, requested_coverage, proposed_start_date, proposed_end_date, status, submitted_at."),
    ("7", "risk_assessments", "assessment_id (PK), application_id, factor scores, overall_score, risk_level, assessed_at", "underwriting_cases", "irrigashield_policy_db", "[CONSOLIDATED] Consolidated into underwriting_cases table: water_availability_score, equipment_condition_score, historical_failure_score, seasonal_risk_score, backup_source_score, overall_risk_score, risk_level."),
    ("8", "premium_recommendations", "recommendation_id (PK), application_id, base_premium, risk_loading, crop_factor, seasonal_factor, final_premium", "underwriting_cases", "irrigashield_policy_db", "[CONSOLIDATED] Consolidated into underwriting_cases table: base_premium, risk_loading, recommended_premium, recommended_coverage."),
    ("9", "underwriting_decisions", "decision_id (PK), application_id, underwriter_id, decision, remarks, decided_at", "underwriting_cases", "irrigashield_policy_db", "[CONSOLIDATED] Consolidated into underwriting_cases table: decision, remarks, status, review_started_at, review_completed_at."),
    ("10", "insurance_plans", "plan_id (PK), name, description, coverage_min, coverage_max, base_premium, active", "insurance_plans", "irrigashield_policy_db", "[ACTIVE] Implemented. Stores plan_name, description, min_coverage_amount, max_coverage_amount, base_premium_rate, status."),
    ("11", "trigger_rules", "rule_id (PK), plan_id, crop_type, growth_stage, availability_threshold_pct, continuous_hours, window_days, cumulative_shortage_days", "trigger_rules", "irrigashield_policy_db", "[ACTIVE] Implemented. Stores plan_id, crop_type, growth_stage, availability_threshold_pct, minimum_continuous_hours, rolling_window_days, required_shortage_days, status."),
    ("12", "payout_slabs", "slab_id (PK), rule_id, duration_from_hours, duration_to_hours, payout_percentage", "payout_slabs", "irrigashield_policy_db", "[ACTIVE] Implemented. Stores trigger_rule_id, minimum_duration_hours, maximum_duration_hours, payout_percentage, status."),
    ("13", "policies", "policy_id (PK), policy_number (UQ), application_id, farmer_id, farm_id, crop_id, coverage_amount, premium_amount, start_date, end_date, status", "policies", "irrigashield_policy_db", "[ACTIVE] Implemented. Stores application_id, farmer_user_id, farm_id, crop_id, plan_id, coverage_amount, premium_amount, start_date, end_date, payment_status, status, accepted_at."),
    ("14", "data_providers", "provider_id (PK), provider_code (UQ), name, source_type, api_key_hash, status", "users (role=DATA_PROVIDER)", "irrigashield_auth_db", "[CONSOLIDATED] Managed as users with role=DATA_PROVIDER in irrigashield_auth_db."),
    ("15", "irrigation_readings", "reading_id (PK), provider_id, farm_id, availability_pct, water_availability_pct, pump_status, recorded_at, validation_status", "irrigation_readings", "irrigashield_event_claims_db", "[ACTIVE] Implemented. Stores policy_id, farm_id, availability_percentage, recorded_at, submitted_by, created_at."),
    ("16", "disruption_trackers", "tracker_id (PK), policy_id, breach_started_at, latest_reading_at, duration_hours, cumulative_shortage_hours, lowest_availability, status", "irrigation_readings", "irrigashield_event_claims_db", "[STREAMLINED] Streamlined directly into reactive reading evaluation pipeline; breach metrics recorded in reading and claim."),
    ("17", "parametric_events", "event_id (PK), event_reference (UQ), policy_id, farm_id, rule_id, started_at, triggered_at, duration_hours, event_status", "claims", "irrigashield_event_claims_db", "[CONSOLIDATED] Parametric breach instantly instantiates a verified claim record; event details embedded directly."),
    ("18", "claims", "claim_id (PK), claim_number (UQ), policy_id, event_id (UQ), status, auto_created_at, claims_officer_id, remarks", "claims", "irrigashield_event_claims_db", "[ACTIVE] Implemented. Stores reading_id, policy_id, farmer_user_id, farm_id, availability_percentage, payout_percentage, coverage_amount, compensation_amount, payout_status, status."),
    ("19", "claim_validations", "validation_id (PK), claim_id, policy_active, source_authorized, threshold_met, duration_met, duplicate_found, result", "claims", "irrigashield_event_claims_db", "[CONSOLIDATED] Validations executed in reactive business logic before claim persistence; duplicate check via claimRepository."),
    ("20", "compensations", "compensation_id (PK), claim_id, coverage_amount, payout_percentage, eligible_amount, approved_amount, calculated_at", "claims", "irrigashield_event_claims_db", "[CONSOLIDATED] Stored directly as coverage_amount, payout_percentage, compensation_amount in claims table."),
    ("21", "payments", "payment_id (PK), reference_type, reference_id, farmer_id, amount, transaction_reference, status, payment_date", "policies & claims", "irrigashield_policy_db & claims_db", "[EMBEDDED] Simulated payment reflected via policies.payment_status='PAID' and claims.payout_status='SUCCESS'."),
    ("22", "payouts", "payout_id (PK), claim_id, compensation_id, amount, transaction_reference, status, processed_at", "claims", "irrigashield_event_claims_db", "[CONSOLIDATED] Stored directly in claims table as payout_status='SUCCESS', status='SETTLED', settled_at=now()."),
    ("23", "notifications", "notification_id (PK), user_id, title, message, notification_type, read_status, created_at", "Domain State Fields", "All Services", "[ROADMAP] Live state exposed across application, policy, and claim records; dedicated notification bus in Phase 2 roadmap.")
]

for row in SCHEMA_TABLE_MAPPING:
    add(f"| {row[0]} | `{row[1]}` | {row[2]} | `{row[3]}` | `{row[4]}` | {row[5]} |")

add("")
add("### 9.2 Production DDL Schemas (Spring Data R2DBC)")
add("")
add("#### Database: `irrigashield_auth_db`")
add("```sql")
add("CREATE TABLE IF NOT EXISTS users (")
add("    user_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    name VARCHAR(100) NOT NULL,")
add("    email VARCHAR(150) NOT NULL,")
add("    password_hash VARCHAR(255) NOT NULL,")
add("    role VARCHAR(30) NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_users PRIMARY KEY (user_id),")
add("    CONSTRAINT uk_users_email UNIQUE (email),")
add("    INDEX idx_users_role (role),")
add("    INDEX idx_users_status (status)")
add(");")
add("```")
add("")
add("#### Database: `irrigashield_farm_db`")
add("```sql")
add("CREATE TABLE IF NOT EXISTS farmer_profiles (")
add("    farmer_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    auth_user_id BIGINT NOT NULL,")
add("    email VARCHAR(150) NOT NULL,")
add("    full_name VARCHAR(100) NOT NULL,")
add("    phone_number VARCHAR(15) NOT NULL,")
add("    address_line VARCHAR(200) NOT NULL,")
add("    village VARCHAR(100) NOT NULL,")
add("    district VARCHAR(100) NOT NULL,")
add("    state VARCHAR(100) NOT NULL,")
add("    postal_code VARCHAR(10) NOT NULL,")
add("    bank_account_last_four VARCHAR(4),")
add("    verification_status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_farmer_profiles PRIMARY KEY (farmer_id),")
add("    CONSTRAINT uk_farmer_profiles_auth_user UNIQUE (auth_user_id),")
add("    CONSTRAINT uk_farmer_profiles_email UNIQUE (email),")
add("    INDEX idx_farmer_profiles_district (district),")
add("    INDEX idx_farmer_profiles_verification_status (verification_status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS farms (")
add("    farm_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    farmer_id BIGINT NOT NULL,")
add("    farm_name VARCHAR(100) NOT NULL,")
add("    survey_number VARCHAR(50),")
add("    address_line VARCHAR(200) NOT NULL,")
add("    village VARCHAR(100) NOT NULL,")
add("    district VARCHAR(100) NOT NULL,")
add("    state VARCHAR(100) NOT NULL,")
add("    postal_code VARCHAR(10) NOT NULL,")
add("    area_in_acres DECIMAL(10, 2) NOT NULL,")
add("    soil_type VARCHAR(50) NOT NULL,")
add("    ownership_type VARCHAR(30) NOT NULL,")
add("    latitude DECIMAL(10, 7),")
add("    longitude DECIMAL(10, 7),")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_farms PRIMARY KEY (farm_id),")
add("    CONSTRAINT fk_farms_farmer FOREIGN KEY (farmer_id) REFERENCES farmer_profiles (farmer_id),")
add("    INDEX idx_farms_farmer_id (farmer_id),")
add("    INDEX idx_farms_district (district),")
add("    INDEX idx_farms_status (status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS crops (")
add("    crop_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    farm_id BIGINT NOT NULL,")
add("    crop_type VARCHAR(50) NOT NULL,")
add("    variety VARCHAR(100),")
add("    sowing_date DATE NOT NULL,")
add("    expected_harvest_date DATE NOT NULL,")
add("    growth_stage VARCHAR(30) NOT NULL,")
add("    irrigation_requirement VARCHAR(20) NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_crops PRIMARY KEY (crop_id),")
add("    CONSTRAINT fk_crops_farm FOREIGN KEY (farm_id) REFERENCES farms (farm_id),")
add("    INDEX idx_crops_farm_id (farm_id),")
add("    INDEX idx_crops_crop_type (crop_type),")
add("    INDEX idx_crops_status (status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS irrigation_profiles (")
add("    irrigation_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    farm_id BIGINT NOT NULL,")
add("    water_source VARCHAR(30) NOT NULL,")
add("    irrigation_method VARCHAR(30) NOT NULL,")
add("    equipment_condition VARCHAR(20) NOT NULL,")
add("    backup_water_source VARCHAR(30),")
add("    historical_failure_count INT NOT NULL DEFAULT 0,")
add("    average_water_availability_pct DECIMAL(5, 2) NOT NULL,")
add("    seasonal_risk VARCHAR(20) NOT NULL,")
add("    maintenance_frequency VARCHAR(20) NOT NULL,")
add("    last_maintenance_date DATE,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_irrigation_profiles PRIMARY KEY (irrigation_id),")
add("    CONSTRAINT uk_irrigation_profiles_farm UNIQUE (farm_id),")
add("    CONSTRAINT fk_irrigation_profiles_farm FOREIGN KEY (farm_id) REFERENCES farms (farm_id),")
add("    INDEX idx_irrigation_water_source (water_source),")
add("    INDEX idx_irrigation_seasonal_risk (seasonal_risk)")
add(");")
add("```")
add("")
add("#### Database: `irrigashield_policy_db`")
add("```sql")
add("CREATE TABLE IF NOT EXISTS insurance_plans (")
add("    plan_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    plan_name VARCHAR(100) NOT NULL,")
add("    description VARCHAR(500) NOT NULL,")
add("    min_coverage_amount DECIMAL(12, 2) NOT NULL,")
add("    max_coverage_amount DECIMAL(12, 2) NOT NULL,")
add("    base_premium_rate DECIMAL(12, 2) NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_insurance_plans PRIMARY KEY (plan_id),")
add("    CONSTRAINT uk_insurance_plans_name UNIQUE (plan_name),")
add("    INDEX idx_insurance_plans_status (status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS trigger_rules (")
add("    trigger_rule_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    plan_id BIGINT NOT NULL,")
add("    crop_type VARCHAR(50) NOT NULL,")
add("    growth_stage VARCHAR(30) NOT NULL,")
add("    availability_threshold_pct DECIMAL(5, 2) NOT NULL,")
add("    minimum_continuous_hours INT NOT NULL,")
add("    rolling_window_days INT NOT NULL,")
add("    required_shortage_days INT NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_trigger_rules PRIMARY KEY (trigger_rule_id),")
add("    CONSTRAINT fk_trigger_rules_plan FOREIGN KEY (plan_id) REFERENCES insurance_plans (plan_id),")
add("    CONSTRAINT uk_trigger_rule_plan_crop_stage UNIQUE (plan_id, crop_type, growth_stage),")
add("    INDEX idx_trigger_rules_plan_id (plan_id),")
add("    INDEX idx_trigger_rules_crop_type (crop_type),")
add("    INDEX idx_trigger_rules_status (status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS payout_slabs (")
add("    payout_slab_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    trigger_rule_id BIGINT NOT NULL,")
add("    minimum_duration_hours INT NOT NULL,")
add("    maximum_duration_hours INT,")
add("    payout_percentage DECIMAL(5, 2) NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_payout_slabs PRIMARY KEY (payout_slab_id),")
add("    CONSTRAINT fk_payout_slabs_trigger_rule FOREIGN KEY (trigger_rule_id) REFERENCES trigger_rules (trigger_rule_id),")
add("    CONSTRAINT uk_payout_slab_duration UNIQUE (trigger_rule_id, minimum_duration_hours),")
add("    INDEX idx_payout_slabs_trigger_rule (trigger_rule_id),")
add("    INDEX idx_payout_slabs_status (status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS policy_applications (")
add("    application_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    farmer_user_id BIGINT NOT NULL,")
add("    farmer_email VARCHAR(150) NOT NULL,")
add("    farm_id BIGINT NOT NULL,")
add("    crop_id BIGINT NOT NULL,")
add("    plan_id BIGINT NOT NULL,")
add("    requested_coverage DECIMAL(12, 2) NOT NULL,")
add("    proposed_start_date DATE NOT NULL,")
add("    proposed_end_date DATE NOT NULL,")
add("    status VARCHAR(30) NOT NULL,")
add("    submitted_at TIMESTAMP NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_policy_applications PRIMARY KEY (application_id),")
add("    CONSTRAINT fk_policy_applications_plan FOREIGN KEY (plan_id) REFERENCES insurance_plans (plan_id),")
add("    INDEX idx_applications_farmer (farmer_user_id),")
add("    INDEX idx_applications_farm (farm_id),")
add("    INDEX idx_applications_status (status)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS underwriting_cases (")
add("    case_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    application_id BIGINT NOT NULL,")
add("    underwriter_user_id BIGINT NOT NULL,")
add("    underwriter_email VARCHAR(150) NOT NULL,")
add("    water_availability_score INT,")
add("    equipment_condition_score INT,")
add("    historical_failure_score INT,")
add("    seasonal_risk_score INT,")
add("    backup_source_score INT,")
add("    overall_risk_score INT,")
add("    risk_level VARCHAR(20),")
add("    base_premium DECIMAL(12, 2),")
add("    risk_loading DECIMAL(12, 2),")
add("    recommended_premium DECIMAL(12, 2),")
add("    recommended_coverage DECIMAL(12, 2),")
add("    decision VARCHAR(20),")
add("    remarks VARCHAR(500),")
add("    status VARCHAR(30) NOT NULL,")
add("    review_started_at TIMESTAMP NULL,")
add("    review_completed_at TIMESTAMP NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_underwriting_cases PRIMARY KEY (case_id),")
add("    CONSTRAINT uk_underwriting_application UNIQUE (application_id),")
add("    CONSTRAINT fk_underwriting_application FOREIGN KEY (application_id) REFERENCES policy_applications (application_id),")
add("    INDEX idx_underwriting_status (status),")
add("    INDEX idx_underwriting_underwriter (underwriter_user_id)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS policies (")
add("    policy_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    application_id BIGINT NOT NULL,")
add("    farmer_user_id BIGINT NOT NULL,")
add("    farmer_email VARCHAR(150) NOT NULL,")
add("    farm_id BIGINT NOT NULL,")
add("    crop_id BIGINT NOT NULL,")
add("    plan_id BIGINT NOT NULL,")
add("    coverage_amount DECIMAL(12, 2) NOT NULL,")
add("    premium_amount DECIMAL(12, 2) NOT NULL,")
add("    start_date DATE NOT NULL,")
add("    end_date DATE NOT NULL,")
add("    payment_status VARCHAR(20) NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    accepted_at TIMESTAMP NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_policies PRIMARY KEY (policy_id),")
add("    CONSTRAINT uk_policies_application UNIQUE (application_id),")
add("    CONSTRAINT fk_policies_application FOREIGN KEY (application_id) REFERENCES policy_applications (application_id),")
add("    CONSTRAINT fk_policies_plan FOREIGN KEY (plan_id) REFERENCES insurance_plans (plan_id),")
add("    INDEX idx_policies_farmer (farmer_user_id),")
add("    INDEX idx_policies_status (status)")
add(");")
add("```")
add("")
add("#### Database: `irrigashield_event_claims_db`")
add("```sql")
add("CREATE TABLE IF NOT EXISTS irrigation_readings (")
add("    reading_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    policy_id BIGINT NOT NULL,")
add("    farm_id BIGINT NOT NULL,")
add("    availability_percentage DECIMAL(5, 2) NOT NULL,")
add("    recorded_at TIMESTAMP NOT NULL,")
add("    submitted_by BIGINT NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    CONSTRAINT pk_irrigation_readings PRIMARY KEY (reading_id),")
add("    INDEX idx_readings_policy (policy_id),")
add("    INDEX idx_readings_farm (farm_id)")
add(");")
add("")
add("CREATE TABLE IF NOT EXISTS claims (")
add("    claim_id BIGINT NOT NULL AUTO_INCREMENT,")
add("    reading_id BIGINT NOT NULL,")
add("    policy_id BIGINT NOT NULL,")
add("    farmer_user_id BIGINT NOT NULL,")
add("    farm_id BIGINT NOT NULL,")
add("    availability_percentage DECIMAL(5, 2) NOT NULL,")
add("    payout_percentage DECIMAL(5, 2) NOT NULL,")
add("    coverage_amount DECIMAL(12, 2) NOT NULL,")
add("    compensation_amount DECIMAL(12, 2) NOT NULL,")
add("    payout_status VARCHAR(20) NOT NULL,")
add("    status VARCHAR(20) NOT NULL,")
add("    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,")
add("    settled_at TIMESTAMP NULL,")
add("    CONSTRAINT pk_claims PRIMARY KEY (claim_id),")
add("    CONSTRAINT fk_claims_reading FOREIGN KEY (reading_id) REFERENCES irrigation_readings (reading_id),")
add("    CONSTRAINT uk_claims_reading UNIQUE (reading_id),")
add("    INDEX idx_claims_policy (policy_id),")
add("    INDEX idx_claims_farmer (farmer_user_id),")
add("    INDEX idx_claims_status (status)")
add(");")
add("```")
add("")
add("---")
add("")

# Section 10: Evaluation_Mapping
add("## 10. Capstone Evaluation & Technical Competency Matrix (`Evaluation_Mapping`)")
add("")
add("This matrix maps every evaluation category from `IrrigaShield_main_plan.xlsx` directly to concrete architectural implementations and codebase locations in `Capstone-IrrigaSheild`.")
add("")
add("| # | Category | Metric | How IrrigaShield Demonstrates It | Evidence / Codebase Location | Actual Status |")
add("| :-: | :--- | :--- | :--- | :--- | :---: |")

EVAL_MAPPINGS = [
    ("1", "Requirements & Planning", "Understanding of Project Requirements", "Parametric business workflow, personas (FARMER, UNDERWRITER, DATA_PROVIDER, ADMIN), trigger-to-claim lifecycle fully documented.", "API_DOCUMENTATION.md, FEATURE_DRIVEN_ANGULAR21_PLAN.md, IRRIGASHIELD_BACKEND_CONTEXT.md", "[VERIFIED / COMPLETE]"),
    ("2", "Frontend Engineering", "Angular + Tailwind", "Role-based dashboards, responsive forms, status indicators, and modular layout with modern standalone components.", "irrigashield-frontend/src/app/features/*", "[IMPLEMENTED / PLANNED]"),
    ("3", "Frontend Engineering", "NgRx State Management", "Feature-driven state management for auth, farmer, underwriter, and claims with typed actions, reducers, and effects.", "irrigashield-frontend/src/app/features/*/state/*", "[IMPLEMENTED / PLANNED]"),
    ("4", "Frontend Engineering", "Frontend Validation", "Reactive forms with cross-field validators, postal code regex, coverage amount bounds, and route guards.", "irrigashield-frontend/src/app/shared/validators/*", "[IMPLEMENTED / PLANNED]"),
    ("5", "Frontend Engineering", "Frontend DTOs", "Strict TypeScript models mirroring backend request/response structures.", "irrigashield-frontend/src/app/features/*/model/*", "[IMPLEMENTED / PLANNED]"),
    ("6", "Architecture & Microservices", "API Gateway & Routing", "Spring Cloud Gateway Reactive with centralized CORS, JWT cookie extraction, header stripping, and internal header injection.", "api-gateway/src/main/resources/application.yaml, JwtAuthenticationFilter.java", "[VERIFIED / COMPLETE]"),
    ("7", "Architecture & Microservices", "Service Discovery (Eureka)", "Centralized service discovery and dynamic client-side load balancing via Spring Cloud Netflix Eureka.", "eureka-server/src/main/resources/application.yml, EurekaServerApplication.java", "[VERIFIED / COMPLETE]"),
    ("8", "Backend Engineering", "Java & Spring Boot Reactive", "Fully non-blocking reactive microservices built on Spring WebFlux, Project Reactor (Mono/Flux), Netty, and Spring Data R2DBC.", "auth-service, farm-service, policy-service, event-claims-service", "[VERIFIED / COMPLETE]"),
    ("9", "Backend Engineering", "Backend Validation", "Jakarta Bean Validation (@NotNull, @DecimalMin, @Size, @Pattern) on all incoming request records.", "dto/request/* across all services", "[VERIFIED / COMPLETE]"),
    ("10", "Backend Engineering", "Backend DTOs", "Clean separation of Request DTOs, Response DTOs, Domain Entities, and Inter-service internal DTOs using Java Records.", "dto/request/*, dto/response/* in all microservices", "[VERIFIED / COMPLETE]"),
    ("11", "Backend Engineering", "Custom Exceptions & Response", "Global reactive exception handler returning standardized ApiErrorResponse with HTTP status, code, path, timestamp, and field errors.", "exception/GlobalExceptionHandler.java, ApiException.java", "[VERIFIED / COMPLETE]"),
    ("12", "Backend Engineering", "JWT & Security", "Role-Based Access Control (RBAC) enforced via HttpOnly cookies and TrustedHeaderAuthenticationFilter with @PreAuthorize.", "security/TrustedHeaderAuthenticationFilter.java, SecurityConfig.java", "[VERIFIED / COMPLETE]"),
    ("13", "Backend Engineering", "Logging, Monitoring & Actuator", "Structured SLF4J logging aspect (@Aspect) on controllers and services; Spring Boot Actuator endpoints for health and metrics.", "aspect/LoggingAspect.java, application.yaml management endpoints", "[VERIFIED / COMPLETE]"),
    ("14", "Data & Operations", "Database Design & Integration", "Database-per-service pattern with 4 isolated MySQL databases, primary keys, foreign keys, unique constraints, and status indexes.", "schema.sql across all 4 services", "[VERIFIED / COMPLETE]"),
    ("15", "Code Quality & Integration", "Unit Testing & Code Coverage", "Comprehensive unit and integration test suites with JUnit 5, Mockito, StepVerifier, WebTestClient, and MockMvc.", "src/test/java/* across all 4 microservices", "[VERIFIED / COMPLETE]"),
    ("16", "Code Quality & Integration", "Code Quality & Best Practices", "SOLID principles, domain mapper classes, immutable records, idempotency locks, and clean package modularity.", "util/*Mapper.java, service/impl/*", "[VERIFIED / COMPLETE]"),
    ("17", "Code Quality & Integration", "Frontend & Backend Integration", "Angular HTTP interceptor propagating credentials with CORS, handling 401/403/500 errors gracefully.", "features/auth/interceptors/auth.interceptor.ts", "[IMPLEMENTED / PLANNED]"),
    ("18", "Final Assessment", "Presentation & Communication", "End-to-end live demonstration walking from farmer registration to automated deficit detection and payout settlement.", "Demo_Journey sheet & Section 11 of this plan", "[VERIFIED / READY]"),
    ("19", "Final Assessment", "User Journey & Strengths/Weaknesses", "Comprehensive analysis of basis risk, simulation boundaries, straight-through efficiency, and architectural strengths.", "Notes sheet & Section 12 of this plan", "[VERIFIED / READY]")
]

for row in EVAL_MAPPINGS:
    add(f"| **{row[0]}** | **{row[1]}** | **{row[2]}** | {row[3]} | `{row[4]}` | **{row[5]}** |")

add("")
add("---")
add("")

# Section 11: Demo_Journey
add("## 11. Live Demonstration Script & End-to-End Walkthrough (`Demo_Journey`)")
add("")
add("This 15-step script provides the precise demonstration workflow to showcase the live platform during technical evaluations.")
add("")
add("| Step | Actor | Demo Action | Expected Business Result | Exact API Call & Verification Payload |")
add("| :---: | :--- | :--- | :--- | :--- |")

DEMO_STEPS = [
    ("1", "Farmer", "Register and login", "FARMER JWT cookie generated and stored in browser session.", "`POST /api/auth/register` then `POST /api/auth/login` (email: `farmer@irrigashield.com`, password: `Farmer@123`). Header `Set-Cookie: IRRIGASHIELD_TOKEN` returned."),
    ("2", "Farmer", "Register farm, crop and irrigation details", "Farm risk inputs stored in `irrigashield_farm_db`.", "`POST /api/farmers/profile`, `POST /api/farms` (10 acres), `POST /api/farms/{id}/crops` (WHEAT, VEGETATIVE), `POST /api/farms/{id}/irrigation` (CANAL, FAIR condition, 65% avg water)."),
    ("3", "Farmer", "Select plan and submit application", "Application created in `DRAFT` then transitioned to `SUBMITTED`.", "`GET /api/plans/active`, `POST /api/applications` (requested coverage: 50,000), `POST /api/applications/{id}/submit`. Status becomes `SUBMITTED`."),
    ("4", "Underwriter", "Run risk assessment", "Risk score (55) and classification (`HIGH`) displayed.", "Login as `underwriter@irrigashield.com`. `GET /api/underwriting/applications/pending`. `POST /api/underwriting/applications/{id}/start-review`. `POST /api/underwriting/applications/{id}/assess`. Returns factor breakdown and recommended premium."),
    ("5", "Underwriter", "Approve application", "Application marked `APPROVED` with binding quote.", "`PUT /api/underwriting/applications/{id}/approve` with `DecisionRequest(remarks='Low historical failure rate')`. Case and application status become `APPROVED`."),
    ("6", "Farmer", "Accept quote and pay premium", "Premium payment marked `PAID` instantly.", "Switch back to Farmer. `GET /api/policies/approved-quote/{applicationId}`. `POST /api/policies/approved-quote/{applicationId}/accept`. Payment status clears as `PAID`."),
    ("7", "Policy Service", "Activate policy", "Policy record created with status `ACTIVE`.", "Response from step 6 returns active policy with `policyId`, agreed coverage (`50,000`), premium (`3,250`), `startDate`, `endDate`, and `status='ACTIVE'`."),
    ("8", "Data Provider", "Submit normal telemetry reading", "Reading saved; no claim triggered.", "Login as `dataprovider@irrigashield.com`. `POST /api/readings` with `policyId`, `availabilityPercentage: 85.00`. Response returns `claimCreated: false, status: 'NO_CLAIM', payoutStatus: 'NOT_REQUIRED'`."),
    ("9", "Data Provider", "Submit below-threshold reading (Breach)", "Breach detected; enters parametric deficit settlement.", "`POST /api/readings` with `policyId`, `availabilityPercentage: 25.00`. Threshold breach (< 70%) triggers automated straight-through settlement engine."),
    ("10", "Parametric Engine", "Satisfy trigger rules", "Deficit validated; idempotency verified.", "Engine queries `GET /api/internal/policies/{policyId}`, verifies active policy dates, checks that no prior settled claim exists on policy."),
    ("11", "Claims Service", "Auto-create verified claim", "Claim record instantiated.", "Claim record generated linked to `readingId`, `policyId`, `farmerUserId`, `farmId` with availability 25.00%."),
    ("12", "Claims Service", "Calculate rule-based compensation", "50% payout tier applied; compensation computed.", "Availability < 30.00% triggers Tier 1 payout (50%). Compensation calculated: `50,000 * 50% = 25,000.00`."),
    ("13", "Claims Officer", "Straight-Through Processing clearance", "Claim approved automatically without human queue.", "Under normal straight-through parametric processing, claim transitions directly to settlement without manual intervention."),
    ("14", "Payment Service", "Process simulated payout", "Claim marked `SETTLED` with payout `SUCCESS`.", "Claim persisted with `payout_status='SUCCESS'`, `status='SETTLED'`, and `settled_at=now()`."),
    ("15", "Farmer", "View claim, payout and notification", "Farmer verifies full indemnity settlement.", "Farmer calls `GET /api/claims/my-claims`. Receives settled claim with compensation `25,000.00`, reading `25%`, and payout `SUCCESS`. Complete journey concluded!")
]

for row in DEMO_STEPS:
    add(f"| **{row[0]}** | **{row[1]}** | **{row[2]}** | {row[3]} | {row[4]} |")

add("")
add("---")
add("")

# Section 12: Notes
add("## 12. Architectural Principles & Governance Notes (`Notes`)")
add("")
add("### 12.1 Core Architectural Principles")
add("")

ARCH_NOTES = [
    ("Final Project Description", "IrrigaShield is a reactive microservices parametric insurance platform protecting farmers from irrigation deficits. It receives readings from authorized external telemetry providers, evaluates policy-defined triggers, auto-creates claims, computes predefined compensation, and settles payouts straight-through without manual surveys."),
    ("Important Architecture Decision", "Consolidated 8 theoretical services into 4 reactive domain microservices. Underwriting is unified inside `policy-service`. Monitoring, trigger evaluation, claims calculation, and payout simulation are unified inside `event-claims-service`. This eliminates distributed transactions, network latency, and eventual consistency failures."),
    ("Data Collection Boundary", "IrrigaShield does not operate physical IoT sensors. The platform defines an authorized external data provider contract (`POST /api/readings`) secured by `ROLE_DATA_PROVIDER`. Sensors, SCADA, or satellite feeds authenticate and transmit structured telemetry indices."),
    ("Trigger Model", "Configurable parametric triggers define availability percentage thresholds (default 70.00%), continuous duration hours, and growth-stage windows. Payouts are graduated into mathematical tiers based on severity."),
    ("Claim Model", "Straight-Through Processing (STP) automates verified claim approval and immediate payout clearance. Anomaly exception queues for human Claims Officers are reserved for Phase 2 roadmap audits."),
    ("Capstone Limitation", "Physical IoT hardware, live central banking rail transfers, and actuarial machine learning models are represented via robust software simulators and deterministic rule engines."),
    ("Core Strength & Differentiator", "The true differentiator is the frictionless, end-to-end straight-through flow: trusted external telemetry directly generates verified claims and instant compensation with zero human latency or adjuster bias.")
]

for title, content in ARCH_NOTES:
    add(f"#### {title}")
    add(f"> {content}")
    add("")

add("---")
add("")

# Section 13: Algorithmic Engines
add("## 13. Algorithmic Engines & Business Logic Specifications")
add("")
add("### 13.1 Underwriting Risk Calculator Engine (`RiskCalculator.java`)")
add("The Underwriting Risk Engine evaluates the farm's agronomic and hydrological risk profile to compute risk loading and premium rates.")
add("")
add("#### Factor Scoring Formula:")
add("$$\\text{Total Score} = S_{\\text{avail}} + S_{\\text{equip}} + S_{\\text{fail}} + S_{\\text{season}} + S_{\\text{backup}}$$")
add("")
add("| Factor | Condition / Metric | Points Contributed |")
add("| :--- | :--- | :---: |")
add("| **Water Availability ($S_{\\text{avail}}$)** | $\\ge 85\\%$ | 0 pts |")
add("| | $70\\% \\text{ to } 84.99\\%$ | 10 pts |")
add("| | $50\\% \\text{ to } 69.99\\%$ | 20 pts |")
add("| | $< 50\\%$ (or missing) | 30 pts |")
add("| **Equipment Condition ($S_{\\text{equip}}$)** | `GOOD` | 0 pts |")
add("| | `FAIR` | 10 pts |")
add("| | `POOR` / unknown | 20 pts |")
add("| **Historical Failures ($S_{\\text{fail}}$)** | 0 historical failures | 0 pts |")
add("| | 1 to 2 historical failures | 10 pts |")
add("| | $> 2$ historical failures | 20 pts |")
add("| **Seasonal Drought Risk ($S_{\\text{season}}$)** | `LOW` | 0 pts |")
add("| | `MEDIUM` | 10 pts |")
add("| | `HIGH` | 15 pts |")
add("| | `VERY_HIGH` | 20 pts |")
add("| **Backup Water Source ($S_{\\text{backup}}$)** | Valid secondary source present | 0 pts |")
add("| | `NONE` or blank | 10 pts |")
add("")
add("#### Risk Classification & Premium Loading Matrix:")
add("| Total Risk Score | Risk Classification | Premium Loading (%) | Recommended Premium Formula |")
add("| :---: | :---: | :---: | :--- |")
add("| **0 to 25** | `LOW` | **+5%** | $\\text{Base Premium} \\times 1.05$ |")
add("| **26 to 50** | `MODERATE` | **+15%** | $\\text{Base Premium} \\times 1.15$ |")
add("| **51 to 75** | `HIGH` | **+30%** | $\\text{Base Premium} \\times 1.30$ |")
add("| **76 to 100** | `VERY_HIGH` | **+50%** | $\\text{Base Premium} \\times 1.50$ |")
add("")
add("#### Recommended Coverage Clamping:")
add("$$\\text{Recommended Coverage} = \\min(\\max(\\text{Requested Coverage}, \\text{Plan Min}), \\text{Plan Max})$$")
add("")
add("---")
add("")
add("### 13.2 Parametric Deficit Settlement Engine (`EventClaimsServiceImpl.java`)")
add("When telemetry is ingested (`POST /api/readings`), it is compared against the parametric trigger threshold:")
add("")
add("#### Settlement Tier Table:")
add("| Water Availability (%) | Trigger Status | Payout Percentage (%) | Indemnity Compensation Formula | Payout & Claim Status |")
add("| :---: | :---: | :---: | :--- | :---: |")
add("| **$\\ge 70.00\\%$** | Normal (No Breach) | **0%** | $0.00$ | `NO_CLAIM` / `NOT_REQUIRED` |")
add("| **$50.00\\% \\text{ to } 69.99\\%$** | Minor Deficit (Tier 3) | **10.00%** | $\\text{Coverage Amount} \\times 0.10$ | `SETTLED` / `SUCCESS` |")
add("| **$30.00\\% \\text{ to } 49.99\\%$** | Moderate Deficit (Tier 2) | **25.00%** | $\\text{Coverage Amount} \\times 0.25$ | `SETTLED` / `SUCCESS` |")
add("| **$< 30.00\\%$** | Severe Drought (Tier 1) | **50.00%** | $\\text{Coverage Amount} \\times 0.50$ | `SETTLED` / `SUCCESS` |")
add("")
add("#### Settlement Idempotency Rule:")
add("To prevent duplicate payouts during continuous deficit reporting, `event-claims-service` checks:")
add("```java")
add("claimRepository.existsByPolicyIdAndStatus(policy.policyId(), \"SETTLED\")")
add("```")
add("If a settled claim already exists for the policy, subsequent breach readings return HTTP 409 `SETTLED_CLAIM_ALREADY_EXISTS`.")
add("")
add("---")
add("")

# Section 14: Frontend Integration Architecture
add("## 14. Frontend Integration Architecture (Angular 21 + NgRx)")
add("")
add("### 14.1 Enterprise Directory Structure")
add("```")
add("irrigashield-frontend/src/app/")
add("├── core/")
add("│   ├── auth/              # Global auth interceptor & cookie credential handling")
add("│   ├── layout/            # Navbar, Sidebar, Footer, Role-based navigation")
add("│   └── guards/            # RoleGuard, AuthGuard")
add("├── features/")
add("│   ├── auth/              # Login, Register, Profile components & state")
add("│   ├── farmer/            # Farm creation, Crop form, Irrigation setup, My Farms")
add("│   ├── underwriter/       # Review queue, Risk assessment view, Approval dialog")
add("│   ├── claims/            # Claims list, Compensation breakdown, Payout receipts")
add("│   └── admin/             # Plan management, User admin, Trigger rule forms")
add("├── shared/")
add("│   ├── components/        # Metric cards, Alert banners, Status badges")
add("│   ├── directives/        # DigitOnly, DecimalFormat")
add("│   └── validators/        # PostalCode, Acreage, DateRange validators")
add("└── app.routes.ts          # Lazy-loaded standalone routes with CanActivate guards")
add("```")
add("")
add("### 14.2 HTTP Cookie Interceptor (`withCredentials: true`)")
add("Because the backend uses HttpOnly cookies (`IRRIGASHIELD_TOKEN`), all Angular HTTP client requests must include credentials:")
add("```typescript")
add("export const authInterceptor: HttpInterceptorFn = (req, next) => {")
add("  const cloned = req.clone({")
add("    withCredentials: true")
add("  });")
add("  return next(cloned);")
add("};")
add("```")
add("")
add("---")
add("")

# Section 15: Backend Improvements & Future Roadmap
add("## 15. Backend Improvements & Future Roadmap")
add("")
add("As documented in `BACKEND_IMPROVEMENTS_ROADMAP.md`, future enhancements will evolve IrrigaShield from capstone excellence to commercial production grade:")
add("")
add("```mermaid")
add("flowchart LR")
add("    P1[\"Phase 1: Dual Auth<br/>(Bearer Token + Cookie)\"] --> P2[\"Phase 2: Observability<br/>(Micrometer + Zipkin)\"]")
add("    P2 --> P3[\"Phase 3: Event Streaming<br/>(Apache Kafka Telemetry)\"]")
add("    P3 --> P4[\"Phase 4: Weather Oracles<br/>(Automated Satellite APIs)\"]")
add("```")
add("")
add("1. **Phase 1: Dual Authentication (Bearer Token + Cookie):** Update `JwtAuthenticationFilter` in `api-gateway` to support both `Authorization: Bearer <token>` and `IRRIGASHIELD_TOKEN` cookie. Unlocks Swagger UI, SoapUI, and native mobile testing.")
add("2. **Phase 2: Distributed Tracing & Circuit Breakers:** Integrate Spring Cloud Circuit Breaker (Resilience4j) on `WebClient` inter-service calls and Micrometer Tracing with Zipkin for distributed request tracing.")
add("3. **Phase 3: High-Scale IoT Event Streaming:** Introduce Apache Kafka or RabbitMQ broker between external telemetry sources and `event-claims-service` to ingest thousands of sensor events per second asynchronously.")
add("4. **Phase 4: External Weather Oracle Integration:** Connect OpenWeatherMap, Copernicus Sentinel-1 SAR, or ECMWF rainfall oracles to automate telemetry validation with satellite-derived soil moisture.")
add("")
add("---")
add("")
add("*Document compiled and validated against Capstone-IrrigaSheild code repository and IrrigaShield_main_plan.xlsx.*")

full_content = "\n".join(doc)

out_path1 = os.path.join(r"d:\Capstone", "IRRIGASHIELD_UPDATED_MAIN_PLAN.md")
out_path2 = os.path.join(r"d:\Capstone\Capstone-IrrigaSheild", "IRRIGASHIELD_UPDATED_MAIN_PLAN.md")

with open(out_path1, "w", encoding="utf-8") as f:
    f.write(full_content)

with open(out_path2, "w", encoding="utf-8") as f:
    f.write(full_content)

print(f"Successfully generated updated plan at:")
print(f"  -> {out_path1} ({os.path.getsize(out_path1)} bytes)")
print(f"  -> {out_path2} ({os.path.getsize(out_path2)} bytes)")
