package net.twisterrob.inventory.android.activity;

import java.io.*;

import android.content.Intent;
import android.os.*;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;

public class ImportActivity extends BaseActivity implements BackupPickerListener {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			BackupPickerFragment.choose(null, ".*\\.zip$", getString(R.string.backup_import_sd))
			                    .show(getSupportFragmentManager(), "import");
		}
	}

	@Override
	public void filePicked(Serializable tag, File file) {
		AsyncTask<File, ?, ?> task = ImportFragment.create(this, getSupportFragmentManager());
		task.execute(file);
	}

	public static Intent chooser() {
		Intent intent = new Intent(App.getAppContext(), ImportActivity.class);
		return intent;
	}
}
