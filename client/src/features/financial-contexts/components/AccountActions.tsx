import { type FormEvent, useState } from "react";
import type { Account } from "../../../shared/api/contracts";
import { errorMessage } from "../../../shared/api/api-error";
import { isValidDecimal } from "../../../shared/lib/format";
import { useDeleteAccount, useUpdateAccount } from "../queries";

interface AccountActionsProps {
  financialContextId: string;
  account: Account;
  onDeleted: () => void;
  onSaved: (account: Account) => void;
}

export function AccountActions({ financialContextId, account, onDeleted, onSaved }: AccountActionsProps) {
  const update = useUpdateAccount(financialContextId);
  const remove = useDeleteAccount(financialContextId);
  const [editing, setEditing] = useState(false);
  const [name, setName] = useState(account.name);
  const [initialAmount, setInitialAmount] = useState(String(account.initialAmount));
  const [localError, setLocalError] = useState<string>();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!name.trim() || !isValidDecimal(initialAmount)) {
      setLocalError("Enter an account name and a valid initial amount.");
      return;
    }
    const saved = await update.mutateAsync({
      account,
      input: { name: name.trim(), initialAmount },
    });
    setEditing(false);
    onSaved(saved);
  }

  async function deleteAccount() {
    if (!window.confirm(`Delete “${account.name}”? Existing transactions are retained.`)) return;
    await remove.mutateAsync(account);
    onDeleted();
  }

  return (
    <section className="panel compact-form" aria-label="Account settings">
      <h2>Account settings</h2>
      {editing ? (
        <form className="stack-form" onSubmit={submit} noValidate>
          <label htmlFor="edit-account-name">Name<input id="edit-account-name" value={name} onChange={(event) => setName(event.target.value)} /></label>
          <label htmlFor="edit-account-initial">Initial amount<input id="edit-account-initial" value={initialAmount} onChange={(event) => setInitialAmount(event.target.value)} inputMode="decimal" /></label>
          {localError && <p className="form-error" role="alert">{localError}</p>}
          {update.isError && <p className="form-error" role="alert">{errorMessage(update.error)}</p>}
          <div className="dialog-actions"><button className="button button-quiet" type="button" onClick={() => setEditing(false)}>Cancel</button><button className="button button-primary" type="submit" disabled={update.isPending}>{update.isPending ? "Saving…" : "Save changes"}</button></div>
        </form>
      ) : (
        <button className="button button-quiet" type="button" onClick={() => setEditing(true)}>Edit account</button>
      )}
      <button className="button button-quiet" type="button" onClick={deleteAccount} disabled={remove.isPending}>{remove.isPending ? "Deleting…" : "Delete account"}</button>
      {remove.isError && <p className="form-error" role="alert">{errorMessage(remove.error)}</p>}
    </section>
  );
}
