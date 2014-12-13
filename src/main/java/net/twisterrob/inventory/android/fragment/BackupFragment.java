package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.io.ExporterTask;

public class BackupFragment extends DialogFragment {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterTask.class);

	private static final int IMPORT_DRIVE = 0;
	private static final int EXPORT_DRIVE = 1;
	private static final int IMPORT_SD = 2;
	private static final int EXPORT_SD = 3;

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		CharSequence[] items = new CharSequence[4];
		items[IMPORT_DRIVE] = getResources().getText(R.string.backup_import_drive);
		items[EXPORT_DRIVE] = getResources().getText(R.string.backup_export_drive);
		items[IMPORT_SD] = getResources().getText(R.string.backup_import_sd);
		items[EXPORT_SD] = getResources().getText(R.string.backup_export_sd);

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.backup_title)
				.setCancelable(true)
				.setItems(items, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case EXPORT_DRIVE:
								startActivity(ExportGoogleActivity.chooser());
								break;
							case IMPORT_DRIVE:
								startActivity(ImportGoogleActivity.chooser());
								break;
							case EXPORT_SD:
								startActivity(ExportActivity.chooser());
								break;
							case IMPORT_SD: {
								startActivity(ImportActivity.chooser());
								break;
							}
						}
					}
				})
				.create();
	}

	public static BackupFragment create() {
		return new BackupFragment();
	}
}
