package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.io.ExporterTask;
import net.twisterrob.inventory.android.content.io.csv.DatabaseCSVImporter;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;

public class BackupFragment extends DialogFragment implements BackupPickerListener {
	private PickMode pickMode;

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
				ExporterTask task = new ExporterTask(getActivity());
				ExportFragment.create(getActivity().getSupportFragmentManager(), task);

				String fileName = String.format(
						Locale.ROOT, Constants.Paths.EXPORT_FILE_NAME_FORMAT, Calendar.getInstance());
				File path = new File(App.getInstance().getPhoneHome(), Constants.Paths.EXPORT_SDCARD_FOLDER);
				final File file = new File(path, fileName);
				try {
					// TODO move IO to background
					file.getParentFile().mkdirs();
					task.execute(new FileOutputStream(file));
				} catch (IOException ex) {
					ex.printStackTrace();
					App.toast("Export failed: " + ex.getMessage());
				}
				return true;
			case R.id.importSDCard: {
				pickMode = PickMode.Import;
				showDialog(BackupPickerFragment.choose("Select a backup to restore", ".csv"));
				return true;
			}
			case R.id.exportSDCardRemove: {
				pickMode = PickMode.Remove;
				showDialog(BackupPickerFragment.choose("Select a backup to remove", ".csv"));
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
				R.id.exportSDCard,
				R.id.exportSDCardRemove
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

	protected void showDialog(DialogFragment dialog) {
		dialog.show(getFragmentManager(), dialog.getClass().getSimpleName());
	}

	@Override
	public void filePicked(File file) {
		if (pickMode == null) {
			throw new IllegalStateException("Access this method through a dialog's callback");
		}
		switch (pickMode) {
			case Import: {
				try {
					@SuppressWarnings("resource")
					InputStream input = new FileInputStream(file);
					DatabaseCSVImporter importer = new DatabaseCSVImporter();
					importer.importAll(input);
					App.toast("Import successful from " + file.getName());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				break;
			}
			case Remove: {
				file.delete();
				App.toast("Backup removed successfully: " + file.getName());
				break;
			}
			default:
				throw new UnsupportedOperationException(pickMode + " is not implemented");
		}
		pickMode = null;
	}

	private static enum PickMode {
		Import,
		Remove
	}

	public static BackupFragment create() {
		return new BackupFragment();
	}
}
