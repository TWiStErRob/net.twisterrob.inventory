package net.twisterrob.inventory.android.fragment;

import java.io.*;

import org.slf4j.*;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.*;
import android.view.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.io.ExporterTask;
import net.twisterrob.inventory.android.content.io.csv.DatabaseCSVImporter;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;
import net.twisterrob.inventory.android.view.SafeSimpleAsyncTask;

public class BackupFragment extends DialogFragment implements BackupPickerListener {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterTask.class);

	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.backup, menu);
		inflater.inflate(R.menu.backup_items, menu.findItem(R.id.backup).getSubMenu());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return handle(item.getItemId()) || super.onOptionsItemSelected(item);
	}

	private boolean handle(int ID) {
		switch (ID) {
			case R.id.exportDrive:
				startActivity(ExportActivity.chooser());
				return true;
			case R.id.importDrive:
				startActivity(ImportActivity.chooser());
				return true;
			case R.id.exportSDCard:
				FragmentActivity ac = getActivity();
				AsyncTask<OutputStream, ?, ?> task = ExportFragment.create(ac, ac.getSupportFragmentManager());
				new OpenPhoneOutputStream(task).execute();
				return true;
			case R.id.importSDCard: {
				BackupPickerFragment.choose(null, ".*\\.zip$", getString(R.string.backup_import_sd))
				                    .show(getActivity().getSupportFragmentManager(), "import");
				return true;
			}
			default:
				return false;
		}
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		final int[] ids = new int[] {
				R.id.importDrive,
				R.id.exportDrive,
				R.id.importSDCard,
				R.id.exportSDCard
		};
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.backup_title)
				.setItems(new CharSequence[] {
						getResources().getText(R.string.backup_import_drive),
						getResources().getText(R.string.backup_export_drive),
						getResources().getText(R.string.backup_import_sd),
						getResources().getText(R.string.backup_export_sd),
						getResources().getText(R.string.backup_export_sd_del),
				}, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						handle(ids[which]);
					}
				})
				.create();
	}
	@Override public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}

	@Override
	public void filePicked(Serializable tag, File file) {
		try {
			@SuppressWarnings("resource")
			InputStream input = new FileInputStream(file);
			DatabaseCSVImporter importer = new DatabaseCSVImporter();
			importer.importAll(input);
			App.toast("Import successful from " + file.getName());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static class OpenPhoneOutputStream extends SafeSimpleAsyncTask<Void, Void, OutputStream> {
		private AsyncTask<OutputStream, ?, ?> task;
		private OpenPhoneOutputStream(AsyncTask<OutputStream, ?, ?> task) {
			this.task = task;
		}
		@Override protected OutputStream doInBackgroundSafe(Void aVoid) throws Exception {
			File parent = App.getInstance().getPhoneHome();
			File file = new File(parent, Constants.Paths.getExportFileName());
			if (!(parent.mkdirs() || parent.isDirectory())) {
				throw new IOException("Cannot use directory: " + parent);
			}
			return new FileOutputStream(file);
		}
		@Override protected void onResult(OutputStream stream) {
			task.execute(stream);
		}
		@Override protected void onError(Exception error) {
			App.toast("Export failed: " + error.getMessage());
		}
	}

	public static BackupFragment create() {
		return new BackupFragment();
	}
}
