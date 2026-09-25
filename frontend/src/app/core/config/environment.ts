/**
 * Central application configuration.
 *
 * The API base URL and the temporary demo user are defined here and
 * nowhere else: components and services must never hardcode
 * "http://localhost:18081" or the user id 1.
 *
 * Authentication supplies the user id at runtime.
 */
export const environment = {
  demoUserId: 1, // legacy test fixture; runtime uses authenticated userId
  production: false,
  apiUrl: 'http://localhost:18081/api',
} as const;
