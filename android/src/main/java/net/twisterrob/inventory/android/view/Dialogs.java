package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.fragment.BaseDialogFragment;

/**
 * <ol>
 * <li>{@link #executeConfirm}
 * <li>{@link ConfirmedExecute}<ol>
 *     <li>doInBackground: prepare</li>
 *     <li>onPostExecute: {@link Builder#show}</li>
 * </ol>
 * <li>{@link ConfirmedExecute#onPostExecute}<ol>
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
// FIXME make these dialogs rotation-proof, the actions need to have another way of communicating their results
// See BaseFragment.refresh() as well.
public class Dialogs {
	private static final Logger LOG = LoggerFactory.getLogger(Dialogs.class);

	public static void executeConfirm(@NonNull FragmentActivity activity, @NonNull Action action) {
		new ConfirmedExecute(activity).execute(new ActionState(action));
	}

	public static void executeDirect(@NonNull FragmentActivity activity, @NonNull Action action) {
		new NoQuestionsWithUndo(activity).execute(new ActionState(action));
	}

	static void undo(final @NonNull FragmentActivity activity, @NonNull ActionState state) {
		final Action undo = state.action.buildUndo();
		if (undo != null) {
			CharSequence message = state.action.getSuccessMessage(activity.getResources());
			Snackbar
					.make(activity.getWindow().getDecorView().getRootView(), message, 5000)
					.setAction(R.string.action_undo, new View.OnClickListener() {
						@Override public void onClick(View v) {
							new NoQuestions(activity).execute(new ActionState(undo));
						}
					})
					.show()
			;
		}
	}

	static class ConfirmedExecute extends ProgressAsyncTask {
		ConfirmedExecute(@NonNull FragmentActivity context) {
			super(context);
		}

		@Override protected ActionState doInBackground(ActionState state) {
			state.prepare();
			return state;
		}

		@TargetApi(VERSION_CODES.HONEYCOMB)
		@Override protected void onPostExecute(final ActionState state) {
			super.onPostExecute(state);
			if (state.check(context)) {
				new AlertDialog.Builder(context)
						.setTitle(state.action.getConfirmationTitle(context.getResources()))
						.setMessage(state.action.getConfirmationMessage(context.getResources()))
						.setView(state.action.getConfirmationView(context))
						.setPositiveButton(android.R.string.yes, new OnClickListener() {
							@Override public void onClick(DialogInterface dialog, int which) {
								new Execute(context).execute(state);
							}
						})
						.setNegativeButton(android.R.string.no, null)
						.setIconAttribute(android.R.attr.alertDialogIcon)
						.show();
			}
		}
	}

	private static abstract class ProgressAsyncTask extends SimpleAsyncTask<ActionState, Void, ActionState> {
		protected final FragmentActivity context;
		private final BaseDialogFragment progress;

		protected ProgressAsyncTask(@NonNull FragmentActivity context) {
			this.context = context;
			this.progress = new ActionProgressFragment();
		}

		@CallSuper
		@Override protected void onPreExecute() {
			super.onPreExecute();
			progress.show(context.getSupportFragmentManager(), "dialog-action-progress");
		}

		@CallSuper
		@Override protected void onPostExecute(ActionState state) {
			super.onPostExecute(state);
			progress.dismiss();
		}

		public static class ActionProgressFragment extends BaseDialogFragment {
			@Override public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setCancelable(false);
			}
			@SuppressWarnings("deprecation") // blocking the user's view intentionally
			@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
				ProgressDialog dialog = new ProgressDialog(requireContext());
				dialog.setMessage("Please wait...");
				dialog.setIndeterminate(true);
				return dialog;
			}
		}
	}

	static class Execute extends ProgressAsyncTask {
		protected Execute(@NonNull FragmentActivity context) {
			super(context);
		}
		@Override protected ActionState doInBackground(ActionState state) {
			state.execute();
			return state;
		}

		@Override protected void onPostExecute(ActionState state) {
			super.onPostExecute(state);
			if (state.check(context)) {
				state.action.finished();
			}
		}
	}

	static class NoQuestions extends ProgressAsyncTask {
		protected NoQuestions(@NonNull FragmentActivity context) {
			super(context);
		}
		@Override protected ActionState doInBackground(ActionState state) {
			state.prepare();
			if (state.hasPassed()) {
				state.execute();
			}
			return state;
		}

		@Override protected void onPostExecute(ActionState state) {
			super.onPostExecute(state);
			if (state.check(context)) {
				state.action.finished();
			}
		}
	}

	static class NoQuestionsWithUndo extends NoQuestions {
		NoQuestionsWithUndo(@NonNull FragmentActivity activity) {
			super(activity);
		}

		@Override protected void onPostExecute(ActionState state) {
			super.onPostExecute(state);
			if (state.hasPassed()) {
				undo(context, state);
			}
		}
	}

	private static class ActionState {
		public final Action action;
		private Throwable prepare;
		private Throwable execute;
		private Throwable failureMessage;

		public ActionState(@NonNull Action action) {
			this.action = action;
		}

		@WorkerThread
		public void prepare() {
			try {
				action.prepare();
			} catch (Exception ex) {
				prepare = ex;
				LOG.warn("Action {} failed to prepare()", action, ex);
			}
		}

		@WorkerThread
		public void execute() {
			try {
				action.execute();
			} catch (Exception ex) {
				execute = ex;
				LOG.warn("Action {} failed to execute()", action, ex);
			}
		}

		@UiThread
		public boolean check(@NonNull Context context) {
			if (hasPassed()) {
				return true;
			}
			CharSequence message;
			try {
				message = action.getFailureMessage(context.getResources());
				if (prepare != null || execute != null) {
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
			App.toastUser(message);
			return false;
		}

		public boolean hasPassed() {
			return prepare == null && execute == null;
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
