package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;

import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;

public class Dialogs {
	private static final Logger LOG = LoggerFactory.getLogger(Dialogs.class);

	public interface Callback {
		void success();
		void failed();
	}

	public static abstract class ActionParams {
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
				callback.success();
			}
		}
		protected void failed() {
			if (callback != null) {
				callback.failed();
			}
		}

		public OnClickListener createDialogClickListener() {
			return new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						execute();
						success();
					} catch (Exception ex) {
						LOG.warn("Cannot execute {}", getClass().getSimpleName(), ex);
						failed();
					}
				}
			};
		}
	}

	public static void executeTask(final Activity activity, ActionParams params) {
		new SimpleAsyncTask<ActionParams, Void, ActionParams>() {
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
}
