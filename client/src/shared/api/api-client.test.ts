import { describe, expect, test } from "vitest";
import { createApiClient, type ApiTransport } from "./api-client";


//describe serves to group related tests
describe("API client", () => {

  //it and Test mean the same in vit, defining a set of related expectations
  test("keeps API paths and request payloads in the shared operation layer", async () => {

    //the array where we'll store the generated calls
    const calls: Array<{ method: string; path: string; body?: unknown }> = [];


    const transport: ApiTransport = {
      get: async <T>(path: string) => {
        calls.push({ method: "GET", path });
        return [] as T;
      },
      post: async <T>(path: string, body?: unknown) => {
        calls.push({ method: "POST", path, body });
        return {
          financialContextId: "ctx-1",
          name: "Personal",
          parentFinancialContextId: null,
        } as T;
      },
    };

    const client = createApiClient(transport);
    await client.financialContexts.createAccount("ctx-1", {
      name: "Current account",
      initialAmount: "12.50",
    });
    await client.financialContexts.cloneAccount("ctx-1", {
      sourceAccountId: "account-1",
      sourceFinancialContextId: "parent-ctx",
    });

    expect(calls).toEqual([
      {
        method: "POST",
        path: "/financial-contexts/ctx-1/accounts",
        body: { name: "Current account", initialAmount: "12.50" },
      },
      {
        method: "POST",
        path: "/financial-contexts/ctx-1/accounts/clones",
        body: {
          sourceAccountId: "account-1",
          sourceFinancialContextId: "parent-ctx",
        },
      },
    ]);
  });
});
