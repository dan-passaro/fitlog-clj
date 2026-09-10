// SPDX-FileCopyrightText: 2026 2026 Dan Passaro
//
// SPDX-License-Identifier: AGPL-3.0-or-later

import { test, expect } from '@playwright/test';

test('has title', async ({ page }) => {
  await page.goto('.')
  await expect(page).toHaveTitle(/Fitlog/)
});

test('new workout button', async ({ page }) => {
  await page.goto('.')
  await expect(page.getByRole('button', { name: 'New Workout' })).toBeVisible()
});

test('persists data in local storage', async ({ page}) => {
  await page.goto('.')

  // Precondition: should have no data yet
  await expect(page.getByText('Today')).not.toBeVisible()

  // Create a workout
  await page.getByRole('button', { name: 'New Workout' }).click()
  await page.getByRole('link', { name: 'Back'}).click()

  // See workout exists
  await expect(page.getByText('Today')).toBeVisible()

  // Reload the page and ensure it still exists
  await page.reload()
  await expect(page.getByText('Today')).toBeVisible()
})
