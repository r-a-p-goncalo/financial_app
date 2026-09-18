import type {
  Account,
  CloneAccountInput,
  CreateAccountInput,
  CreateFinancialContextInput,
  CreateTransactionInput,
  CredentialsInput,
  CsrfToken,
  FinancialContext,
  FinancialContextDetails,
  Transaction,
  User,
} from "./contracts";

export interface ApiTransport {
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
}

/**
 * Platform-independent operation layer. Browser cookie/CSRF logic and future
 * native bearer-token logic both belong in their respective transports.
 */
export function createApiClient(transport: ApiTransport) {
  return {
    auth: {
      csrf: () => transport.get<CsrfToken>("/auth/csrf"),
      register: (input: CredentialsInput) => transport.post<User>("/auth/register", input),
      login: (input: CredentialsInput) => transport.post<User>("/auth/login", input),
      me: () => transport.get<User>("/auth/me"),
      logout: () => transport.post<void>("/auth/logout"),
    },
    financialContexts: {
      list: () => transport.get<FinancialContext[]>("/financial-contexts"),
      create: (input: CreateFinancialContextInput) =>
        transport.post<FinancialContext>("/financial-contexts", input),
      get: (financialContextId: string) =>
        transport.get<FinancialContextDetails>(`/financial-contexts/${financialContextId}`),
      clone: (financialContextId: string, name?: string) =>
        transport.post<FinancialContext>(
          `/financial-contexts/${financialContextId}/clones`,
          name ? { name } : undefined,
        ),
      createAccount: (financialContextId: string, input: CreateAccountInput) =>
        transport.post<Account>(`/financial-contexts/${financialContextId}/accounts`, input),
      cloneAccount: (financialContextId: string, input: CloneAccountInput) =>
        transport.post<Account>(`/financial-contexts/${financialContextId}/accounts/clones`, input),
      createTransaction: (financialContextId: string, input: CreateTransactionInput) =>
        transport.post<Transaction>(`/financial-contexts/${financialContextId}/transactions`, input),
    },
  };
}

export type FinancialApi = ReturnType<typeof createApiClient>;
