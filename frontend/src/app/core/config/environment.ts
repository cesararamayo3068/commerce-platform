/**
 * Central application configuration.
 *
 * The API base URL and the temporary demo user are defined here and
 * nowhere else: components and services must never hardcode
 * "http://localhost:18080" or the user id 1.
 *
 * demoUserId is a temporary stand-in until authentication (JWT) is
 * implemented. It is documented in the project README.
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:18080/api',
  demoUserId: 1,
} as const;
