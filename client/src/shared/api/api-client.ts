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
  UpdateAccountInput,
  UpdateFinancialContextInput,
  User,
} from "./contracts";

//Abstracts http requests for a path and a type of response
export interface ApiTransport {
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
  put<T>(path: string, body: unknown): Promise<T>;
  delete<T>(path: string): Promise<T>;
}

/**
 * Platform-independent operation layer. Browser cookie/CSRF logic and future
 * native bearer-token logic both belong in their respective transports.
 *
 * This creates an object that represents the client
 *
 */
export function createApiClient(transport: ApiTransport) {

  return {

    //when creating a client, we prepare the functions that represent the authentication
    auth: {
      csrf: () => transport.get<CsrfToken>("/auth/csrf"),

      register: (input: CredentialsInput) => transport.post<User>("/auth/register", input),

      login: (input: CredentialsInput) => transport.post<User>("/auth/login", input),

      me: () => transport.get<User>("/auth/me"),

      logout: () => transport.post<void>("/auth/logout"),
    },

    //when creating a client, we prepare the functions that represent its behavior regarding the financial contexts
    financialContexts: {
      list: () => transport.get<FinancialContext[]>("/financial-contexts"),

      create: (input: CreateFinancialContextInput) =>
        transport.post<FinancialContext>("/financial-contexts", input),

      update: (financialContextId: string, input: UpdateFinancialContextInput) =>
        transport.put<FinancialContext>(`/financial-contexts/${financialContextId}`, input),

      delete: (financialContextId: string) =>
        transport.delete<void>(`/financial-contexts/${financialContextId}`),

      get: (financialContextId: string) =>
        transport.get<FinancialContextDetails>(`/financial-contexts/${financialContextId}`),

      clone: (financialContextId: string, name?: string) =>
        transport.post<FinancialContext>(
          `/financial-contexts/${financialContextId}/clones`,
          name ? { name } : undefined,
        ),

      createAccount: (financialContextId: string, input: CreateAccountInput) =>
        transport.post<Account>(`/financial-contexts/${financialContextId}/accounts`, input),

      updateAccount: (
        financialContextId: string,
        account: Account,
        input: UpdateAccountInput,
      ) => transport.put<Account>(
        `/financial-contexts/${financialContextId}/accounts/${account.financialContextId}/${account.accountId}`,
        input,
      ),

      deleteAccount: (financialContextId: string, account: Account) =>
        transport.delete<void>(
          `/financial-contexts/${financialContextId}/accounts/${account.financialContextId}/${account.accountId}`,
        ),

      cloneAccount: (financialContextId: string, input: CloneAccountInput) =>
        transport.post<Account>(`/financial-contexts/${financialContextId}/accounts/clones`, input),

      createTransaction: (financialContextId: string, input: CreateTransactionInput) =>
        transport.post<Transaction>(`/financial-contexts/${financialContextId}/transactions`, input),

      updateTransaction: (
        financialContextId: string,
        transaction: Transaction,
        input: CreateTransactionInput,
      ) => transport.put<Transaction>(
        `/financial-contexts/${financialContextId}/transactions/${transaction.financialContextId}/${transaction.transactionId}`,
        input,
      ),

      deleteTransaction: (financialContextId: string, transaction: Transaction) =>
        transport.delete<void>(
          `/financial-contexts/${financialContextId}/transactions/${transaction.financialContextId}/${transaction.transactionId}`,
        ),
    },
  };
}
