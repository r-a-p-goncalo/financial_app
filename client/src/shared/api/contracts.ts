/**
 * HTTP representations for the v1 API.
 *
 * Keep this module free of React and browser APIs. It is the seam that can be
 * replaced by generated OpenAPI types once the server publishes a contract.
 */
export type Decimal = number | string;

export interface ApiErrorPayload {
  code: string;
  message: string;
}

export interface CsrfToken {
  headerName: string;
  token: string;
}

export interface User {
  userId: string;
  name: string;
}

export interface FinancialContext {
  financialContextId: string;
  name: string;
  parentFinancialContextId: string | null;
}

export interface Account {
  accountId: string;
  name: string;
  initialAmount: Decimal;
}

export interface Transaction {
  transactionId: string;
  originAccountId: string | null;
  targetAccountId: string | null;
  dateTime: string;
  value: Decimal;
}

export interface FinancialContextDetails {
  financialContext: FinancialContext;
  accounts: Account[];
  transactions: Transaction[];
}

export interface CredentialsInput {
  name: string;
  password: string;
}

export interface CreateFinancialContextInput {
  name: string;
}

export interface CreateAccountInput {
  name: string;
  initialAmount: string;
}

export interface CreateTransactionInput {
  originAccountId: string | null;
  targetAccountId: string | null;
  dateTime: string;
  value: string;
}
