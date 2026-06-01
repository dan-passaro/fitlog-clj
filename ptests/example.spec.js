import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('http://localhost:8081/');
  await expect(page).toHaveTitle(/Fitlog/);
});

test('new workout button', async ({ page }) => {
  await page.goto('http://localhost:8081/');
  await page.getByRole('button', { name: 'New Workout' }).click();
  await expect(page.locator('#app')).toContainText('This is workout 0');
});
