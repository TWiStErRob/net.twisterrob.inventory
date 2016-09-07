package net.twisterrob.inventory.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.View;

import net.twisterrob.inventory.android.view.Action;

public abstract class UndoAction implements Action {
	protected final Action original;

	protected UndoAction(Action original) {
		this.original = original;
	}

	@Override public void prepare() {
		// usually no preparation needed because all the information is available from the original.
	}
	@Override public final @NonNull CharSequence getConfirmationTitle(@NonNull Resources res) {
		return "Undo";
	}
	@Override public final @NonNull CharSequence getConfirmationMessage(@NonNull Resources res) {
		return "Are you sure you want to undo?";
	}
	@Override public View getConfirmationView(@NonNull Context context) {
		return null;
	}

	@Override public abstract void execute();

	@Override public void finished() {
		if (original != null) {
			original.undoFinished();
		}
	}

	@Override public @NonNull CharSequence getSuccessMessage(@NonNull Resources res) {
		return "Un-done";
	}
	@Override public @NonNull CharSequence getFailureMessage(@NonNull Resources res) {
		return "Undo failed";
	}
	@Override public final Action buildUndo() {
		return null;
	}
	@Override public final void undoFinished() {
		throw new UnsupportedOperationException("Shouldn't be called since we didn't produce an undo.");
	}
}
