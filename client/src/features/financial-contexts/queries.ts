import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../shared/api/browser-transport";
import type {
  CloneAccountInput,
  CreateAccountInput,
  CreateFinancialContextInput,
  CreateTransactionInput,
} from "../../shared/api/contracts";

export const financialContextKeys = {
  all: ["financial-contexts"] as const,
  details: (financialContextId: string) => ["financial-contexts", financialContextId] as const,
};

export function useFinancialContexts() {
  return useQuery({
    queryKey: financialContextKeys.all,
    queryFn: api.financialContexts.list,
  });
}

export function useFinancialContext(financialContextId: string | undefined) {
  return useQuery({
    queryKey: financialContextKeys.details(financialContextId ?? "missing"),
    queryFn: () => api.financialContexts.get(financialContextId!),
    enabled: Boolean(financialContextId),
  });
}

export function useCreateFinancialContext() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateFinancialContextInput) => api.financialContexts.create(input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: financialContextKeys.all }),
  });
}

export function useCloneFinancialContext() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ financialContextId, name }: { financialContextId: string; name?: string }) =>
      api.financialContexts.clone(financialContextId, name),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: financialContextKeys.all }),
  });
}

export function useCreateAccount(financialContextId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateAccountInput) =>
      api.financialContexts.createAccount(financialContextId, input),
    // A new parent account is visible in every active child context.
    onSuccess: () => queryClient.invalidateQueries({
      queryKey: financialContextKeys.all,
    }),
  });
}

export function useCloneAccount(financialContextId: string) {
  return useMutation({
    mutationFn: (input: CloneAccountInput) =>
      api.financialContexts.cloneAccount(financialContextId, input),
  });
}

export function useCreateTransaction(financialContextId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: CreateTransactionInput) =>
      api.financialContexts.createTransaction(financialContextId, input),
    // A transaction in a parent context changes every active clone's
    // effective transaction stream, so all context queries become stale.
    onSuccess: () => queryClient.invalidateQueries({
      queryKey: financialContextKeys.all,
    }),
  });
}
