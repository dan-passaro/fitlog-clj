import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('.')
  await expect(page).toHaveTitle(/Fitlog/)
});

test('new workout button', async ({ page }) => {
  await page.goto('.')
  await page.getByRole('button', { name: 'New Workout' }).click()
  await expect(page.locator('#app')).toContainText('This is workout 0')
});

test('persists data in local storage', async ({ page}) => {
  await page.goto('.')
  await page.getByRole('button', { name: 'New Workout' }).click()
  await page.getByRole('button', { name: 'Go Home'}).click()
  await expect(page.getByText('Workout 0')).toBeVisible()
  await page.reload()
  await expect(page.getByText('Workout 0')).toBeVisible()
})
