package net.twisterrob.inventory.android.activity;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.CancellationException;

import org.slf4j.*;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks.DoNothing;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.space.ManageSpaceActivity;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.inventory.android.backup.xml.ZippedXMLExporter;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.fragment.BackupListFragment;

import static net.twisterrob.android.utils.tools.AndroidTools.*;
import static net.twisterrob.inventory.android.backup.concurrent.NotificationProgressService.*;

public class BackupActivity extends BaseActivity implements BackupListFragment.BackupListCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivity.class);
	private static final int REQUEST_CODE_PICK_EXTERNAL = 0x4412;
	private BackupListFragment fileList;
	private boolean allowNew;
	/** Prevents {@code android.view.WindowLeaked} "Activity BackupActivity has leaked window". See usages. */
	private AlertDialog finishDialog; // CONSIDER DialogFragment to auto-dismiss? and handle rotation
	private Progress unhandled;

	@VisibleForTesting final BackupServiceConnection backupService = new BackupServiceConnection() {
		@Override protected void serviceBound(ComponentName name, BackupService.LocalBinder service) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_FINISHED_BROADCAST);
			LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, filter);

			setAllowNew(!service.isInProgress());
		}
		@Override protected void serviceUnbound(ComponentName name, BackupService.LocalBinder service) {
			setAllowNew(true);

			LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
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
			Progress progress = (Progress)intent.getSerializableExtra(BackupService.EXTRA_PROGRESS);
			displayPopup(progress);
		}
	};

	private void setAllowNew(boolean allowNew) {
		this.allowNew = allowNew;
		fileList.onRefresh();
		supportInvalidateOptionsMenu();
	}
	@Override public boolean isAllowNew() {
		return allowNew;
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
		fileList = getFragment(R.id.backup_list);

		displayPopup(findProgress(savedInstanceState, getIntent()));
	}
	private @Nullable Progress findProgress(@Nullable Bundle savedInstanceState, @Nullable Intent intent) {
		if (savedInstanceState != null) {
			return (Progress)savedInstanceState.getSerializable(BackupService.EXTRA_PROGRESS);
		} else if (intent != null) {
			return (Progress)intent.getSerializableExtra(BackupService.EXTRA_PROGRESS);
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
		unhandled = progress;
		finishDialog = new ProgressDisplayer(this, progress).displayFinishMessage(new PopupCallbacks<Void>() {
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
		// this should ignore any incoming finish progress while the dialog is up
		// and hopefully that only happens when Google Drive reads the second time
		// lack of initiating component (see doExportExternal), try the best heuristic to filter Drive peeking for size:
		if (unhandled != null) {
			// while another finish is already displayed (because the BackupService is sequential)
			LOG.warn("Double-finish\n{}\n{}", unhandled, progress);
			boolean cancelled = false, pipe = false;
			if (progress.failure instanceof CancellationException) {
				cancelled = true;
				Throwable cause = progress.failure.getCause();
				if (IOTools.isEPIPE(cause)) {
					pipe = true;
					// EPIPE error (this just means that external party closed the PIPE)
					for (StackTraceElement st : cause.getStackTrace()) {
						// stack trace failed in ZippedXMLExporter.copyXSLT
						if (ZippedXMLExporter.class.getName().equals(st.getClassName())
								&& "copyXSLT".equals(st.getMethodName())) {
							// very likely the external app closed the stream
							// or the user has 100kb free space, but then... good for them
							LOG.warn("Ignoring Google Drive's weirdness of peeking for size.", progress.failure);
							return false;
						}
					}
				}
			}
			LOG.warn("Letting double-dialog display: cancelled={}, pipe={}, stack={}", cancelled, pipe, false);
			// anything that doesn't match the above: it's better to display double dialog
		}
		return true;
	}

	@Override protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BackupService.EXTRA_PROGRESS, unhandled);
	}
	@Override protected void onResume() {
		super.onResume();
		backupService.bind(this);
	}
	@Override protected void onPause() {
		super.onPause();
		backupService.unbind();
	}
	@Override protected void onStop() {
		super.onStop();
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
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu)) {
			return false;
		}
		getMenuInflater().inflate(R.menu.backup, menu);
		return true;
	}
	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		AndroidTools.enabledIf(menu, R.id.action_import_external, allowNew);
		AndroidTools.enabledIf(menu, R.id.action_export_internal, allowNew);
		AndroidTools.enabledIf(menu, R.id.action_export_external, allowNew);
		return super.onPrepareOptionsMenu(menu);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_export_home:
				fileList.filePicked(Paths.getPhoneHome(), true);
				return true;
			case R.id.action_import_external:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				intent = Intent.createChooser(intent, getString(R.string.backup_import_external));
				startActivityForResult(intent, REQUEST_CODE_PICK_EXTERNAL);
				return true;
			case R.id.action_export_internal:
				newIntoDir(fileList.getDir());
				return true;
			case R.id.action_export_external:
				DialogTools
						.confirm(this, new PopupCallbacks<Boolean>() {
							@Override public void finished(Boolean value) {
								if (Boolean.TRUE.equals(value)) {
									doExportExternal();
								}
							}
						})
						.setTitle(R.string.backup_export_external_confirm_title)
						.setMessage(R.string.backup_export_external_confirm_warning)
						.show();
				return true;
			case R.id.action_manage_space:
				startActivity(ManageSpaceActivity.launch());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CODE_PICK_EXTERNAL: {
				if (resultCode == RESULT_OK && data != null && data.getData() != null) {
					Uri uri = data.getData();
					doImport(uri);
				}
				break;
			}
		}
	}

	public void filePicked(@NonNull final File file) {
		//ensureNotInProgress(); // for other operations the UI is disabled, but the user can still tap in the file list
		if (!allowNew) {
			DialogTools
					.notify(this, DoNothing.<Boolean>instance())
					.setTitle(R.string.backup_import_confirm_title)
					.setMessage(R.string.backup_warning_inprogress)
					.show();
			return;
		}
		DialogTools
				.confirm(this, new PopupCallbacks<Boolean>() {
					@Override public void finished(Boolean value) {
						if (Boolean.TRUE.equals(value)) {
							doImport(Uri.fromFile(file));
						}
					}
				})
				.setTitle(R.string.backup_import_confirm_title)
				.setMessage(getString(R.string.backup_import_confirm_warning, file.getName()))
				.show();
	}

	private void doImport(Uri source) {
		ensureNotInProgress();
		Intent intent = new Intent(BackupService.ACTION_IMPORT, source, getApplicationContext(), BackupService.class);
		startService(intent);
	}

	private void doExportExternal() {
		ensureNotInProgress();
		Calendar now = Calendar.getInstance();
		Intent intent = new Intent(Intent.ACTION_SEND)
				.setType(InventoryContract.Export.TYPE_BACKUP)
				.putExtra(Intent.EXTRA_STREAM, InventoryContract.Export.getUri(now))
				.putExtra(Intent.EXTRA_SUBJECT, Paths.getExportFileName(now))
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// this makes the user choose a target and that target will call the provider later, which starts the service
		startActivity(Intent.createChooser(intent, getString(R.string.backup_export_external)));
		// alternatives would be:
		// ACTION_PICK_ACTIVITY which calls onActivityResult, but it looks ugly
		// createChooser(..., PendingIntent.getBroadcast.getIntentSender), but it's API 22 and only notifies after started
	}

	@Override public void newIntoDir(@NonNull File targetDir) {
		ensureNotInProgress();
		Uri dir = Uri.fromFile(targetDir);
		Intent intent = new Intent(BackupService.ACTION_EXPORT_DIR, dir, getApplicationContext(), BackupService.class);
		startService(intent);
	}

	private void ensureNotInProgress() {
		if (!allowNew) {
			throw new IllegalStateException(getString(R.string.backup_warning_inprogress));
		}
	}

	public static Intent chooser() {
		return new Intent(App.getAppContext(), BackupActivity.class);
	}
}
