@echo off
echo ===================================================
echo Launching Playwright E2E Test in Headed Mode
echo ===================================================
cd /d d:\Capstone\playwright-testing
npx playwright test tests/05-full-e2e-journey.spec.ts --headed
pause
