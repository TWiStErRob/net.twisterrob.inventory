package net.twisterrob.inventory.android.view;

import android.content.res.Resources;

public interface Action {
	/**
	 * Load the affected entities into memory for use on the UI.
	 *
	 * <i>Called in background.</i>
	 * @throws RuntimeException If any of the arguments are incorrect or load fails.
	 */
	void prepare();
	String getConfirmationTitle(Resources res);
	String getConfirmationMessage(Resources res);
	/**
	 * Send all the requested actions the user has confirmed to the database.
	 * TODO Should be ACID, that is either all operations fail or all succeed.
	 *
	 * <i>Called in background.</i>
	 * @throws RuntimeException If the operation cannot be completed.
	 */
	void execute();
	/**
	 * Notification after {@link #execute} is done.
	 *
	 * <i>Called on UI thread.</i>
	 */
	void finished();
	/** Will be displayed in a Toast or Undobar */
	String getSuccessMessage(Resources res);
	/** Will be displayed in a Toast or AlertDialog */
	String getFailureMessage(Resources res);
	/**
	 * Build an opposite of this action implying that all the data has been modified by {@link #execute} already.
	 *
	 * <i>Called on UI thread.</i>
	 * @return action that can reset the database to the original state or {@code null} if no undo available
	 */
	Action buildUndo();
	/**
	 * Notification after {@link #buildUndo()}.{@link #execute()} is done.
	 * Not called on error or if {@link #buildUndo()} returned {@code null}.
	 *
	 * <i>Called on UI thread.</i>
	 */
	void undoFinished();
}
