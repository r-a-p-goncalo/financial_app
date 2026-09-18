import { type FormEvent, useId, useRef, useState } from "react";
import type { Account } from "../../../shared/api/contracts";
import { errorMessage } from "../../../shared/api/api-error";
import { dateTimeInputToInstant, isPositiveDecimal } from "../../../shared/lib/format";
import { useCloneAccount, useCreateTransaction } from "../queries";

type TransactionType = "income" | "expense" | "transfer";

const transactionTypes: Record<TransactionType, { title: string; description: string }> = {
  income: { title: "income", description: "Money entering a recorded account from an external source." },
  expense: { title: "expense", description: "Money leaving a recorded account for an external destination." },
  transfer: { title: "transfer", description: "Money moving between two recorded accounts." },
};

interface TransactionDialogProps {
  accounts: Account[];
  financialContextId: string;
  defaultAccountId?: string;
  onTransactionSaved?: (clonedAccountIds: ReadonlyMap<string, string>) => void;
}

export function TransactionDialog({
  accounts,
  financialContextId,
  defaultAccountId,
  onTransactionSaved,
}: TransactionDialogProps) {
  const createTransaction = useCreateTransaction(financialContextId);
  const cloneAccount = useCloneAccount(financialContextId);
  const dialogRef = useRef<HTMLDialogElement>(null);
  const headingId = useId();
  const [transactionType, setTransactionType] = useState<TransactionType>();
  const [originAccountId, setOriginAccountId] = useState("");
  const [targetAccountId, setTargetAccountId] = useState("");
  const [value, setValue] = useState("");
  const [dateTime, setDateTime] = useState("");
  const [localError, setLocalError] = useState<string>();

  function resetForm() {
    createTransaction.reset();
    cloneAccount.reset();
    setTransactionType(undefined);
    setOriginAccountId("");
    setTargetAccountId("");
    setValue("");
    setDateTime("");
    setLocalError(undefined);
  }

  function open() {
    resetForm();
    dialogRef.current?.showModal();
  }

  function chooseTransactionType(type: TransactionType) {
    setTransactionType(type);
    setOriginAccountId(type === "income" ? "" : defaultAccountId ?? "");
    setTargetAccountId(type === "expense" ? "" : type === "income" ? defaultAccountId ?? "" : "");
  }

  function close() {
    dialogRef.current?.close();
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLocalError(undefined);

    if (!transactionType) return;

    const originId = transactionType === "income" ? null : originAccountId || null;
    const targetId = transactionType === "expense" ? null : targetAccountId || null;

    if (!originId && !targetId) {
      setLocalError("Choose an account.");
      return;
    }
    if (transactionType === "transfer" && (!originId || !targetId)) {
      setLocalError("Choose both accounts for a transfer.");
      return;
    }
    if (originId && originId === targetId) {
      setLocalError("The origin and destination must be different.");
      return;
    }
    if (!isPositiveDecimal(value)) {
      setLocalError("Enter an amount greater than zero.");
      return;
    }

    try {
      const clonedAccountIds = new Map<string, string>();
      const localOriginAccountId = await localAccountId(originId, clonedAccountIds);
      const localTargetAccountId = await localAccountId(targetId, clonedAccountIds);

      await createTransaction.mutateAsync({
        originAccountId: localOriginAccountId,
        targetAccountId: localTargetAccountId,
        dateTime: dateTime ? dateTimeInputToInstant(dateTime) : new Date().toISOString(),
        value,
      });
      onTransactionSaved?.(clonedAccountIds);
      close();
    } catch (error) {
      if (error instanceof Error && (
        error.message === "Choose a valid date and time."
        || error.message === "The selected account is no longer available."
      )) {
        setLocalError(error.message);
      }
      // Mutation state provides server errors.
    }
  }

  async function localAccountId(
    accountId: string | null,
    clonedAccountIds: Map<string, string>,
  ): Promise<string | null> {
    if (!accountId) return null;

    const account = accounts.find((candidate) => candidate.accountId === accountId);
    if (!account) {
      throw new Error("The selected account is no longer available.");
    }
    if (account.financialContextId === financialContextId) {
      return account.accountId;
    }

    const clone = await cloneAccount.mutateAsync({
      sourceAccountId: account.accountId,
      sourceFinancialContextId: account.financialContextId,
    });
    clonedAccountIds.set(account.accountId, clone.accountId);
    return clone.accountId;
  }

  const selectedType = transactionType ? transactionTypes[transactionType] : undefined;

  return (
    <>
      <button className="button button-secondary" type="button" onClick={open} disabled={accounts.length === 0}>
        Add transaction
      </button>
      {accounts.length === 0 && <p className="field-hint">Add an account before recording a transaction.</p>}
      <dialog className="transaction-dialog" ref={dialogRef} aria-labelledby={headingId} onClose={resetForm}>
        <div className="dialog-header">
          <div>
            <p className="eyebrow">Transaction</p>
            <h2 id={headingId}>{selectedType ? `Add ${selectedType.title}` : "Add a transaction"}</h2>
          </div>
          <button className="dialog-close" type="button" onClick={close} aria-label="Close transaction dialog">×</button>
        </div>
        {!transactionType && (
          <div className="transaction-type-grid">
            {(Object.keys(transactionTypes) as TransactionType[]).map((type) => (
              <button className="transaction-type-option" type="button" key={type} onClick={() => chooseTransactionType(type)}>
                <span>{`Add ${transactionTypes[type].title}`}</span>
                <small>{transactionTypes[type].description}</small>
              </button>
            ))}
          </div>
        )}
        {selectedType && (
          <form className="stack-form transaction-form" onSubmit={submit} noValidate>
            <p className="muted">{selectedType.description}</p>
            {transactionType !== "income" && (
              <label htmlFor={`${headingId}-origin`}>
                From account
                <select id={`${headingId}-origin`} value={originAccountId} onChange={(event) => setOriginAccountId(event.target.value)} required>
                  <option value="">Choose an account</option>
                  {accounts.map((account) => <option key={account.accountId} value={account.accountId}>{account.name}</option>)}
                </select>
              </label>
            )}
            {transactionType !== "expense" && (
              <label htmlFor={`${headingId}-target`}>
                To account
                <select id={`${headingId}-target`} value={targetAccountId} onChange={(event) => setTargetAccountId(event.target.value)} required>
                  <option value="">Choose an account</option>
                  {accounts.map((account) => <option key={account.accountId} value={account.accountId}>{account.name}</option>)}
                </select>
              </label>
            )}
            <label htmlFor={`${headingId}-amount`}>
              Amount
              <input id={`${headingId}-amount`} value={value} onChange={(event) => setValue(event.target.value)} inputMode="decimal" required />
            </label>
            <label htmlFor={`${headingId}-date-time`}>
              Date and time <span className="label-optional">optional</span>
              <input id={`${headingId}-date-time`} type="datetime-local" value={dateTime} onChange={(event) => setDateTime(event.target.value)} />
            </label>
            <p className="field-hint">Leave the date blank to use the current date and time when you save.</p>
            {localError && <p className="form-error" role="alert">{localError}</p>}
            {cloneAccount.isError && <p className="form-error" role="alert">{errorMessage(cloneAccount.error)}</p>}
            {createTransaction.isError && <p className="form-error" role="alert">{errorMessage(createTransaction.error)}</p>}
            <div className="dialog-actions">
              <button className="button button-quiet" type="button" onClick={() => setTransactionType(undefined)}>Back</button>
              <button className="button button-primary" type="submit" disabled={createTransaction.isPending || cloneAccount.isPending}>
                {createTransaction.isPending || cloneAccount.isPending ? "Saving…" : `Add ${selectedType.title}`}
              </button>
            </div>
          </form>
        )}
      </dialog>
    </>
  );
}
