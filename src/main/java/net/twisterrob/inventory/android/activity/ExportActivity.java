package net.twisterrob.inventory.android.activity;

import java.io.*;

import android.content.Intent;
import android.os.*;
import android.support.annotation.NonNull;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.Constants.Paths;
import net.twisterrob.inventory.android.fragment.ExportFragment;
import net.twisterrob.inventory.android.view.SafeSimpleAsyncTask;

public class ExportActivity extends BaseActivity {
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			exportSDCard();
		}
	}
	private void exportSDCard() {
		AsyncTask<OutputStream, ?, ?> task = ExportFragment.create(this, getSupportFragmentManager());
		new OpenPhoneOutputStream(task).execute();
	}

	private static class OpenPhoneOutputStream extends SafeSimpleAsyncTask<Void, Void, OutputStream> {
		private AsyncTask<OutputStream, ?, ?> task;
		private OpenPhoneOutputStream(AsyncTask<OutputStream, ?, ?> task) {
			this.task = task;
		}
		@Override protected OutputStream doInBackgroundSafe(Void aVoid) throws Exception {
			return new FileOutputStream(Paths.getExportFile());
		}
		@Override protected void onResult(OutputStream stream) {
			task.execute(stream);
		}
		@Override protected void onError(@NonNull Exception error) {
			App.toast("Export failed: " + error.getMessage());
		}
	}

	public static Intent chooser() {
		Intent intent = new Intent(App.getAppContext(), ExportActivity.class);
		return intent;
	}
}
