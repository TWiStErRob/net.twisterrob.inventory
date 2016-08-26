package net.twisterrob.inventory.android.activity;

import java.io.File;
import java.util.Calendar;

import org.slf4j.*;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.*;
import android.view.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks.DoNothing;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.activity.space.ManageSpaceActivity;
import net.twisterrob.inventory.android.backup.*;
import net.twisterrob.inventory.android.backup.concurrent.*;
import net.twisterrob.inventory.android.content.InventoryContract;
import net.twisterrob.inventory.android.fragment.BackupListFragment;

public class BackupActivity extends BaseActivity implements BackupListFragment.BackupListCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(BackupActivity.class);
	private static final int REQUEST_CODE_PICK_EXTERNAL = 0x4412;
	private BackupListFragment fileList;
	private boolean allowNew;
	@VisibleForTesting final BackupServiceConnection backupService = new BackupServiceConnection() {
		@Override protected void serviceBound(ComponentName name, BackupService.LocalBinder service) {
			setAllowNew(!service.isInProgress());
		}
		@Override protected void serviceUnbound(ComponentName name, BackupService.LocalBinder service) {
			setAllowNew(true);
		}
		@Override public void started() {
			setAllowNew(false);
		}
		@Override public void finished() {
			setAllowNew(true);
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
		handleIntent();
		fileList = getFragment(R.id.backup_list);
	}
	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent();
	}
	private void handleIntent() {
		Progress progress = (Progress)getIntent().getSerializableExtra(BackupService.EXTRA_PROGRESS);
		if (progress != null && progress.phase == Progress.Phase.Finished) {
			new ProgressDisplayer(this, progress).displayFinishMessage(new PopupCallbacks<Void>() {
				@Override public void finished(Void value) {
					getIntent().removeExtra(BackupService.EXTRA_PROGRESS);
				}
			});
		}
	}

	@Override protected void onStart() {
		super.onStart();
		backupService.bind(this);
	}
	@Override protected void onStop() {
		super.onStop();
		backupService.unbind();
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
		if (!allowNew) {
			throw new IllegalStateException(getString(R.string.backup_warning_inprogress));
		}
		Intent intent = new Intent(BackupService.ACTION_IMPORT, source, getApplicationContext(), BackupService.class);
		startService(intent);
	}

	private void doExportExternal() {
		if (!allowNew) {
			throw new IllegalStateException(getString(R.string.backup_warning_inprogress));
		}
		Calendar now = Calendar.getInstance();
		Intent intent = new Intent(Intent.ACTION_SEND)
				.setType(InventoryContract.Export.TYPE_BACKUP)
				.putExtra(Intent.EXTRA_STREAM, InventoryContract.Export.getUri(now))
				.putExtra(Intent.EXTRA_SUBJECT, Paths.getExportFileName(now))
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		// this makes the user choose a target and that target will call the provider later, which starts the service
		startActivity(Intent.createChooser(intent, getString(R.string.backup_export_external)));
	}

	@Override public void newIntoDir(@NonNull File targetDir) {
		if (!allowNew) {
			throw new IllegalStateException(getString(R.string.backup_warning_inprogress));
		}
		Uri dir = Uri.fromFile(targetDir);
		Intent intent = new Intent(BackupService.ACTION_EXPORT_DIR, dir, getApplicationContext(), BackupService.class);
		startService(intent);
	}

	public static Intent chooser() {
		return new Intent(App.getAppContext(), BackupActivity.class);
	}
}
