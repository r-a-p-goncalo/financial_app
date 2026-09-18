import { type FormEvent, useId, useState } from "react";
import type { Account } from "../../../shared/api/contracts";
import { errorMessage } from "../../../shared/api/api-error";
import { currentDateTimeInputValue, dateTimeInputToInstant, isPositiveDecimal } from "../../../shared/lib/format";
import { useCreateTransaction } from "../queries";

interface TransactionFormProps {
  financialContextId: string;
  accounts: Account[];
}

export function TransactionForm({ financialContextId, accounts }: TransactionFormProps) {
  const createTransaction = useCreateTransaction(financialContextId);
  const formId = useId();
  const [originAccountId, setOriginAccountId] = useState("");
  const [targetAccountId, setTargetAccountId] = useState("");
  const [value, setValue] = useState("");
  const [dateTime, setDateTime] = useState(currentDateTimeInputValue);
  const [localError, setLocalError] = useState<string>();
  const [successMessage, setSuccessMessage] = useState<string>();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(undefined);
    setSuccessMessage(undefined);

    if (!originAccountId && !targetAccountId) {
      setLocalError("Choose an origin or a destination account.");
      return;
    }
    if (originAccountId && originAccountId === targetAccountId) {
      setLocalError("The origin and destination must be different.");
      return;
    }
    if (!isPositiveDecimal(value)) {
      setLocalError("Enter an amount greater than zero.");
      return;
    }

    try {
      await createTransaction.mutateAsync({
        originAccountId: originAccountId || null,
        targetAccountId: targetAccountId || null,
        dateTime: dateTimeInputToInstant(dateTime),
        value,
      });
      setOriginAccountId("");
      setTargetAccountId("");
      setValue("");
      setDateTime(currentDateTimeInputValue());
      setSuccessMessage("Transaction recorded.");
    } catch (error) {
      if (error instanceof Error && error.message === "Choose a valid date and time.") {
        setLocalError(error.message);
      }
      // Mutation state provides server errors.
    }
  }

  return (
    <form className="stack-form compact-form" onSubmit={submit} noValidate>
      <h3>Record a transaction</h3>
      <label htmlFor={`${formId}-origin`}>
        From
        <select id={`${formId}-origin`} value={originAccountId} onChange={(event) => setOriginAccountId(event.target.value)}>
          <option value="">External / not recorded</option>
          {accounts.map((account) => <option key={account.accountId} value={account.accountId}>{account.name}</option>)}
        </select>
      </label>
      <label htmlFor={`${formId}-target`}>
        To
        <select id={`${formId}-target`} value={targetAccountId} onChange={(event) => setTargetAccountId(event.target.value)}>
          <option value="">External / not recorded</option>
          {accounts.map((account) => <option key={account.accountId} value={account.accountId}>{account.name}</option>)}
        </select>
      </label>
      <label htmlFor={`${formId}-amount`}>
        Amount
        <input id={`${formId}-amount`} value={value} onChange={(event) => setValue(event.target.value)} inputMode="decimal" required />
      </label>
      <label htmlFor={`${formId}-date-time`}>
        Date and time
        <input id={`${formId}-date-time`} type="datetime-local" value={dateTime} onChange={(event) => setDateTime(event.target.value)} required />
      </label>
      <p className="field-hint">Choose External on one side to record income or an expense. Transfers between recorded accounts do not change the context total.</p>
      {localError && <p className="form-error" role="alert">{localError}</p>}
      {createTransaction.isError && <p className="form-error" role="alert">{errorMessage(createTransaction.error)}</p>}
      {successMessage && <p className="form-success" role="status">{successMessage}</p>}
      <button className="button button-secondary" type="submit" disabled={createTransaction.isPending || accounts.length === 0}>
        {createTransaction.isPending ? "Recording…" : "Record transaction"}
      </button>
    </form>
  );
}
