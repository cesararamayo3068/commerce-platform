/**
 * Production application configuration.
 *
 * Vercel uses this file through Angular's production file replacement.
 * Local development continues to use environment.ts and localhost:18080.
 */
export const environment = {
  demoUserId: 1, // legacy test fixture; runtime uses authenticated userId
  production: true,
  apiUrl: 'https://commerce-platform-backend.onrender.com/api',
} as const;
