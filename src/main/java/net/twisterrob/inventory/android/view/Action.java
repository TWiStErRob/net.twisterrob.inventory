package net.twisterrob.inventory.android.view;

public interface Action {
	/**
	 * Load the affected entities into memory for use on the UI.
	 *
	 * <i>Called in background.</i>
	 */
	void prepare();
	String getConfirmationTitle();
	String getConfirmationMessage();
	/**
	 * Send all the requested actions the user has confirmed to the database.
	 *
	 * <i>Called in background.</i>
	 */
	void execute();
	/**
	 * Notification after {@link #execute} is done.
	 *
	 * <i>Called on UI thread.</i>
	 */
	void finished();
	/** Will be displayed in a Toast or Undobar */
	String getSuccessMessage();
	/** Will be displayed in a Toast or AlertDialog */
	String getFailureMessage();
	/**
	 * Build an opposite of this action implying that all the data has been modified by {@link #execute} already.
	 *
	 * <i>Called on UI thread.</i>
	 * @return action that can reset the database to the original state
	 */
	Action buildUndo();
	void undoFinished();
}
