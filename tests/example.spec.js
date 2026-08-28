// @ts-check
import { test, expect } from '@playwright/test';

test('application is reachable', async ({ page }) => {
  const response = await page.goto('/');

  expect(response).not.toBeNull();
  expect(response.status()).toBeGreaterThanOrEqual(200);
  expect(response.status()).toBeLessThan(500);

  const holdTime = Number(process.env.PLAYWRIGHT_HOLD_MS || 0);
  if (holdTime > 0) {
    await new Promise((resolve) => setTimeout(resolve, holdTime));
  }
});
