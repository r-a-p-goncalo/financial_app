import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, browserTransport } from "../../shared/api/browser-transport";
import type { CredentialsInput, User } from "../../shared/api/contracts";

export const sessionQueryKey = ["auth", "session"] as const;

export function useSession() {
  return useQuery({
    queryKey: sessionQueryKey,
    queryFn: api.auth.me,
    retry: false,
    staleTime: Infinity,
  });
}

function useAuthentication(action: (input: CredentialsInput) => Promise<User>) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: action,
    onSuccess: (user) => {
      browserTransport.clearCsrfToken();
      queryClient.setQueryData(sessionQueryKey, user);
    },
  });
}

export function useLogin() {
  return useAuthentication(api.auth.login);
}

export function useRegister() {
  return useAuthentication(api.auth.register);
}

export function useLogout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: api.auth.logout,
    onSuccess: () => {
      browserTransport.clearCsrfToken();
      queryClient.clear();
    },
  });
}
