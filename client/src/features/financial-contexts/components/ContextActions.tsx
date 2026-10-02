import { type FormEvent, useState } from "react";
import { errorMessage } from "../../../shared/api/api-error";
import { useDeleteFinancialContext, useUpdateFinancialContext } from "../queries";

interface ContextActionsProps {
  financialContextId: string;
  name: string;
  onDeleted: () => void;
}

export function ContextActions({ financialContextId, name, onDeleted }: ContextActionsProps) {
  const update = useUpdateFinancialContext(financialContextId);
  const remove = useDeleteFinancialContext();
  const [editing, setEditing] = useState(false);
  const [nextName, setNextName] = useState(name);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!nextName.trim()) return;
    await update.mutateAsync(nextName.trim());
    setEditing(false);
  }

  async function deleteContext() {
    if (!window.confirm(`Delete “${name}”? Its data will be hidden, not permanently removed.`)) return;
    await remove.mutateAsync(financialContextId);
    onDeleted();
  }

  return (
    <div className="hero-actions">
      {editing ? (
        <form className="inline-form" onSubmit={submit}>
          <label className="sr-only" htmlFor="context-rename">Context name</label>
          <input id="context-rename" value={nextName} onChange={(event) => setNextName(event.target.value)} />
          <button className="button button-quiet" type="submit" disabled={update.isPending}>Save</button>
          <button className="button button-quiet" type="button" onClick={() => { setNextName(name); setEditing(false); }}>Cancel</button>
        </form>
      ) : (
        <button className="button button-quiet" type="button" onClick={() => setEditing(true)}>Edit name</button>
      )}
      <button className="button button-quiet" type="button" onClick={deleteContext} disabled={remove.isPending}>
        {remove.isPending ? "Deleting…" : "Delete context"}
      </button>
      {update.isError && <p className="form-error hero-error" role="alert">{errorMessage(update.error)}</p>}
      {remove.isError && <p className="form-error hero-error" role="alert">{errorMessage(remove.error)}</p>}
    </div>
  );
}
