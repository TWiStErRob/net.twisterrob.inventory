package net.twisterrob.inventory.android.view;

import java.lang.annotation.*;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.*;

public interface Action {
	/**
	 * Load the affected entities into memory for use on the UI.
	 *
	 * @throws RuntimeException If any of the arguments are incorrect or load fails.
	 */
	@WorkerThread
	void prepare() throws RuntimeException;
	@UiThread
	@NonNull CharSequence getConfirmationTitle(@NonNull Resources res);
	@UiThread
	@NonNull CharSequence getConfirmationMessage(@NonNull Resources res);
	/**
	 * Custom view for confirmation dialog. Implementers must hold on to it to be able to read from it in {@link #execute}.
	 */
	@UiThread
	@Nullable View getConfirmationView(@NonNull Context context);
	/**
	 * Send all the requested actions the user has confirmed to the database.
	 * FIXME Should be ACID, that is either all operations fail or all succeed.
	 * @throws RuntimeException If the operation cannot be completed.
	 */
	@WorkerThread
	void execute();
	/**
	 * Notification after {@link #execute} is done.
	 */
	@UiThread
	void finished();
	/** Will be displayed in a Toast or Undobar. */
	@UiThread
	@NonNull CharSequence getSuccessMessage(@NonNull Resources res);
	/** Will be displayed in a Toast or AlertDialog. {@link #prepare()} might not have been called or failed. */
	@UiThread
	@NonNull CharSequence getFailureMessage(@NonNull Resources res);
	/**
	 * Build an opposite of this action implying that all the data has been modified by {@link #execute} already.
	 *
	 * @return action that can reset the database to the original state or {@code null} if no undo available
	 */
	@UiThread
	@Nullable Action buildUndo();
	/**
	 * Notification after {@link #buildUndo()}.{@link #execute()} is done.
	 * Not called on error or if {@link #buildUndo()} returned {@code null}.
	 */
	@UiThread
	void undoFinished();

	/**
	 * Tagging annotation to mark fields that are populated during action construction.
	 * They are always be available during the entire lifecycle of the action.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD}) @interface Input {
	}

	/**
	 * Tagging annotation to mark fields that are populated during action {@link #prepare()}.
	 * They are be available during {@link #execute()}.
	 * They might not be available in {@link #getFailureMessage(Resources)} if {@link #prepare()} failed.
	 */
	@Documented
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD}) @interface Prepared {
	}
}
