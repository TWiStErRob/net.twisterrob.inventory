package net.twisterrob.inventory.android.tasks;

import android.content.Context;
import android.content.res.Resources;
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
	@Override public final CharSequence getConfirmationTitle(Resources res) {
		return "Undo";
	}
	@Override public final CharSequence getConfirmationMessage(Resources res) {
		return "Are you sure you want to undo?";
	}
	@Override public View getConfirmationView(Context context) {
		return null;
	}

	@Override public abstract void execute();

	@Override public void finished() {
		if (original != null) {
			original.undoFinished();
		}
	}

	@Override public CharSequence getSuccessMessage(Resources res) {
		return "Un-done";
	}
	@Override public CharSequence getFailureMessage(Resources res) {
		return "Undo failed";
	}
	@Override public final Action buildUndo() {
		return null;
	}
	@Override public final void undoFinished() {
		throw new UnsupportedOperationException("Shouldn't be called since we didn't produce an undo.");
	}
}
