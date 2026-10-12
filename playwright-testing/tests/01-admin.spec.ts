import { test, expect } from '@playwright/test';

async function loginAsAdmin(page: any) {
  await page.goto('/login');
  await page.waitForLoadState('networkidle');
  await page.fill('input[formControlName="email"]', 'admin@irrigashield.com');
  await page.fill('input[formControlName="password"]', 'Admin12345');
  await page.click('button[type="submit"]:has-text("Sign In")');
  await page.waitForURL('**/admin/users', { timeout: 15000 });
}

test.describe('Admin Workflows - User Provisioning, Plans, Rules & Slabs', () => {
  test('Admin logs in and provisions Underwriter and Data Provider', async ({ page }) => {
    const timestamp = Date.now();
    const uwEmail = `uw_${timestamp}@irrigashield.com`;
    const dpEmail = `dp_${timestamp}@irrigashield.com`;

    await loginAsAdmin(page);
    await expect(page.locator('h1')).toContainText('User Provisioning & Role Management');

    // Provision Underwriter
    await page.click('button:has-text("+ Provision User")');
    await page.fill('input[formControlName="name"]', 'Test Underwriter');
    await page.fill('input[formControlName="email"]', uwEmail);
    await page.fill('input[formControlName="password"]', 'Underwriter123');
    await page.selectOption('select[formControlName="role"]', 'UNDERWRITER');
    await page.click('button[type="submit"]:has-text("Create User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator(`td:has-text("${uwEmail}")`)).toBeVisible({ timeout: 10000 });

    // Provision Data Provider
    await page.click('button:has-text("+ Provision User")');
    await page.fill('input[formControlName="name"]', 'Test Data Provider');
    await page.fill('input[formControlName="email"]', dpEmail);
    await page.fill('input[formControlName="password"]', 'Provider123');
    await page.selectOption('select[formControlName="role"]', 'DATA_PROVIDER');
    await page.click('button[type="submit"]:has-text("Create User")');
    await expect(page.locator('h3:has-text("Provision New System Account")')).toBeHidden({ timeout: 10000 });
    await expect(page.locator(`td:has-text("${dpEmail}")`)).toBeVisible({ timeout: 10000 });
  });

  test('Admin creates Insurance Plan, Trigger Rule, and Payout Slab', async ({ page }) => {
    const timestamp = Date.now();
    const planName = `Wheat Shield ${timestamp}`;

    await loginAsAdmin(page);

    // Navigate to Plans & Rules
    await page.click('a:has-text("Plans & Rules")');
    await page.waitForURL('**/admin/plans', { timeout: 15000 });
    await expect(page.locator('h1')).toContainText('Insurance Plans');

    // Create Plan
    await page.click('button:has-text("+ Create Plan")');
    await page.fill('input[formControlName="planName"]', planName);
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="season"]', 'RABI');
    await page.fill('input[formControlName="maxCoverageAmount"]', '100000');
    await page.fill('input[formControlName="basePremiumRate"]', '5');
    await page.fill('textarea[formControlName="description"]', 'Comprehensive Rabi wheat parametric soil moisture deficit protection');
    await page.click('button[type="submit"]:has-text("Create Plan")');
    await expect(page.locator('h3:has-text("Create Insurance Plan")')).toBeHidden({ timeout: 10000 });

    // Verify Plan is visible and click it
    const planCard = page.locator(`text=${planName}`).first();
    await expect(planCard).toBeVisible({ timeout: 10000 });
    await planCard.click();
    await page.waitForTimeout(500);

    // Add Trigger Rule
    await page.selectOption('select[formControlName="cropType"]', 'WHEAT');
    await page.selectOption('select[formControlName="growthStage"]', 'VEGETATIVE');
    await page.fill('input[formControlName="availabilityThresholdPercentage"]', '25');
    await page.click('button[type="submit"]:has-text("+ Add Rule")');
    await expect(page.locator('text=25%').first()).toBeVisible({ timeout: 10000 });

    // Add Payout Slab
    await page.fill('input[formControlName="lowerDeficitThreshold"]', '0');
    await page.fill('input[formControlName="upperDeficitThreshold"]', '25');
    await page.fill('input[formControlName="payoutPercentage"]', '50');
    await page.click('button[type="submit"]:has-text("+ Add Slab")');
    await expect(page.locator('text=50% Payout').first()).toBeVisible({ timeout: 10000 });
  });
});
