import type { ApiErrorPayload } from "./contracts";

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;

  constructor(status: number, payload: ApiErrorPayload) {
    super(payload.message);
    this.name = "ApiError";
    this.status = status;
    this.code = payload.code;
  }
}

export function isUnauthenticated(error: unknown): error is ApiError {
  return error instanceof ApiError && error.status === 401;
}

export function errorMessage(error: unknown, fallback = "Something went wrong."): string {
  return error instanceof Error && error.message ? error.message : fallback;
}
