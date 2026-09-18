import { ApiError } from "./api-error";
import { createApiClient, type ApiTransport } from "./api-client";
import type { ApiErrorPayload, CsrfToken } from "./contracts";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

function endpoint(path: string): string {
  return `${apiBaseUrl}${path}`;
}

async function responseData<T>(response: Response): Promise<T> {
  if (response.ok) {
    if (response.status === 204) {
      return undefined as T;
    }
    return (await response.json()) as T;
  }

  let payload: ApiErrorPayload = {
    code: "request_failed",
    message: "The request could not be completed.",
  };

  try {
    payload = (await response.json()) as ApiErrorPayload;
  } catch {
    // A reverse proxy or network failure can return a non-JSON response.
  }

  throw new ApiError(response.status, payload);
}

/** Browser-specific session and CSRF implementation. */
export class BrowserTransport implements ApiTransport {
  private csrfToken: CsrfToken | undefined;

  async get<T>(path: string): Promise<T> {
    const response = await fetch(endpoint(path), { credentials: "include" });
    return responseData<T>(response);
  }

  async post<T>(path: string, body?: unknown): Promise<T> {
    const csrfToken = await this.getCsrfToken();
    const response = await fetch(endpoint(path), {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        [csrfToken.headerName]: csrfToken.token,
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    return responseData<T>(response);
  }

  clearCsrfToken(): void {
    this.csrfToken = undefined;
  }

  private async getCsrfToken(): Promise<CsrfToken> {
    if (!this.csrfToken) {
      this.csrfToken = await this.get<CsrfToken>("/auth/csrf");
    }
    return this.csrfToken;
  }
}

export const browserTransport = new BrowserTransport();
export const api = createApiClient(browserTransport);
