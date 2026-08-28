// @ts-check
import { test, expect } from '@playwright/test';

test('application is reachable', async ({ page }) => {
  const response = await page.goto('/');

  expect(response).not.toBeNull();
  expect(response.ok()).toBeTruthy();
});
