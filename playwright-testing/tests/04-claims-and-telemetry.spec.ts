import { test, expect } from '@playwright/test';

test.describe('Parametric Telemetry & Automated Claims Evaluation', () => {
  test('Data Provider ingests breached moisture reading, Evaluation engine settles claim, Farmer inspects payout', async ({ page }) => {
    const timestamp = Date.now();
    const farmerName = `Anil Sharma ${timestamp}`;
    const farmerEmail = `farmer_${timestamp}@example.com`;
    const farmName = `Chambal Valley Plot ${timestamp}`;
    const dpEmail = `dp_${timestamp}@irrigashield.com`;

    // Step 1: Provision Data Provider Account as Admin
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await page.fill('input[formControlName="email"]', 'admin@irrigashield.com');
    await page.fill('input[formControlName="password"]', 'Admin12345');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/admin/users', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('User Provisioning & Role Management');

    await page.click('button:has-text("+ Provision User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeVisible({ timeout: 10000 });

    await page.fill('input[formControlName="name"]', 'Authorized IoT Telemetry Agency');
    await page.fill('input[formControlName="email"]', dpEmail);
    await page.fill('input[formControlName="password"]', 'Provider123');
    await page.selectOption('select[formControlName="role"]', 'DATA_PROVIDER');
    await page.click('button[type="submit"]:has-text("Create User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator(`td:has-text("${dpEmail}")`).first()).toBeVisible({ timeout: 10000 });

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // Step 2: Farmer Onboarding and Policy Binding
    await page.goto('/register');
    await page.waitForLoadState('networkidle');
    await page.fill('input[formControlName="name"]', farmerName);
    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Register Account")');

    await page.waitForURL('**/login', { timeout: 15000 });
    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/farmer/farms', { timeout: 15000 });

    // KYC
    await page.goto('/farmer/profile');
    await page.waitForLoadState('networkidle');
    const editBtn = page.locator('button:has-text("Edit Profile")');
    if (await editBtn.isVisible()) {
      await editBtn.click();
    }
    await page.fill('input[formControlName="fullName"]', farmerName);
    await page.fill('input[formControlName="phone"]', '9812345678');
    await page.fill('input[formControlName="address"]', 'Gwalior Rural Division');
    await page.fill('input[formControlName="district"]', 'Gwalior');
    await page.fill('input[formControlName="state"]', 'Madhya Pradesh');
    await page.fill('input[formControlName="bankAccountNumber"]', '5566');
    await page.click('button[type="submit"]');
    await expect(page.locator('span:has-text("VERIFIED")')).toBeVisible({ timeout: 10000 });

    // Farm Plot
    await page.goto('/farmer/farms');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("+ Add Farm Plot")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="farmName"]', farmName);
    await page.fill('input[formControlName="totalAreaHectares"]', '6.0');
    await page.selectOption('select[formControlName="soilType"]', 'SANDY_LOAM');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.click('button[type="submit"]:has-text("Save Farm")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeHidden({ timeout: 10000 });

    // Crop & Irrigation
    await page.click(`text=${farmName} >> xpath=ancestor::div[contains(@class, "rounded-2xl")] >> a:has-text("Manage Crops")`);
    await page.waitForURL('**/farmer/farms/**', { timeout: 15000 });

    await page.click('button:has-text("+ Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="cropName"]', 'Sharbati Wheat');
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="growthStage"]', 'VEGETATIVE');
    await page.fill('input[formControlName="plantedAreaHectares"]', '5.0');
    await page.click('button[type="submit"]:has-text("Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeHidden({ timeout: 10000 });

    const createIrrBtn = page.locator('button:has-text("+ Create Irrigation Profile")');
    if (await createIrrBtn.isVisible()) {
      await createIrrBtn.click();
    }
    await page.selectOption('select[formControlName="irrigationType"]', 'DRIP');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.selectOption('select[formControlName="equipmentCondition"]', 'GOOD');
    await page.fill('input[formControlName="efficiencyPercentage"]', '90');
    await page.click('button[type="submit"]:has-text("Save")');
    await expect(page.locator('button:has-text("Edit Profile")')).toBeVisible({ timeout: 10000 });

    // Apply for Policy
    await page.goto('/farmer/plans');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Active Insurance Plans');
    await page.locator('button:has-text("Apply for Coverage")').first().click();

    await expect(page.locator('h3:has-text("Apply for")')).toBeVisible({ timeout: 10000 });
    await page.waitForTimeout(500);
    await page.locator('select[formControlName="farmId"]').selectOption({ index: 1 });
    await page.waitForTimeout(500);
    await page.locator('select[formControlName="cropId"]').selectOption({ index: 1 });
    await page.fill('input[formControlName="requestedCoverageAmount"]', '50000');
    await page.click('button[type="submit"]:has-text("Submit Application")');
    await expect(page.locator('h3:has-text("Apply for")')).toBeHidden({ timeout: 10000 });

    await page.waitForURL('**/farmer/applications', { timeout: 15000 });
    const appHeaderText = await page.locator('span:has-text("Application #")').first().innerText();
    const applicationId = appHeaderText.replace('Application #', '').trim();

    // Step 3: Underwriter Reviews & Approves Application
    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    await page.fill('input[formControlName="email"]', 'admin@irrigashield.com');
    await page.fill('input[formControlName="password"]', 'Admin12345');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/admin/users', { timeout: 15000 });

    await page.goto('/underwriting');
    await page.waitForLoadState('networkidle');

    const targetCaseCard = page.locator(`span:has-text("Application #${applicationId}") >> xpath=ancestor::div[contains(@class, "rounded-2xl")]`).first();
    await expect(targetCaseCard).toBeVisible({ timeout: 10000 });
    await targetCaseCard.locator('button:has-text("Review & Decide")').click();
    await page.waitForTimeout(500);

    await page.click('button:has-text("Run Risk Telemetry Assessment")');
    await expect(page.getByText('Telemetry Risk Assessment:').first()).toBeVisible({ timeout: 10000 });

    await page.click('button[type="submit"]:has-text("Confirm Underwriting Decision")');
    await page.waitForTimeout(1000);

    // Step 4: Farmer Accepts Quote and Retrieves Bound Policy ID
    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/farmer/farms', { timeout: 15000 });

    await page.goto('/farmer/applications');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('span:has-text("APPROVED")').first()).toBeVisible({ timeout: 10000 });
    await page.locator('button:has-text("Accept & Buy Policy")').first().click();
    await page.waitForTimeout(1500);

    await page.goto('/farmer/policies');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('My Active Policies');
    await expect(page.locator('span:has-text("ACTIVE")').first()).toBeVisible({ timeout: 10000 });

    const policyIdText = await page.locator('h3:has-text("#")').first().innerText();
    const policyId = policyIdText.replace('#', '').trim();

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // Step 5: Data Provider Submits Soil Moisture Telemetry Reading
    await page.fill('input[formControlName="email"]', dpEmail);
    await page.fill('input[formControlName="password"]', 'Provider123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/data-provider', { timeout: 15000 });
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('IoT Telemetry');

    // Ingest Breached Reading (15% moisture < 20% threshold)
    await page.locator('input[formControlName="policyId"]').clear();
    await page.locator('input[formControlName="policyId"]').fill(policyId);
    await page.locator('input[formControlName="availabilityPercentage"]').clear();
    await page.locator('input[formControlName="availabilityPercentage"]').fill('15');
    await page.click('button[type="submit"]:has-text("Submit Telemetry Reading")');

    // Step 6: Verify Evaluation Engine Live Response (created in PENDING_APPROVAL)
    await expect(page.locator('text=YES (Shortage)').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('span:has-text("PENDING_APPROVAL")').first()).toBeVisible({ timeout: 10000 });

    // Step 7: Claims Officer Reviews & Approves Claim (Transition to SETTLED)
    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    await page.fill('input[formControlName="email"]', 'officer@irrigashield.com');
    await page.fill('input[formControlName="password"]', 'Officer12345');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/claims-officer/claims', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Claims Review');

    // Claims Officer edits compensation before giving approval
    const compInput = page.locator('input[placeholder="Enter approved payout"]').first();
    await expect(compInput).toBeVisible({ timeout: 10000 });
    await compInput.fill('28000');

    const remarksInput = page.locator('input[placeholder*="Officer review remarks"]').first();
    if (await remarksInput.isVisible()) {
      await remarksInput.fill('Sensor breach verified; compensation adjusted to 28000');
    }

    // Approve the pending claim
    await page.locator('button:has-text("Approve & Settle Payout")').first().click();
    await page.waitForTimeout(1500);

    // Step 8: Farmer Inspects Settled Claim on /farmer/claims and Submits Feedback
    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/farmer/farms', { timeout: 15000 });

    await page.goto('/farmer/claims');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Parametric Claims & Payouts');

    // Verify Settled Claim Card
    await expect(page.locator('span:has-text("SETTLED")').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator(`text=Policy #${policyId}`).first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=15%').first()).toBeVisible({ timeout: 10000 });
  });
});
