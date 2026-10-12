# IrrigaShield: Complete SoapUI Execution & Code Guide

This guide provides **ready-to-run code for every step**:
1. **Ready-to-use SoapUI Groovy Scripts** to automatically extract cookies and IDs (`farmerId`, `farmId`, `planId`, `applicationId`, `policyId`) and pass them between requests.
2. **Ready-to-import cURL commands** for every single endpoint (can be imported directly into SoapUI via *Tools -> cURL* or run in PowerShell / Bash).
3. **Exact HTTP Request & Response JSON code blocks** for all 27 steps.
4. An importable **`IrrigaShield-soapui-project.xml`** file is also provided in the project root.

---

## 1. SoapUI Setup & Automation Groovy Scripts

In SoapUI, instead of manually copying and pasting tokens across 27 requests, you can use **Groovy Script TestSteps**.

### 1.1 Automated Cookie Extraction Groovy Script
Add a **Groovy Script** step right after each login step (`Admin Login`, `Farmer Login`, `Underwriter Login`, `Data Provider Login`):

```groovy
// Groovy Script: Extract IRRIGASHIELD_TOKEN and save to TestSuite Properties
def responseHeaders = messageExchange.responseHeaders
def setCookieHeaders = responseHeaders["Set-Cookie"]

if (setCookieHeaders != null) {
    for (cookie in setCookieHeaders) {
        if (cookie.contains("IRRIGASHIELD_TOKEN=")) {
            def token = cookie.split("IRRIGASHIELD_TOKEN=")[1].split(";")[0]
            testRunner.testCase.testSuite.setPropertyValue("AUTH_COOKIE", "IRRIGASHIELD_TOKEN=" + token)
            log.info("Extracted Cookie: IRRIGASHIELD_TOKEN=" + token)
            break
        }
    }
} else {
    log.error("No Set-Cookie header found in response!")
}
```

### 1.2 Automated Cookie Injection Groovy Script
Add this Groovy Script before sending authenticated requests:

```groovy
// Groovy Script: Inject Cookie into Next Request
def cookie = testRunner.testCase.testSuite.getPropertyValue("AUTH_COOKIE")
def request = testRunner.testCase.getTestStepByName("RequestStepName").httpRequest
request.addHeader("Cookie", cookie)
```
*(Or simply type `${#TestSuite#AUTH_COOKIE}` directly inside the `Cookie` header value in the SoapUI request header table!)*

---

## 2. Complete Step-by-Step Execution Code

---

### Step 0: Eureka Discovery Health Check
Check that all 5 microservices are registered and running.

#### cURL Code:
```bash
curl -X GET "http://localhost:8761/eureka/apps" -H "Accept: application/json"
```

#### HTTP Request Code:
```http
GET /eureka/apps HTTP/1.1
Host: localhost:8761
Accept: application/json
```

#### Expected Response Code (`200 OK`):
```json
{
  "applications": {
    "application": [
      { "name": "API-GATEWAY", "instance": [ { "status": "UP", "port": { "$": 8080 } } ] },
      { "name": "AUTH-SERVICE", "instance": [ { "status": "UP", "port": { "$": 8081 } } ] },
      { "name": "FARM-SERVICE", "instance": [ { "status": "UP", "port": { "$": 8082 } } ] },
      { "name": "POLICY-SERVICE", "instance": [ { "status": "UP", "port": { "$": 8083 } } ] },
      { "name": "EVENT-CLAIMS-SERVICE", "instance": [ { "status": "UP", "port": { "$": 8084 } } ] }
    ]
  }
}
```

---

### Step 1: Admin Login (Obtain Admin Token)

#### cURL Code:
```bash
curl -i -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@irrigashield.com",
    "password": "Admin12345"
  }'
```

#### HTTP Request Code:
```http
POST /api/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "admin@irrigashield.com",
  "password": "Admin12345"
}
```

#### Expected Response Code (`200 OK`):
```http
HTTP/1.1 200 OK
Set-Cookie: IRRIGASHIELD_TOKEN=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600
Content-Type: application/json

{
  "tokenType": "Cookie",
  "expiresIn": 3600,
  "user": {
    "userId": 1,
    "name": "IrrigaShield Admin",
    "email": "admin@irrigashield.com",
    "role": "ADMIN",
    "status": "ACTIVE"
  }
}
```

---

### Step 2: Admin Creates Underwriter Account

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/admin/users" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_ADMIN_TOKEN>" \
  -d '{
    "name": "Neha Underwriter",
    "email": "neha.underwriter@irrigashield.com",
    "password": "Password123",
    "role": "UNDERWRITER"
  }'
```

#### HTTP Request Code:
```http
POST /api/admin/users HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Cookie: IRRIGASHIELD_TOKEN=<ADMIN_TOKEN>

{
  "name": "Neha Underwriter",
  "email": "neha.underwriter@irrigashield.com",
  "password": "Password123",
  "role": "UNDERWRITER"
}
```

#### Expected Response Code (`201 Created`):
```json
{
  "userId": 2,
  "name": "Neha Underwriter",
  "email": "neha.underwriter@irrigashield.com",
  "role": "UNDERWRITER",
  "status": "ACTIVE"
}
```

---

### Step 3: Admin Creates Data Provider (IoT Sensor) Account

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/admin/users" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_ADMIN_TOKEN>" \
  -d '{
    "name": "IoT Sensor Feed",
    "email": "sensor.feed@irrigashield.com",
    "password": "Password123",
    "role": "DATA_PROVIDER"
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "userId": 3,
  "name": "IoT Sensor Feed",
  "email": "sensor.feed@irrigashield.com",
  "role": "DATA_PROVIDER",
  "status": "ACTIVE"
}
```

---

### Step 4: Admin Creates Parametric Insurance Plan

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/admin/plans" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_ADMIN_TOKEN>" \
  -d '{
    "planName": "Sugarcane Drought Shield",
    "description": "Parametric water deficit insurance covering vegetative and maturity stages.",
    "minimumCoverage": 50000.00,
    "maximumCoverage": 500000.00,
    "basePremium": 3000.00,
    "maximumPolicyDays": 180
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "planId": 1,
  "planName": "Sugarcane Drought Shield",
  "description": "Parametric water deficit insurance covering vegetative and maturity stages.",
  "minimumCoverage": 50000.00,
  "maximumCoverage": 500000.00,
  "basePremium": 3000.00,
  "maximumPolicyDays": 180,
  "status": "ACTIVE",
  "createdBy": 1,
  "createdAt": "2026-09-29T10:00:00",
  "updatedAt": "2026-09-29T10:00:00"
}
```

---

### Step 5: Admin Creates Trigger Rule for Plan 1

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/admin/plans/1/trigger-rules" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_ADMIN_TOKEN>" \
  -d '{
    "cropType": "Sugarcane",
    "growthStage": "VEGETATIVE",
    "availabilityThresholdPercentage": 70.00,
    "minimumContinuousHours": 48,
    "rollingWindowDays": 7,
    "requiredShortageDays": 3
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "triggerRuleId": 1,
  "planId": 1,
  "cropType": "Sugarcane",
  "growthStage": "VEGETATIVE",
  "availabilityThresholdPercentage": 70.00,
  "minimumContinuousHours": 48,
  "rollingWindowDays": 7,
  "requiredShortageDays": 3,
  "status": "ACTIVE"
}
```

---

### Step 6: Admin Creates Payout Slab for Trigger Rule 1

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/admin/trigger-rules/1/payout-slabs" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_ADMIN_TOKEN>" \
  -d '{
    "minimumDurationHours": 48,
    "maximumDurationHours": 72,
    "payoutPercentage": 25.00
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "payoutSlabId": 1,
  "triggerRuleId": 1,
  "minimumDurationHours": 48,
  "maximumDurationHours": 72,
  "payoutPercentage": 25.00,
  "status": "ACTIVE"
}
```

---

### Step 7: Farmer Self-Registration

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ramesh Kumar",
    "email": "ramesh.kumar@example.com",
    "password": "Password123"
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "userId": 4,
  "name": "Ramesh Kumar",
  "email": "ramesh.kumar@example.com",
  "role": "FARMER",
  "status": "ACTIVE"
}
```

---

### Step 8: Farmer Login (Obtain Farmer Token)

#### cURL Code:
```bash
curl -i -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ramesh.kumar@example.com",
    "password": "Password123"
  }'
```

#### Expected Response Code (`200 OK`):
```http
HTTP/1.1 200 OK
Set-Cookie: IRRIGASHIELD_TOKEN=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600
Content-Type: application/json

{
  "tokenType": "Cookie",
  "expiresIn": 3600,
  "user": {
    "userId": 4,
    "name": "Ramesh Kumar",
    "email": "ramesh.kumar@example.com",
    "role": "FARMER",
    "status": "ACTIVE"
  }
}
```
*(Copy `Set-Cookie` value as `FARMER_TOKEN`)*

---

### Step 9: Farmer Creates KYC Profile

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/farmers/profile" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>" \
  -d '{
    "fullName": "Ramesh Kumar",
    "phoneNumber": "9876543210",
    "addressLine": "Plot 42, Kisan Nagar",
    "village": "Baramati",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "413102",
    "bankAccountLastFour": "4321"
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "farmerId": 1,
  "authUserId": 4,
  "email": "ramesh.kumar@example.com",
  "fullName": "Ramesh Kumar",
  "phoneNumber": "9876543210",
  "addressLine": "Plot 42, Kisan Nagar",
  "village": "Baramati",
  "district": "Pune",
  "state": "Maharashtra",
  "postalCode": "413102",
  "bankAccountLastFour": "4321",
  "verificationStatus": "VERIFIED"
}
```

---

### Step 10: Farmer Registers Farm Plot

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/farms" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>" \
  -d '{
    "farmName": "Krishna Valley Farm",
    "surveyNumber": "SY-88/1A",
    "addressLine": "Canal Road, Sector 3",
    "village": "Baramati",
    "district": "Pune",
    "state": "Maharashtra",
    "postalCode": "413102",
    "areaInAcres": 12.50,
    "soilType": "Loamy Black Soil",
    "ownershipType": "OWNED",
    "latitude": 18.1512,
    "longitude": 74.5771
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "farmId": 1,
  "farmerId": 1,
  "farmName": "Krishna Valley Farm",
  "surveyNumber": "SY-88/1A",
  "addressLine": "Canal Road, Sector 3",
  "village": "Baramati",
  "district": "Pune",
  "state": "Maharashtra",
  "postalCode": "413102",
  "areaInAcres": 12.50,
  "soilType": "Loamy Black Soil",
  "ownershipType": "OWNED",
  "latitude": 18.1512,
  "longitude": 74.5771,
  "status": "ACTIVE"
}
```

---

### Step 11: Farmer Adds Crop Cycle

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/farms/1/crops" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>" \
  -d '{
    "cropType": "Sugarcane",
    "variety": "Co 86032",
    "sowingDate": "2026-08-01",
    "expectedHarvestDate": "2027-05-30",
    "growthStage": "VEGETATIVE",
    "irrigationRequirement": "HIGH"
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "cropId": 1,
  "farmId": 1,
  "cropType": "Sugarcane",
  "variety": "Co 86032",
  "sowingDate": "2026-08-01",
  "expectedHarvestDate": "2027-05-30",
  "growthStage": "VEGETATIVE",
  "irrigationRequirement": "HIGH",
  "status": "ACTIVE"
}
```

---

### Step 12: Farmer Adds Irrigation Infrastructure

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/farms/1/irrigation" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>" \
  -d '{
    "waterSource": "BOREWELL",
    "irrigationMethod": "DRIP",
    "equipmentCondition": "GOOD",
    "backupWaterSource": "CANAL",
    "historicalFailureCount": 1,
    "averageWaterAvailabilityPercentage": 78.50,
    "seasonalRisk": "LOW",
    "maintenanceFrequency": "QUARTERLY",
    "lastMaintenanceDate": "2026-08-15"
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "irrigationId": 1,
  "farmId": 1,
  "waterSource": "BOREWELL",
  "irrigationMethod": "DRIP",
  "equipmentCondition": "GOOD",
  "backupWaterSource": "CANAL",
  "historicalFailureCount": 1,
  "averageWaterAvailabilityPercentage": 78.50,
  "seasonalRisk": "LOW",
  "maintenanceFrequency": "QUARTERLY",
  "lastMaintenanceDate": "2026-08-15",
  "status": "ACTIVE"
}
```

---

### Step 13: Farmer Browses Active Insurance Plans

#### cURL Code:
```bash
curl -X GET "http://localhost:8080/api/plans/active" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
[
  {
    "planId": 1,
    "planName": "Sugarcane Drought Shield",
    "description": "Parametric water deficit insurance covering vegetative and maturity stages.",
    "minimumCoverage": 50000.00,
    "maximumCoverage": 500000.00,
    "basePremium": 3000.00,
    "maximumPolicyDays": 180,
    "status": "ACTIVE"
  }
]
```

---

### Step 14: Farmer Creates Insurance Application Draft

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/applications" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>" \
  -d '{
    "farmId": 1,
    "cropId": 1,
    "planId": 1,
    "requestedCoverage": 200000.00,
    "proposedStartDate": "2026-10-01",
    "proposedEndDate": "2027-03-25"
  }'
```

#### Expected Response Code (`201 Created`):
```json
{
  "applicationId": 1,
  "farmerUserId": 4,
  "farmerEmail": "ramesh.kumar@example.com",
  "farmId": 1,
  "cropId": 1,
  "planId": 1,
  "requestedCoverage": 200000.00,
  "proposedStartDate": "2026-10-01",
  "proposedEndDate": "2027-03-25",
  "status": "DRAFT",
  "submittedAt": null
}
```

---

### Step 15: Farmer Submits Application for Underwriting

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/applications/1/submit" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
{
  "applicationId": 1,
  "farmerUserId": 4,
  "farmerEmail": "ramesh.kumar@example.com",
  "farmId": 1,
  "cropId": 1,
  "planId": 1,
  "requestedCoverage": 200000.00,
  "proposedStartDate": "2026-10-01",
  "proposedEndDate": "2027-03-25",
  "status": "SUBMITTED",
  "submittedAt": "2026-09-29T10:15:00"
}
```

---

### Step 16: Underwriter Login

#### cURL Code:
```bash
curl -i -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "neha.underwriter@irrigashield.com",
    "password": "Password123"
  }'
```

#### Expected Response Code (`200 OK`):
```http
HTTP/1.1 200 OK
Set-Cookie: IRRIGASHIELD_TOKEN=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600
Content-Type: application/json

{
  "tokenType": "Cookie",
  "expiresIn": 3600,
  "user": {
    "userId": 2,
    "name": "Neha Underwriter",
    "email": "neha.underwriter@irrigashield.com",
    "role": "UNDERWRITER",
    "status": "ACTIVE"
  }
}
```
*(Copy `Set-Cookie` value as `UNDERWRITER_TOKEN`)*

---

### Step 17: Underwriter Views Pending Applications Queue

#### cURL Code:
```bash
curl -X GET "http://localhost:8080/api/underwriting/applications/pending" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_UNDERWRITER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
[
  {
    "caseId": null,
    "applicationId": 1,
    "farmerUserId": 4,
    "farmerEmail": "ramesh.kumar@example.com",
    "farmId": 1,
    "cropId": 1,
    "planId": 1,
    "planName": "Sugarcane Drought Shield",
    "requestedCoverage": 200000.00,
    "proposedStartDate": "2026-10-01",
    "proposedEndDate": "2027-03-25",
    "applicationStatus": "SUBMITTED",
    "underwriterUserId": null,
    "underwritingStatus": null
  }
]
```

---

### Step 18: Underwriter Starts Review (Claims Case)

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/underwriting/applications/1/start-review" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_UNDERWRITER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
{
  "caseId": 1,
  "applicationId": 1,
  "farmerUserId": 4,
  "farmerEmail": "ramesh.kumar@example.com",
  "underwriterUserId": 2,
  "underwriterEmail": "neha.underwriter@irrigashield.com",
  "applicationStatus": "UNDER_REVIEW",
  "underwritingStatus": "UNDER_REVIEW",
  "reviewStartedAt": "2026-09-29T10:20:00"
}
```

---

### Step 19: Underwriter Runs Automated Risk Assessment (Key Demo Step)

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/underwriting/applications/1/assess" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_UNDERWRITER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
{
  "caseId": 1,
  "applicationId": 1,
  "farmerUserId": 4,
  "farmerEmail": "ramesh.kumar@example.com",
  "farmId": 1,
  "cropId": 1,
  "planId": 1,
  "planName": "Sugarcane Drought Shield",
  "requestedCoverage": 200000.00,
  "proposedStartDate": "2026-10-01",
  "proposedEndDate": "2027-03-25",
  "applicationStatus": "UNDER_REVIEW",
  "underwriterUserId": 2,
  "underwriterEmail": "neha.underwriter@irrigashield.com",
  "waterAvailabilityScore": 10,
  "equipmentConditionScore": 0,
  "historicalFailureScore": 10,
  "seasonalRiskScore": 0,
  "backupSourceScore": 0,
  "overallRiskScore": 20,
  "riskLevel": "LOW",
  "basePremium": 3000.00,
  "riskLoading": 150.00,
  "recommendedPremium": 3150.00,
  "recommendedCoverage": 200000.00,
  "underwritingStatus": "ASSESSED"
}
```

---

### Step 20: Underwriter Approves Application

#### cURL Code:
```bash
curl -X PUT "http://localhost:8080/api/underwriting/applications/1/approve" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_UNDERWRITER_TOKEN>" \
  -d '{
    "remarks": "Risk evaluation verified. Excellent drip equipment and reliable canal backup access."
  }'
```

#### Expected Response Code (`200 OK`):
```json
{
  "caseId": 1,
  "applicationId": 1,
  "applicationStatus": "APPROVED",
  "underwritingStatus": "COMPLETED",
  "decision": "APPROVED",
  "remarks": "Risk evaluation verified. Excellent drip equipment and reliable canal backup access.",
  "reviewCompletedAt": "2026-09-29T10:25:00"
}
```

---

### Step 21: Farmer Views Approved Quote

#### cURL Code:
```bash
curl -X GET "http://localhost:8080/api/policies/approved-quote/1" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
{
  "policyId": null,
  "applicationId": 1,
  "farmerUserId": 4,
  "farmerEmail": "ramesh.kumar@example.com",
  "farmId": 1,
  "cropId": 1,
  "planId": 1,
  "planName": "Sugarcane Drought Shield",
  "coverageAmount": 200000.00,
  "premiumAmount": 3150.00,
  "startDate": "2026-10-01",
  "endDate": "2027-03-25",
  "paymentStatus": "PAID",
  "status": "APPROVED",
  "acceptedAt": null
}
```

---

### Step 22: Farmer Accepts Quote (Policy Issued & Activated)

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/policies/approved-quote/1/accept" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
{
  "policyId": 1,
  "applicationId": 1,
  "farmerUserId": 4,
  "farmerEmail": "ramesh.kumar@example.com",
  "farmId": 1,
  "cropId": 1,
  "planId": 1,
  "planName": "Sugarcane Drought Shield",
  "coverageAmount": 200000.00,
  "premiumAmount": 3150.00,
  "startDate": "2026-10-01",
  "endDate": "2027-03-25",
  "paymentStatus": "PAID",
  "status": "ACTIVE",
  "acceptedAt": "2026-09-29T10:30:00"
}
```

---

### Step 23: Farmer Verifies Active Policies List

#### cURL Code:
```bash
curl -X GET "http://localhost:8080/api/policies/my-policies" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
[
  {
    "policyId": 1,
    "applicationId": 1,
    "farmerUserId": 4,
    "farmerEmail": "ramesh.kumar@example.com",
    "farmId": 1,
    "cropId": 1,
    "planId": 1,
    "planName": "Sugarcane Drought Shield",
    "coverageAmount": 200000.00,
    "premiumAmount": 3150.00,
    "startDate": "2026-10-01",
    "endDate": "2027-03-25",
    "paymentStatus": "PAID",
    "status": "ACTIVE",
    "acceptedAt": "2026-09-29T10:30:00"
  }
]
```

---

### Step 24: Data Provider (IoT Sensor) Login

#### cURL Code:
```bash
curl -i -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sensor.feed@irrigashield.com",
    "password": "Password123"
  }'
```

#### Expected Response Code (`200 OK`):
```http
HTTP/1.1 200 OK
Set-Cookie: IRRIGASHIELD_TOKEN=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600
Content-Type: application/json

{
  "tokenType": "Cookie",
  "expiresIn": 3600,
  "user": {
    "userId": 3,
    "name": "IoT Sensor Feed",
    "email": "sensor.feed@irrigashield.com",
    "role": "DATA_PROVIDER",
    "status": "ACTIVE"
  }
}
```
*(Copy `Set-Cookie` value as `DATA_PROVIDER_TOKEN`)*

---

### Step 25: Data Provider Submits Normal Telemetry Reading ($\ge 70\%$)

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/readings" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_DATA_PROVIDER_TOKEN>" \
  -d '{
    "policyId": 1,
    "availabilityPercentage": 82.50,
    "recordedAt": "2026-10-15T10:00:00"
  }'
```

#### Expected Response Code (`200 OK`):
```json
{
  "readingId": 1,
  "claimId": null,
  "policyId": 1,
  "farmId": 1,
  "availabilityPercentage": 82.50,
  "thresholdBreached": false,
  "payoutPercentage": 0.00,
  "compensationAmount": 0.00,
  "payoutStatus": "NOT_REQUIRED",
  "claimStatus": "NO_CLAIM",
  "recordedAt": "2026-10-15T10:00:00"
}
```

---

### Step 26: Data Provider Submits Severe Deficit Reading ($< 70\%$) -> Automated Parametric Claim Trigger

#### cURL Code:
```bash
curl -X POST "http://localhost:8080/api/readings" \
  -H "Content-Type: application/json" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_DATA_PROVIDER_TOKEN>" \
  -d '{
    "policyId": 1,
    "availabilityPercentage": 22.00,
    "recordedAt": "2026-10-20T14:00:00"
  }'
```

#### Expected Response Code (`200 OK`):
```json
{
  "readingId": 2,
  "claimId": 1,
  "policyId": 1,
  "farmId": 1,
  "availabilityPercentage": 22.00,
  "thresholdBreached": true,
  "payoutPercentage": 50.00,
  "compensationAmount": 100000.00,
  "payoutStatus": "SUCCESS",
  "claimStatus": "SETTLED",
  "recordedAt": "2026-10-20T14:00:00"
}
```

---

### Step 27: Farmer Views Automatically Settled Claim

#### cURL Code:
```bash
curl -X GET "http://localhost:8080/api/claims/my-claims" \
  -H "Cookie: IRRIGASHIELD_TOKEN=<PASTE_FARMER_TOKEN>"
```

#### Expected Response Code (`200 OK`):
```json
[
  {
    "readingId": 2,
    "claimId": 1,
    "policyId": 1,
    "farmId": 1,
    "availabilityPercentage": 22.00,
    "thresholdBreached": true,
    "payoutPercentage": 50.00,
    "compensationAmount": 100000.00,
    "payoutStatus": "SUCCESS",
    "claimStatus": "SETTLED",
    "recordedAt": "2026-10-20T14:00:00"
  }
]
```
