// SPDX-FileCopyrightText: 2026 2026 Dan Passaro
//
// SPDX-License-Identifier: AGPL-3.0-or-later
import { test, expect } from '@playwright/test'
import { readFile } from 'node:fs/promises'

test('data can be exported', async ({ page }, testInfo) => {
  await page.goto('.')
  await expect(page.getByRole('heading', {level: 2})).toContainText('Workouts')

  // create a workout so there's something to export
  await page.getByRole('button', {name: 'New Workout'}).click()
  await page.getByRole('link', {name: 'Back'}).click()

  // export data
  const downloadEvent = page.waitForEvent('download')
  await page.getByRole('button', {label: 'Tools menu'}).click()
  await page.getByRole('button', {name: 'Export'}).click()

  const download = await downloadEvent
  expect(download.suggestedFilename()).toBe('fitpad-data.json')

  const path = testInfo.outputPath('data.json')
  await download.saveAs(path)

  // If this doesn't raise, all good
  JSON.parse(await readFile(path, 'utf8'))
});
