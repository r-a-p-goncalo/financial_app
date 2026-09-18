import { type FormEvent, useState } from "react";
import { errorMessage } from "../../../shared/api/api-error";
import { isValidDecimal } from "../../../shared/lib/format";
import { useCreateAccount } from "../queries";

interface AccountFormProps {
  financialContextId: string;
}

export function AccountForm({ financialContextId }: AccountFormProps) {
  const createAccount = useCreateAccount(financialContextId);
  const [name, setName] = useState("");
  const [initialAmount, setInitialAmount] = useState("0");
  const [localError, setLocalError] = useState<string>();
  const [successMessage, setSuccessMessage] = useState<string>();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(undefined);
    setSuccessMessage(undefined);

    if (!name.trim()) {
      setLocalError("Enter an account name.");
      return;
    }
    if (!isValidDecimal(initialAmount)) {
      setLocalError("Enter a valid initial amount.");
      return;
    }

    try {
      await createAccount.mutateAsync({ name: name.trim(), initialAmount });
      setName("");
      setInitialAmount("0");
      setSuccessMessage("Account added.");
    } catch {
      // Mutation state provides the server error.
    }
  }

  return (
    <form className="stack-form compact-form" onSubmit={submit} noValidate>
      <h3>Add an account</h3>
      <label htmlFor="account-name">
        Name
        <input id="account-name" value={name} onChange={(event) => setName(event.target.value)} placeholder="e.g. Everyday account" required />
      </label>
      <label htmlFor="account-initial-amount">
        Initial amount
        <input id="account-initial-amount" value={initialAmount} onChange={(event) => setInitialAmount(event.target.value)} inputMode="decimal" required />
      </label>
      <p className="field-hint">Currency/unit support will be added by the API; enter a neutral decimal amount for now.</p>
      {localError && <p className="form-error" role="alert">{localError}</p>}
      {createAccount.isError && <p className="form-error" role="alert">{errorMessage(createAccount.error)}</p>}
      {successMessage && <p className="form-success" role="status">{successMessage}</p>}
      <button className="button button-secondary" type="submit" disabled={createAccount.isPending}>
        {createAccount.isPending ? "Adding…" : "Add account"}
      </button>
    </form>
  );
}
