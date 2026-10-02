import type { Transaction } from "../../../shared/api/contracts";
import { errorMessage } from "../../../shared/api/api-error";
import { useDeleteTransaction } from "../queries";

interface DeleteTransactionButtonProps {
  financialContextId: string;
  transaction: Transaction;
}

export function DeleteTransactionButton({ financialContextId, transaction }: DeleteTransactionButtonProps) {
  const remove = useDeleteTransaction(financialContextId);

  async function deleteTransaction() {
    if (!window.confirm("Delete this transaction? It will be hidden but retained in history.")) return;
    await remove.mutateAsync(transaction);
  }

  return (
    <span>
      <button className="button button-quiet" type="button" onClick={deleteTransaction} disabled={remove.isPending}>
        {remove.isPending ? "Deleting…" : "Delete"}
      </button>
      {remove.isError && <p className="form-error" role="alert">{errorMessage(remove.error)}</p>}
    </span>
  );
}
