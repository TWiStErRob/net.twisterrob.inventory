package net.twisterrob.inventory.android.tasks;

import net.twisterrob.inventory.android.view.Action;

public abstract class UndoAction implements Action {
	protected final Action original;

	protected UndoAction(Action original) {
		this.original = original;
	}

	@Override public void prepare() {
		// usually no preparation needed because all the information is available from the original.
	}
	@Override public final String getConfirmationTitle() {
		return "Undo";
	}
	@Override public final String getConfirmationMessage() {
		return "Are you sure you want to undo?";
	}
	@Override public abstract void execute();

	@Override public void finished() {
		if (original != null) {
			original.undoFinished();
		}
	}

	@Override public String getSuccessMessage() {
		return "Un-done";
	}
	@Override public String getFailureMessage() {
		return "Undo failed";
	}
	@Override public final Action buildUndo() {
		return null;
	}
	@Override public final void undoFinished() {
		throw new UnsupportedOperationException("Shouldn't be called since we didn't produce an undo.");
	}
}
