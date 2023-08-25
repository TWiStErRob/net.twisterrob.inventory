package net.twisterrob.inventory.android.activity;

import java.util.concurrent.CancellationException;

import org.slf4j.*;

import android.content.*;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter;
import net.twisterrob.inventory.android.fragment.BackupControllerFragment;

import static net.twisterrob.inventory.android.content.BroadcastTools.getLocalBroadcastManager;

@AndroidEntryPoint
public class BackupActivity extends BaseActivity implements BackupControllerFragment.BackupEvents {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivity.class);
	private boolean allowNew;
	/** Prevents {@code android.view.WindowLeaked} "Activity BackupActivity has leaked window". See usages. */
	private AlertDialog finishDialog; // CONSIDER DialogFragment to auto-dismiss? and handle rotation
	private Progress unhandled;
	private BackupControllerFragment controller;

	@VisibleForTesting final BackupServiceConnection backupService = new BackupServiceConnection() {
		@Override protected void serviceBound(ComponentName name, BackupService.LocalBinder service) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(NotificationProgressService.ACTION_FINISHED_BROADCAST);
			getLocalBroadcastManager(getApplicationContext()).registerReceiver(receiver, filter);

			setAllowNew(!service.isInProgress());
		}
		@Override protected void serviceUnbound(ComponentName name, BackupService.LocalBinder service) {
			setAllowNew(true);

			getLocalBroadcastManager(getApplicationContext()).unregisterReceiver(receiver);
		}
		@Override public void started() {
			setAllowNew(false);
		}
		@Override public void finished() {
			setAllowNew(true);
		}
	};
	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override public void onReceive(Context context, Intent intent) {
			Progress progress = IntentTools.getSerializableExtra(intent, BackupService.EXTRA_PROGRESS, Progress.class);
			displayPopup(progress);
		}
	};

	private void setAllowNew(boolean allowNew) {
		this.allowNew = allowNew;
		controller.onAllowNewChanged(allowNew);
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		controller = getFragment(R.id.backup_controller);
		if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
			addMenuProvider(new BackupNotificationsMenu(this));
		}

		displayPopup(findProgress(savedInstanceState, getIntent()));
	}

	@Override protected void onResume() {
		super.onResume();
		if (Build.VERSION_CODES.TIRAMISU <= Build.VERSION.SDK_INT) {
			invalidateOptionsMenu();
		}
	}

	private @Nullable Progress findProgress(@Nullable Bundle savedInstanceState, @Nullable Intent intent) {
		if (savedInstanceState != null) {
			return BundleTools.getSerializable(savedInstanceState, BackupService.EXTRA_PROGRESS, Progress.class);
		} else if (intent != null) {
			return IntentTools.getSerializableExtra(intent, BackupService.EXTRA_PROGRESS, Progress.class);
		} else {
			return null;
		}
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		displayPopup(findProgress(null, getIntent()));
	}
	private void displayPopup(@Nullable Progress progress) {
		if (!shouldDisplay(progress)) {
			return;
		}
		LOG.trace("Displaying popup for {}", progress);
		unhandled = progress;
		finishDialog = new StrictProgressInfoProvider(this, progress).displayFinishMessage(new PopupCallbacks<Void>() {
			@Override public void finished(Void value) {
				unhandled = null;
				finishDialog = null;
			}
		});
	}

	private boolean shouldDisplay(@Nullable Progress progress) {
		if (progress == null) {
			return false;
		}

		// If another finish is not yet displayed, display this progress immediately.
		// This is ok, because the BackupService is sequential.
		if (unhandled == null) {
			return true;
		}

		// The following should ignore any incoming finish progress while the dialog is up.
		// Hopefully that only happens when Google Drive reads the second time.
		// Due to a lack of initiating component (see BackupControllerFragment.doExport),
		// try the best heuristic to filter Drive peeking for size.
		LOG.warn("Double-finish\n{}\n{}", unhandled, progress);
		boolean cancelled = false, pipe = false;
		if (progress.failure instanceof CancellationException) {
			cancelled = true;
			Throwable cause = progress.failure.getCause();
			if (IOTools.isEPIPE(cause)) {
				pipe = true;
				// EPIPE error (this just means that external party closed the PIPE)
				for (StackTraceElement st : cause.getStackTrace()) {
					// Stack trace failed in ZippedXMLExporter.copyEntry
					if (ZippedXMLExporter.class.getName().equals(st.getClassName())
							&& "copyEntry".equals(st.getMethodName())) {
						// very likely the external app closed the stream
						// or the user has 100kb free space, but then... good for them
						LOG.warn("Ignoring Google Drive's weirdness of peeking for size.", progress.failure);
						return false;
					}
				}
			}
		}

		// Anything that doesn't match the above: it's better to display double dialog.
		LOG.warn("Letting double-dialog display: cancelled={}, pipe={}, stack={}", cancelled, pipe, false);
		return true;
	}

	@Override protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BackupService.EXTRA_PROGRESS, unhandled);
	}
	@Override protected void onStart() {
		super.onStart();
		backupService.bind(this);
	}
	@Override protected void onStop() {
		super.onStop();
		backupService.unbind();
		if (finishDialog != null) {
			finishDialog.dismiss();
		}
	}
	@Override protected void onRestart() {
		super.onRestart();
		if (finishDialog != null) {
			finishDialog.show();
		}
	}

	@Override public void ensureNotInProgress() {
		if (!allowNew) {
			throw new IllegalStateException(getString(R.string.backup_warning_inprogress));
		}
	}

	public static Intent chooser(Context context) {
		return new Intent(context, BackupActivity.class);
	}
}
