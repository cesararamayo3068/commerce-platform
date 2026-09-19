/**
 * Production application configuration.
 *
 * Vercel uses this file through Angular's production file replacement.
 * Local development continues to use environment.ts and localhost:18080.
 */
export const environment = {
  production: true,
  apiUrl: 'https://commerce-platform-backend.onrender.com/api',
  demoUserId: 1,
} as const;
