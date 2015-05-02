package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.Toast;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.*;

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

	public static void executeConfirm(Context activity, Action action) {
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
			CharSequence message = state.action.getSuccessMessage(activity.getResources());
			new UndobarController(activity).showUndoBar(false, message, null, undoListener);
		}
	}

	static class ConfirmedExecute extends SimpleAsyncTask<ActionState, Void, ActionState> {
		private final Context context;
		ConfirmedExecute(@NonNull Context context) {
			this.context = context;
		}

		@Override
		protected ActionState doInBackground(ActionState state) {
			state.prepare();
			return state;
		}

		@Override
		protected void onPostExecute(final ActionState state) {
			if (state.check(context)) {
				new AlertDialog.Builder(context)
						.setTitle(state.action.getConfirmationTitle(context.getResources()))
						.setMessage(state.action.getConfirmationMessage(context.getResources()))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setView(state.action.getConfirmationView(context))
						.setPositiveButton(android.R.string.yes, new OnClickListener() {
							@Override public void onClick(DialogInterface dialog, int which) {
								new Execute(context).execute(state);
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.show();
			}
		}
	}

	static class Execute extends SimpleAsyncTask<ActionState, Void, ActionState> {
		protected final Context context;

		Execute(@NonNull Context context) {
			this.context = context;
		}

		@Override
		protected ActionState doInBackground(ActionState state) {
			state.execute();
			return state;
		}

		@Override
		protected void onPostExecute(ActionState state) {
			if (state.check(context)) {
				state.action.finished();
			}
		}
	}

	static class NoQuestions extends SimpleAsyncTask<ActionState, Void, ActionState> {
		protected final Context context;

		NoQuestions(@NonNull Context context) {
			this.context = context;
		}

		@Override
		protected ActionState doInBackground(ActionState state) {
			state.prepare();
			if (!state.hasErrors()) {
				state.execute();
			}
			return state;
		}

		@Override
		protected void onPostExecute(ActionState state) {
			if (state.check(context)) {
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
			if (state.check(context)) {
				state.action.finished();
				undo((Activity)context, state);
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

		boolean check(Context context) {
			if (!hasErrors()) {
				return true;
			} else {
				CharSequence message;
				try {
					message = action.getFailureMessage(context.getResources());
					if (message != null && (prepare != null || execute != null)) {
						message = App.getError(prepare != null? prepare : execute, message);
					}
				} catch (Exception ex) {
					LOG.warn("Failed to get failure message from action {}", action, ex);
					failureMessage = ex;
					message = null;
				}
				if (message == null) { // getFailureMessage returned null or thrown an Exception
					message = getError();
					LOG.warn("No error message from action {}, using one of the exceptions:\n{}", action, message);
					message = context.getString(R.string.action_error, message);
				}
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
				return false;
			}
		}
		private boolean hasErrors() {
			return prepare != null || execute != null;
		}

		private CharSequence getError() {
			if (prepare != null) {
				return App.getError(prepare, "Failed to prepare action.");
			} else if (execute != null) {
				return App.getError(execute, "Failed to execute action.");
			} else if (failureMessage != null) {
				return App.getError(failureMessage, "Failed to check action.");
			}
			return "Unknown error";
		}
	}
}
