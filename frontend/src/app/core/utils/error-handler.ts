import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';

/**
 * Maps an HTTP error to a human-readable message.
 *
 * The backend always answers with the ApiError body; network failures and
 * timeouts have no body and are handled separately. Stack traces are never
 * shown to the user.
 */
export function toUserMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor. Verificá que el backend esté disponible.';
    }
    const body = err.error as ApiError | null;
    if (body && typeof body.message === 'string' && body.message.length > 0) {
      return body.message;
    }
    return `Error ${err.status} ${err.statusText ?? ''}`.trim();
  }
  return 'Ocurrió un error inesperado.';
}

/**
 * Extracts per-field validation messages from an ApiError body.
 */
export function fieldErrorsOf(err: unknown): Map<string, string> {
  const map = new Map<string, string>();
  if (err instanceof HttpErrorResponse && err.status === 400) {
    const body = err.error as ApiError | null;
    if (body && Array.isArray(body.fieldErrors)) {
      for (const fe of body.fieldErrors) {
        map.set(fe.field, fe.message);
      }
    }
  }
  return map;
}
