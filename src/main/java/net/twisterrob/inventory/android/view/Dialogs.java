package net.twisterrob.inventory.android.view;

import org.slf4j.*;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.*;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;

public class Dialogs {
	private static final Logger LOG = LoggerFactory.getLogger(Dialogs.class);

	public interface Callback {
		void dialogSuccess();
		void dialogFailed();
	}

	public static abstract class ActionParams implements Runnable {
		private final Callback callback;
		public ActionParams(Callback callback) {
			this.callback = callback;
		}

		protected abstract void prepare();

		protected abstract String getTitle();
		protected abstract String getMessage();
		protected abstract void execute() throws Exception;

		protected void success() {
			if (callback != null) {
				callback.dialogSuccess();
			}
		}
		protected void failed() {
			if (callback != null) {
				callback.dialogFailed();
			}
		}

		@Override public void run() {
			try {
				execute();
				success();
			} catch (Exception ex) {
				LOG.warn("Cannot execute {}", getClass().getSimpleName(), ex);
				failed();
			}
		}

		public OnClickListener createDialogClickListener() {
			return new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					run();
				}
			};
		}

		public void displayDialog(Activity activity) {
			Dialogs.executeTask(activity, this);
		}

		public void executeBackground() {
			Dialogs.executeTask(this);
		}
	}

	public static void executeTask(final Activity activity, ActionParams params) {
		new SimplerAsyncTask<ActionParams, Void, ActionParams>() {
			@Override
			protected ActionParams doInBackground(ActionParams state) {
				state.prepare();
				return state;
			}

			@Override
			protected void onPostExecute(ActionParams state) {
				new AlertDialog.Builder(activity) //
						.setTitle(state.getTitle()) //
						.setMessage(state.getMessage()) //
						.setIcon(android.R.drawable.ic_dialog_alert) //
						.setPositiveButton(android.R.string.yes, state.createDialogClickListener()) //
						.setNegativeButton(android.R.string.no, null) //
						.show();
			}
		}.execute(params);
	}

	private static void executeTask(ActionParams params) {
		new SimplerAsyncTask<ActionParams, Void, Void>() {
			@Override
			protected Void doInBackground(ActionParams state) {
				state.prepare();
				new Handler(Looper.getMainLooper()).post(state);
				return null;
			}
		}.execute(params);
	}

	// TODO move to lib
	static abstract class SimplerAsyncTask<A, B, C> extends SimpleAsyncTask<A, B, C> {
		public void execute(A a) {
			super.execute(a);
		}
	}
}
