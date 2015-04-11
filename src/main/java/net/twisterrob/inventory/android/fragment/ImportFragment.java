package net.twisterrob.inventory.android.fragment;

import java.io.File;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.io.ImporterTask;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks;

public class ImportFragment extends BaseDialogFragment implements ImportCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(ImportFragment.class);

	private ImporterTask task;
	private FragmentManager parentFragmentManager;
	private Progress progress;

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		task.setCallbacks(this);
	}

	@Override public void onDetach() {
		task.setCallbacks(new ImportFinishedListener(getActivity().getApplicationContext()));
		super.onDetach();
	}

	public void execute(File file) {
		task.execute(file);
	}

	@Override public void importStarting() {
		LOG.trace("importStarting");
		show(parentFragmentManager.beginTransaction().addToBackStack("import"), "import");
		parentFragmentManager = null;
	}

	@Override public void importProgress(Progress progress) {
		this.progress = progress;
		LOG.trace("importProgress {}", progress);
		updateProgress((ProgressDialog)getDialog(), progress);
	}
	private void updateProgress(ProgressDialog dialog, Progress progress) {
		switch (progress.phase) {
			case Init:
				dialog.setMessage(getString(R.string.backup_import_progress_init));
				dialog.setIndeterminate(true);
				break;
			case Data:
				dialog.setMessage(getString(R.string.backup_import_progress_data));
				dialog.setIndeterminate(false);
				dialog.setProgress((int)progress.done);
				dialog.setMax((int)progress.total);
				break;
		}
	}

	@Override public void importFinished(Progress res) {
		LOG.trace("importFinished {}", res);
		dismissAllowingStateLoss();
		displayFinishMessage(getActivity(), res);
	}

	private static void displayFinishMessage(final Context res, final Progress p) {
		String message;
		if (p.failure != null) {
			message = res.getString(R.string.backup_import_result_failed, p.failure.getMessage());
		} else {
			if (p.conflicts.isEmpty()) {
				message = res.getString(R.string.backup_import_result_success, p.total);
			} else {
				message = res.getString(R.string.backup_import_result_warning, p.total, p.done);
			}
		}
		if (!p.conflicts.isEmpty()) {
			new AlertDialog.Builder(res)
					.setTitle(message)
					.setItems(p.conflicts.toArray(new CharSequence[p.conflicts.size()]), null)
					.setCancelable(true)
					.setNeutralButton(android.R.string.ok, null)
					.create()
					.show()
			;
		} else {
			App.toast(message);
		}
	}

	@Override public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		task.cancel(true);
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setIcon(android.R.drawable.ic_menu_upload);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setTitle(R.string.backup_import_progress_title);
		// needs to be non-null at creation time to be able to change it later
		dialog.setMessage(getActivity().getString(R.string.empty));
		if (progress != null) {
			updateProgress(dialog, progress);
		}
		return dialog;
	}

	public static ImportFragment create(Context context, FragmentManager fm) {
		ImportFragment fragment = new ImportFragment();
		fragment.parentFragmentManager = fm;
		fragment.task = new ImporterTask(context);
		fragment.task.setCallbacks(fragment);
		return fragment;
	}

	private static final class ImportFinishedListener implements ImportCallbacks {
		Context context;

		ImportFinishedListener(Context context) {
			this.context = context;
		}

		@Override public void importStarting() {
			// ignore
		}
		@Override public void importProgress(Progress progress) {
			// ignore
		}
		@Override public void importFinished(Progress p) {
			displayFinishMessage(context, p);
		}
	}
}
