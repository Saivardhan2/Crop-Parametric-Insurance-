import { test, expect } from '@playwright/test';

test.describe('Farmer Onboarding - Registration, KYC, Farm, Crop & Irrigation', () => {
  test('Farmer registers account, completes KYC profile, adds farm, and configures crop and irrigation', async ({ page }) => {
    const timestamp = Date.now();
    const farmerName = `Ramesh Patel ${timestamp}`;
    const farmerEmail = `farmer_${timestamp}@example.com`;
    const farmName = `Narmada Valley Plot ${timestamp}`;

    // Step 1: Register New Farmer
    await page.goto('/register');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h2')).toContainText('Farmer Registration');

    await page.fill('input[formControlName="name"]', farmerName);
    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Register Account")');

    // Step 2: Login with New Farmer
    await page.waitForURL('**/login', { timeout: 15000 });
    await expect(page.locator('h2')).toContainText('Sign In');

    await page.fill('input[formControlName="email"]', farmerEmail);
    await page.fill('input[formControlName="password"]', 'Password123');
    await page.click('button[type="submit"]:has-text("Sign In")');

    await page.waitForURL('**/farmer/farms', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Registered Farm Plots');

    // Step 3: Complete KYC Profile
    await page.goto('/farmer/profile');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('h1')).toContainText('Farmer Profile');

    const editBtn = page.locator('button:has-text("Edit Profile")');
    if (await editBtn.isVisible()) {
      await editBtn.click();
    }
    await page.fill('input[formControlName="fullName"]', farmerName);
    await page.fill('input[formControlName="phone"]', '9876543210');
    await page.fill('input[formControlName="address"]', 'Rampur Village, Indore');
    await page.fill('input[formControlName="district"]', 'Indore');
    await page.fill('input[formControlName="state"]', 'Madhya Pradesh');
    await page.fill('input[formControlName="bankAccountNumber"]', '9012');
    await page.click('button[type="submit"]');
    await expect(page.locator('span:has-text("VERIFIED")')).toBeVisible({ timeout: 10000 });

    // Step 4: Register Farm Plot
    await page.goto('/farmer/farms');
    await page.waitForLoadState('networkidle');
    await page.click('button:has-text("+ Add Farm Plot")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeVisible({ timeout: 10000 });

    await page.fill('input[formControlName="farmName"]', farmName);
    await page.fill('input[formControlName="totalAreaHectares"]', '5.0');
    await page.selectOption('select[formControlName="soilType"]', 'SANDY_LOAM');
    await page.selectOption('select[formControlName="waterSource"]', 'CANAL');
    await page.click('button[type="submit"]:has-text("Save Farm")');
    await expect(page.locator('h3:has-text("Register Farm Plot")')).toBeHidden({ timeout: 10000 });

    // Verify Farm appears in list
    const farmCard = page.locator(`text=${farmName}`).first();
    await expect(farmCard).toBeVisible({ timeout: 10000 });

    // Step 5: Add Crop & Configure Irrigation Profile
    await page.click(`text=${farmName} >> xpath=ancestor::div[contains(@class, "rounded-2xl")] >> a:has-text("Manage Crops")`);
    await page.waitForURL('**/farmer/farms/**', { timeout: 15000 });
    await expect(page.locator('h2:has-text("Cultivated Crops")')).toBeVisible({ timeout: 10000 });

    // Add Crop
    await page.click('button:has-text("+ Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeVisible({ timeout: 10000 });
    await page.fill('input[formControlName="cropName"]', 'Sharbati Wheat');
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="growthStage"]', 'VEGETATIVE');
    await page.fill('input[formControlName="plantedAreaHectares"]', '3.0');
    await page.click('button[type="submit"]:has-text("Add Crop")');
    await expect(page.locator('h3:has-text("Add Crop to Farm")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator('text=Sharbati Wheat').first()).toBeVisible({ timeout: 10000 });

    // Save Irrigation Profile
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
  });
});
