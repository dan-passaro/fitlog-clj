import { test, expect } from '@playwright/test';

test('navigable from the home screen', async ({page}) => {
  await page.goto('.')
  await page.getByRole('button', { name: 'New Workout' }).click()
  await expect(page.getByRole('heading')).toContainText('Today');
  await expect(page.getByRole('paragraph')).toContainText('No exercises. Add an exercise to get started!');

})
