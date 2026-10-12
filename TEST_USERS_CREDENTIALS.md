# IrrigaShield - Registered Test Users and Credentials

This document contains verified credentials for all system roles across the IrrigaShield parametric insurance platform.

---

## 1. Fixed Standard Demo Accounts (Recommended for Manual Testing)

These accounts are permanently provisioned in the local MySQL database and are ready for login on http://localhost:4200/login.

| Role | Full Name | Email Address | Password | Accessible Routes |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | IrrigaShield Admin | admin@irrigashield.com | Admin12345 | /admin/users, /admin/plans |
| **UNDERWRITER** | Lead Underwriter | underwriter@irrigashield.com | Underwriter123 | /underwriting |
| **DATA_PROVIDER** | IoT Sensor Network | dataprovider@irrigashield.com | Provider123 | /data-provider |
| **FARMER** | Ramesh Patel | farmer@irrigashield.com | Password123 | /farmer/profile, /farmer/farms, /farmer/plans, /farmer/applications, /farmer/policies, /farmer/claims |

---

## 2. Password Standards and Validation Rules

All user passwords satisfy the backend validation regex `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$` (minimum 8 characters, at least 1 uppercase letter, 1 lowercase letter, and 1 numeric digit):

- **Admin Accounts**: Admin12345
- **Underwriter Accounts**: Underwriter123
- **Data Provider Accounts**: Provider123
- **Farmer Accounts**: Password123

---

## 3. Dynamic Accounts Generated During Automated Playwright Test Runs

### Underwriters (Password: Underwriter123)
- uw_1791472725108@irrigashield.com
- uw_1791423782253@irrigashield.com
- uw_1791423623717@irrigashield.com
- uw_1791423537612@irrigashield.com
- uw_1791423269655@irrigashield.com
- uw_1791420419570@irrigashield.com
- uw_1791420354923@irrigashield.com

### Data Providers (Password: Provider123)
- dp_1791472725108@irrigashield.com
- dp_1791423782253@irrigashield.com
- dp_1791423623717@irrigashield.com
- dp_1791423537612@irrigashield.com
- dp_1791423269655@irrigashield.com
- dp_1791420419570@irrigashield.com
- dp_1791420394255@irrigashield.com
- dp_1791420354923@irrigashield.com

### Farmers (Password: Password123)
- farmer_1791423782253@example.com (Has active policy and settled claim)
- farmer_1791423623717@example.com
- farmer_1791423537612@example.com
- farmer_1791423505806@example.com
- farmer_1791423482749@example.com
- farmer_1791423473029@example.com
- farmer_1791420419570@example.com
- farmer_1791420394255@example.com

---

## 4. Local Service Ports

| Service Name | Port | Description |
| :--- | :---: | :--- |
| **Frontend UI** | 4200 | Angular 21 Single Page Application (http://localhost:4200) |
| **API Gateway** | 8080 | Spring Cloud Gateway (http://localhost:8080/api) |
| **Eureka Server** | 8761 | Service Registry and Discovery (http://localhost:8761) |
| **Auth Service** | 8081 | Authentication and User Management Service |
| **Farm Service** | 8082 | Farms, Plots, Crops and Irrigation Profiles |
| **Policy Service** | 8083 | Parametric Plans, Trigger Rules, Slabs, Underwriting and Policies |
| **Event Claims Service** | 8084 | IoT Telemetry Ingestion, Deficit Evaluation and Claim Settlement |
