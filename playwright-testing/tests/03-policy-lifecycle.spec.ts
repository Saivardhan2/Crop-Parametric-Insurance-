import { test, expect } from '@playwright/test';

test.describe('Policy Lifecycle - Application, Underwriting Assessment & Policy Issuance', () => {
  test('Farmer applies for plan, Underwriter assesses risk and approves, Farmer accepts quote and binds policy', async ({ page }) => {
    const timestamp = Date.now();
    const farmerName = `Sunil Verma ${timestamp}`;
    const farmerEmail = `farmer_${timestamp}@example.com`;
    const farmName = `Malwa Plateau Farm ${timestamp}`;

    // Step 1: Register Farmer, KYC, Farm, Crop, and Irrigation Setup
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

    // Farmer KYC Profile
    await page.goto('/farmer/profile');
    await page.waitForLoadState('networkidle');
    const editBtn = page.locator('button:has-text("Edit Profile")');
    if (await editBtn.isVisible()) {
      await editBtn.click();
    }
    await page.fill('input[formControlName="fullName"]', farmerName);
    await page.fill('input[formControlName="phone"]', '9876543210');
    await page.fill('input[formControlName="address"]', 'Gram Panchayat Road, Ujjain');
    await page.fill('input[formControlName="district"]', 'Ujjain');
    await page.fill('input[formControlName="state"]', 'Madhya Pradesh');
    await page.fill('input[formControlName="bankAccountNumber"]', '1098');
    await page.click('button[type="submit"]');
    await expect(page.locator('span:has-text("VERIFIED")')).toBeVisible({ timeout: 10000 });

    // Register Farm Plot
    await page.goto('/farmer/farms');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("+ Add Farm Plot")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="farmName"]', farmName);
    await page.fill('input[formControlName="totalAreaHectares"]', '4.5');
    await page.selectOption('select[formControlName="soilType"]', 'SANDY_LOAM');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.click('button[type="submit"]:has-text("Save Farm")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeHidden({ timeout: 10000 });

    // Open Farm Detail, Add Crop & Irrigation Profile
    await page.click(`text=${farmName} >> xpath=ancestor::div[contains(@class, "rounded-2xl")] >> a:has-text("Manage Crops")`);
    await page.waitForURL('**/farmer/farms/**', { timeout: 15000 });

    // Add Crop
    await page.click('button:has-text("+ Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="cropName"]', 'Kalyansona Wheat');
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="growthStage"]', 'VEGETATIVE');
    await page.fill('input[formControlName="plantedAreaHectares"]', '4.0');
    await page.click('button[type="submit"]:has-text("Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator('text=Kalyansona Wheat').first()).toBeVisible({ timeout: 10000 });

    // Save Irrigation Profile
    const createIrrBtn = page.locator('button:has-text("+ Create Irrigation Profile")');
    if (await createIrrBtn.isVisible()) {
      await createIrrBtn.click();
    }
    await page.selectOption('select[formControlName="irrigationType"]', 'DRIP');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.selectOption('select[formControlName="equipmentCondition"]', 'GOOD');
    await page.fill('input[formControlName="efficiencyPercentage"]', '88');
    await page.click('button[type="submit"]:has-text("Save")');
    await expect(page.locator('button:has-text("Edit Profile")')).toBeVisible({ timeout: 10000 });

    // Step 2: Farmer Applies for Policy Coverage on Active Plan
    await page.goto('/farmer/plans');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Active Insurance Plans');

    // Click Apply for Coverage on the first plan
    const applyBtn = page.locator('button:has-text("Apply for Coverage")').first();
    await expect(applyBtn).toBeVisible({ timeout: 10000 });
    await applyBtn.click();

    // Fill Application Modal
    await expect(page.locator('h3:has-text("Apply for")')).toBeVisible({ timeout: 10000 });
    await page.waitForTimeout(500);

    // Select Farm Plot
    const farmSelect = page.locator('select[formControlName="farmId"]');
    await farmSelect.selectOption({ index: 1 });
    await page.waitForTimeout(500);

    // Select Crop
    const cropSelect = page.locator('select[formControlName="cropId"]');
    await cropSelect.selectOption({ index: 1 });

    // Set Requested Coverage Amount
    await page.fill('input[formControlName="requestedCoverageAmount"]', '50000');
    await page.click('button[type="submit"]:has-text("Submit Application")');
    await expect(page.locator('h3:has-text("Apply for")')).toBeHidden({ timeout: 10000 });

    // Verify Application Submitted on /farmer/applications
    await page.waitForURL('**/farmer/applications', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('My Policy Applications');
    const applicationCard = page.locator('div:has-text("Application #")').first();
    await expect(applicationCard).toBeVisible({ timeout: 10000 });

    // Extract Application ID from page
    const appHeaderText = await page.locator('span:has-text("Application #")').first().innerText();
    const applicationId = appHeaderText.replace('Application #', '').trim();

    // Step 3: Underwriter Reviews, Runs Telemetry Risk Assessment & Approves
    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // Login as Admin / Underwriter
    await page.fill('input[formControlName="email"]', 'admin@irrigashield.com');
    await page.fill('input[formControlName="password"]', 'Admin12345');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/admin/users', { timeout: 15000 });

    // Navigate to Underwriting Queue
    await page.goto('/underwriting');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Underwriter Application Review Queue');

    // Find and select the submitted application
    const targetCaseCard = page.locator(`span:has-text("Application #${applicationId}") >> xpath=ancestor::div[contains(@class, "rounded-2xl")]`).first();
    await expect(targetCaseCard).toBeVisible({ timeout: 10000 });

    // Click Review & Decide
    await targetCaseCard.locator('button:has-text("Review & Decide")').click();
    await page.waitForTimeout(500);

    // Run Telemetry Risk Assessment
    await page.click('button:has-text("Run Risk Telemetry Assessment")');
    await expect(page.getByText('Telemetry Risk Assessment:').first()).toBeVisible({ timeout: 10000 });

    // Confirm Underwriting Decision (Approve)
    await page.click('button[type="submit"]:has-text("Confirm Underwriting Decision")');
    await page.waitForTimeout(1000);

    // Step 4: Farmer Accepts Quote and Binds Active Policy
    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/farmer/farms', { timeout: 15000 });

    // Visit Applications page and verify status APPROVED
    await page.goto('/farmer/applications');
    await page.waitForLoadState('networkidle');
    const approvedBadge = page.locator('span:has-text("APPROVED")').first();
    await expect(approvedBadge).toBeVisible({ timeout: 10000 });

    // Click Accept & Buy Policy
    const acceptBtn = page.locator('button:has-text("Accept & Buy Policy")').first();
    await expect(acceptBtn).toBeVisible({ timeout: 10000 });
    await acceptBtn.click();
    await page.waitForTimeout(1500);

    // Step 5: Farmer Checks Active Policies
    await page.goto('/farmer/policies');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('My Active Policies');

    // Verify Active Policy is listed
    const activeBadge = page.locator('span:has-text("ACTIVE")').first();
    await expect(activeBadge).toBeVisible({ timeout: 10000 });
    const policyContractHeader = page.locator('div:has-text("Policy Contract")').first();
    await expect(policyContractHeader).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=Soil Moisture Telemetry').first()).toBeVisible({ timeout: 10000 });
  });
});
