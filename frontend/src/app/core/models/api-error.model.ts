/**
 * Consistent error body returned by the backend (ApiError).
 */
export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors: ApiFieldError[] | null;
}
