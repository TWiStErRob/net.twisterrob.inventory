package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.R;

/**
 * <ol>
 * <li>{@link #executeConfirm}
 * <li>{@link ConfirmedExecute}<ol>
 *     <li>doInBackground: prepare</li>
 *     <li>onPostExecute: showDialog</li>
 * </ol>
 * <li>{@link #showDialog}<ol>
 *     <li>AlertDialog.show</li>
 *     <li>setPositiveButton.OnClickListener: executeDirect</li>
 * </ol>
 * <li>{@link #executeDirect}</li>
 * <li>{@link Execute}<ol>
 *     <li>doInBackground: execute</li>
 *     <li>onPostExecute: UndobarController.showUndoBar</li>
 * </ol>
 * </ol>
 */
public class Dialogs {
	private static final Logger LOG = LoggerFactory.getLogger(Dialogs.class);

	public static void executeConfirm(Activity activity, Action action) {
		new ConfirmedExecute(activity).execute(new ActionState(action));
	}

	public static void executeDirect(Activity activity, Action action) {
		new NoQuestionsWithUndo(activity).execute(new ActionState(action));
	}

	static void undo(final Activity activity, ActionState state) {
		final Action undo = state.action.buildUndo();
		if (undo != null) {
			UndobarController.UndoListener undoListener = new UndobarController.UndoListener() {
				@Override public void onUndo(Parcelable token) {
					new NoQuestions(activity).execute(new ActionState(undo));
				}
			};
			String message = state.action.getSuccessMessage(activity.getResources());
			new UndobarController(activity).showUndoBar(false, message, null, undoListener);
		}
	}

	static class ConfirmedExecute extends SimpleAsyncTask<ActionState, Void, ActionState> {
		private final Activity activity;
		ConfirmedExecute(@NonNull Activity activity) {
			this.activity = activity;
		}

		@Override
		protected ActionState doInBackground(ActionState state) {
			state.prepare();
			return state;
		}

		@Override
		protected void onPostExecute(final ActionState state) {
			if (state.check(activity)) {
				new AlertDialog.Builder(activity)
						.setTitle(state.action.getConfirmationTitle(activity.getResources()))
						.setMessage(state.action.getConfirmationMessage(activity.getResources()))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, new OnClickListener() {
							@Override public void onClick(DialogInterface dialog, int which) {
								new Execute(activity).execute(state);
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.show();
			}
		}
	}

	static class Execute extends SimpleAsyncTask<ActionState, Void, ActionState> {
		protected final Activity activity;

		Execute(@NonNull Activity activity) {
			this.activity = activity;
		}

		@Override
		protected ActionState doInBackground(ActionState state) {
			state.execute();
			return state;
		}

		@Override
		protected void onPostExecute(ActionState state) {
			if (state.check(activity)) {
				state.action.finished();
			}
		}
	}

	static class NoQuestions extends SimpleAsyncTask<ActionState, Void, ActionState> {
		protected final Activity activity;

		NoQuestions(@NonNull Activity activity) {
			this.activity = activity;
		}

		@Override
		protected ActionState doInBackground(ActionState state) {
			state.prepare();
			state.execute();
			return state;
		}

		@Override
		protected void onPostExecute(ActionState state) {
			if (state.check(activity)) {
				state.action.finished();
			}
		}
	}

	static class NoQuestionsWithUndo extends NoQuestions {
		NoQuestionsWithUndo(@NonNull Activity activity) {
			super(activity);
		}

		@Override
		protected void onPostExecute(ActionState state) {
			if (state.check(activity)) {
				state.action.finished();
				undo(activity, state);
			}
		}
	}

	static class ActionState {
		final Action action;
		Throwable prepare;
		Throwable execute;
		Throwable failureMessage;

		public ActionState(Action action) {
			this.action = action;
		}

		void prepare() {
			try {
				action.prepare();
			} catch (Exception ex) {
				prepare = ex;
				LOG.warn("Action {} failed to prepare()", action, ex);
			}
		}

		void execute() {
			try {
				action.execute();
			} catch (Exception ex) {
				execute = ex;
				LOG.warn("Action {} failed to execute()", action, ex);
			}
		}

		boolean check(Activity activity) {
			if (prepare == null && execute == null) {
				return true;
			} else {
				String message;
				try {
					message = action.getFailureMessage(activity.getResources());
				} catch (Exception ex) {
					LOG.warn("Failed to get failure message from action {}", action, ex);
					failureMessage = ex;
					message = null;
				}
				if (message == null) { // getFailureMessage returned null or thrown an Exception
					message = getError();
					LOG.warn("No error message from action {}, using one of the exceptions:\n{}", action, message);
					message = activity.getString(R.string.action_error, message);
				}
				Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
				return false;
			}
		}

		private String getError() {
			if (prepare != null) {
				return prepare.getMessage();
			} else if (execute != null) {
				return execute.getMessage();
			} else if (failureMessage != null) {
				return failureMessage.getMessage();
			}
			return "Unknown error";
		}
	}
}
