import { test, expect } from '@playwright/test';

test.describe('Full End-to-End Parametric Insurance Journey', () => {
  test('Complete multi-persona lifecycle: Admin provisioning, Multi-Slab Plan setup, Farmer onboarding, Underwriting review, Policy activation, Water reading trigger and Claim settlement', async ({ page }) => {
    const timestamp = Date.now();
    const uwEmail = `uw_${timestamp}@irrigashield.com`;
    const dpEmail = `dp_${timestamp}@irrigashield.com`;
    const planName = `Mustard Shield ${timestamp}`;
    const farmerName = `Vikram Singh ${timestamp}`;
    const farmerEmail = `farmer_${timestamp}@example.com`;
    const farmName = `Chambal Basin Farm ${timestamp}`;

    // =========================================================================
    // Stage 1: Admin Provisions System Accounts & Configures Insurance Product
    // =========================================================================
    await page.goto('/login');
    await page.waitForLoadState('networkidle');
    await page.fill('input[formControlName="email"]', 'admin@irrigashield.com');
    await page.fill('input[formControlName="password"]', 'Admin12345');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/admin/users', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('User Provisioning & Role Management');

    // Provision Underwriter
    await page.click('button:has-text("+ Provision User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="name"]', 'Regional Underwriter');
    await page.fill('input[formControlName="email"]', uwEmail);
    await page.fill('input[formControlName="password"]', 'Underwriter123');
    await page.selectOption('select[formControlName="role"]', 'UNDERWRITER');
    await page.click('button[type="submit"]:has-text("Create User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator(`td:has-text("${uwEmail}")`).first()).toBeVisible({ timeout: 10000 });

    // Provision Data Provider
    await page.click('button:has-text("+ Provision User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="name"]', 'Water Gauge Operator');
    await page.fill('input[formControlName="email"]', dpEmail);
    await page.fill('input[formControlName="password"]', 'Provider123');
    await page.selectOption('select[formControlName="role"]', 'DATA_PROVIDER');
    await page.click('button[type="submit"]:has-text("Create User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator(`td:has-text("${dpEmail}")`).first()).toBeVisible({ timeout: 10000 });

    // Create Plan, Trigger Rule & Multiple Payout Slabs
    await page.click('a:has-text("Plans & Rules")');
    await page.waitForURL('**/admin/plans', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Insurance Plans');

    await page.click('button:has-text("+ Create Plan")');
    await page.fill('input[formControlName="planName"]', planName);
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="season"]', 'RABI');
    await page.fill('input[formControlName="maxCoverageAmount"]', '100000');
    await page.fill('input[formControlName="basePremiumRate"]', '6');
    await page.fill('textarea[formControlName="description"]', 'Comprehensive parametric water shortage insurance');
    await page.click('button[type="submit"]:has-text("Create Plan")');
    await expect(page.locator('h3:has-text("Create Insurance Plan")')).toBeHidden({ timeout: 10000 });

    const createdPlanCard = page.locator(`text=${planName}`).first();
    await expect(createdPlanCard).toBeVisible({ timeout: 10000 });
    await createdPlanCard.click();
    await page.waitForTimeout(500);

    // Add Trigger Rule (<30% water threshold)
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="growthStage"]', 'VEGETATIVE');
    await page.fill('input[formControlName="availabilityThresholdPercentage"]', '30');
    await page.click('button[type="submit"]:has-text("+ Add Rule")');
    await expect(page.locator('text=30%').first()).toBeVisible({ timeout: 10000 });

    // Add Payout Slab 1 (0% - 15% water -> 50% payout)
    await page.fill('input[formControlName="lowerDeficitThreshold"]', '0');
    await page.fill('input[formControlName="upperDeficitThreshold"]', '15');
    await page.fill('input[formControlName="payoutPercentage"]', '50');
    await page.click('button[type="submit"]:has-text("+ Add Slab")');
    await expect(page.locator('text=50% Payout').first()).toBeVisible({ timeout: 10000 });

    // Add Payout Slab 2 (15% - 30% water -> 25% payout)
    await page.fill('input[formControlName="lowerDeficitThreshold"]', '15');
    await page.fill('input[formControlName="upperDeficitThreshold"]', '30');
    await page.fill('input[formControlName="payoutPercentage"]', '25');
    await page.click('button[type="submit"]:has-text("+ Add Slab")');
    await expect(page.locator('text=25% Payout').first()).toBeVisible({ timeout: 10000 });

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // =========================================================================
    // Stage 2: Farmer Registration, Profile, Farm Setup & Coverage Application
    // =========================================================================
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

    // Farmer Profile
    await page.goto('/farmer/profile');
    await page.waitForLoadState('networkidle');
    const editBtn = page.locator('button:has-text("Edit Profile")');
    if (await editBtn.isVisible()) {
      await editBtn.click();
    }
    await page.fill('input[formControlName="fullName"]', farmerName);
    await page.fill('input[formControlName="phone"]', '9898989898');
    await page.fill('input[formControlName="address"]', 'Plot 12, Morena Agricultural Sector');
    await page.fill('input[formControlName="district"]', 'Morena');
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
    await page.fill('input[formControlName="totalAreaHectares"]', '7.0');
    await page.selectOption('select[formControlName="soilType"]', 'SANDY_LOAM');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.click('button[type="submit"]:has-text("Save Farm")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeHidden({ timeout: 10000 });

    // Crop & Irrigation Profile
    await page.click(`text=${farmName} >> xpath=ancestor::div[contains(@class, "rounded-2xl")] >> a:has-text("Manage Crops")`);
    await page.waitForURL('**/farmer/farms/**', { timeout: 15000 });

    await page.click('button:has-text("+ Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="cropName"]', 'Golden Wheat');
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="growthStage"]', 'VEGETATIVE');
    await page.fill('input[formControlName="plantedAreaHectares"]', '6.0');
    await page.click('button[type="submit"]:has-text("Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator('text=Golden Wheat').first()).toBeVisible({ timeout: 10000 });

    const createIrrBtn = page.locator('button:has-text("+ Create Irrigation Profile")');
    if (await createIrrBtn.isVisible()) {
      await createIrrBtn.click();
    }
    await page.selectOption('select[formControlName="irrigationType"]', 'DRIP');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.selectOption('select[formControlName="equipmentCondition"]', 'GOOD');
    await page.fill('input[formControlName="efficiencyPercentage"]', '85');
    await page.click('button[type="submit"]:has-text("Save")');
    await expect(page.locator('button:has-text("Edit Profile")')).toBeVisible({ timeout: 10000 });

    // Apply for Coverage on newly created Plan
    await page.goto('/farmer/plans');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Active Insurance Plans');

    const targetPlanCard = page.locator(`text=${planName} >> xpath=ancestor::div[contains(@class, "rounded-2xl")]`).first();
    await targetPlanCard.locator('button:has-text("Apply for Coverage")').click();

    await expect(page.locator('h3:has-text("Apply for")')).toBeVisible({ timeout: 10000 });
    await page.waitForTimeout(500);
    await page.locator('select[formControlName="farmId"]').selectOption({ index: 1 });
    await page.waitForTimeout(500);
    await page.locator('select[formControlName="cropId"]').selectOption({ index: 1 });
    await page.fill('input[formControlName="requestedCoverageAmount"]', '60000');
    await page.click('button[type="submit"]:has-text("Submit Application")');
    await expect(page.locator('h3:has-text("Apply for")')).toBeHidden({ timeout: 10000 });

    await page.waitForURL('**/farmer/applications', { timeout: 15000 });
    const appHeaderText = await page.locator('span:has-text("Application #")').first().innerText();
    const applicationId = appHeaderText.replace('Application #', '').trim();

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // =========================================================================
    // Stage 3: Underwriter Reviews, Evaluates Risk & Approves Quote
    // =========================================================================
    await page.fill('input[formControlName="email"]', uwEmail);
    await page.fill('input[formControlName="password"]', 'Underwriter123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/underwriting', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Underwriter Application Review Queue');

    const targetCaseCard = page.locator(`span:has-text("Application #${applicationId}") >> xpath=ancestor::div[contains(@class, "rounded-2xl")]`).first();
    await expect(targetCaseCard).toBeVisible({ timeout: 10000 });
    await targetCaseCard.locator('button:has-text("Review & Decide")').click();
    await page.waitForTimeout(500);

    await page.click('button:has-text("Run Risk Telemetry Assessment")');
    await expect(page.getByText('Telemetry Risk Assessment:').first()).toBeVisible({ timeout: 10000 });

    await page.click('button[type="submit"]:has-text("Confirm Underwriting Decision")');
    await page.waitForTimeout(1000);

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // =========================================================================
    // Stage 4: Farmer Accepts Approved Quote and Binds Active Policy
    // =========================================================================
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

    // =========================================================================
    // Stage 5: Data Provider Submits Water Availability Reading (<30% threshold)
    // =========================================================================
    await page.fill('input[formControlName="email"]', dpEmail);
    await page.fill('input[formControlName="password"]', 'Provider123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/data-provider', { timeout: 15000 });
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('IoT Telemetry');

    // Submit 12% water availability reading (matches 0% - 15% slab -> 50% payout)
    await page.fill('input[formControlName="policyId"]', policyId);
    await page.fill('input[formControlName="availabilityPercentage"]', '12');
    await page.click('button[type="submit"]:has-text("Submit Telemetry Reading")');

    // Verify Evaluation Engine Response (created in PENDING_APPROVAL)
    await expect(page.locator('text=YES (Shortage)').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('span:has-text("PENDING_APPROVAL")').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=50%').first()).toBeVisible({ timeout: 10000 });

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // =========================================================================
    // Stage 6: Claims Officer Reviews & Approves Claim to Settle It
    // =========================================================================
    await page.fill('input[formControlName="email"]', 'officer@irrigashield.com');
    await page.fill('input[formControlName="password"]', 'Officer12345');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/claims-officer/claims', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Claims Review');

    // Approve the pending claim
    await page.locator('button:has-text("Approve & Settle Payout")').first().click();
    await page.waitForTimeout(1500);

    await page.click('button:has-text("Logout")');
    await page.waitForURL('**/login', { timeout: 15000 });

    // =========================================================================
    // Stage 7: Farmer Inspects Settled Claim & Submits Payout Feedback
    // =========================================================================
    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Sign In")');
    await page.waitForURL('**/farmer/farms', { timeout: 15000 });

    await page.goto('/farmer/claims');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Parametric Claims & Payouts');

    await expect(page.locator('span:has-text("SETTLED")').first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator(`text=Policy #${policyId}`).first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator('text=12%').first()).toBeVisible({ timeout: 10000 });
  });
});
