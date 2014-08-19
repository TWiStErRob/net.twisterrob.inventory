package net.twisterrob.inventory.android.fragment;

import java.io.*;
import java.util.*;

import android.view.MenuItem;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.io.csv.*;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;

public class BackupFragment extends BaseFragment<Void> implements BackupPickerListener {
	private PickMode pickMode;

	public BackupFragment() {
		setDynamicResource(DYN_OptionsMenu, R.menu.backup);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.exportDrive:
				startActivity(ExportActivity.chooser());
				break;
			case R.id.importDrive:
				startActivity(ImportActivity.chooser());
				break;
			case R.id.exportSDCard:
				doExport();
				break;
			case R.id.importSDCard: {
				pickMode = PickMode.Import;
				showDialog(BackupPickerFragment.choose("Select a backup to restore", ".csv"));
				break;
			}
			case R.id.exportSDCardRemove: {
				pickMode = PickMode.Remove;
				showDialog(BackupPickerFragment.choose("Select a backup to remove", ".csv"));
				break;
			}
			default:
				// let super decide
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private static void doExport() {
		String fileName = String.format(Locale.ROOT, Constants.EXPORT_FILE_NAME_FORMAT, Calendar.getInstance());

		File path = new File(App.getInstance().getPhoneHome(), Constants.EXPORT_SDCARD_FOLDER);
		path.mkdirs();

		File file = new File(path, fileName);
		try {
			new DatabaseCSVExporter().export(new FileOutputStream(file));
			App.toast("Exported successfully to " + file);
		} catch (IOException ex) {
			ex.printStackTrace();
			App.toast("Export failed: " + ex.getMessage());
		}
	}

	@Override
	public void filePicked(File file) {
		if (pickMode == null) {
			throw new IllegalStateException("Access this method through a dialog's callback");
		}
		switch (pickMode) {
			case Import: {
				DatabaseCSVImporter importer = null;
				try {
					@SuppressWarnings("resource")
					InputStream input = new FileInputStream(file);
					importer = new DatabaseCSVImporter();
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
		Remove;
	}
}
