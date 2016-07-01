package net.twisterrob.inventory.android.fragment;

import java.io.File;

import org.slf4j.*;

import android.app.*;
import android.app.AlertDialog.Builder;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.io.ImporterTask;
import net.twisterrob.inventory.android.content.io.ImporterTask.ImportCallbacks;

public class ImportFragment extends BaseDialogFragment implements ImportCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(ImportFragment.class);

	private ImporterTask task;
	private FragmentManager parentFragmentManager;
	private Progress firstProgressToShow;

	@Override public void onResume() {
		super.onResume();
		if (task == null) {
			// recover from a state where fragment was recreated by framework
			// this can happen if user leaves, task finishes, device rotated and user comes back
			dismissAllowingStateLoss();
		} else {
			task.setCallbacks(this);
		}
	}

	@Override public void onPause() {
		if (task != null) { // guard for recreated fragments, see onResume
			task.setCallbacks(new ImportFinishedListener(getActivity().getApplicationContext()));
		}
		super.onPause();
	}

	public void execute(File file) {
		task.execute(file);
	}

	@Override public void importStarting() {
		LOG.trace("importStarting");
		show(parentFragmentManager, "import");
		parentFragmentManager = null;
	}

	@Override public void importProgress(@NonNull Progress progress) {
		LOG.trace("importProgress {}", progress);
		ProgressDialog dialog = (ProgressDialog)getDialog();
		if (dialog != null) {
			updateProgress(dialog, progress);
		} else {
			firstProgressToShow = progress;
			LOG.warn("Premature progress, no dialog");
		}
	}
	private void updateProgress(@NonNull ProgressDialog dialog, @NonNull Progress progress) {
		if (progress.total == -1) {
			dialog.setIndeterminate(true);
			dialog.setMessage(getString(R.string.backup_import_progress_init));
		} else {
			dialog.setIndeterminate(false);
			dialog.setProgress((int)progress.done);
			dialog.setMax((int)progress.total);
			dialog.setMessage(getString(R.string.backup_import_progress_data));
		}
	}

	@Override public void importFinished(@NonNull Progress res) {
		onFinish(getActivity(), res);
	}

	private void onFinish(final Context context, final Progress p) {
		LOG.trace("importFinished {}", context);
		dismissAllowingStateLoss();

		String message;
		if (p.failure != null) {
			message = context.getString(R.string.backup_import_result_failed, p.failure.getMessage());
		} else {
			if (p.conflicts.isEmpty()) {
				message = context.getString(R.string.backup_import_result_success, p.total);
			} else {
				message = context.getString(R.string.backup_import_result_warning,
						p.total, p.done, p.conflicts.size());
			}
		}

		if (context instanceof Activity) {
			Builder builder = new Builder(context)
					.setCancelable(false)
					.setNeutralButton(android.R.string.ok, new OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							if (context instanceof OnRefreshListener) {
								((OnRefreshListener)context).onRefresh();
							}
						}
					});
			if (!p.conflicts.isEmpty()) {
				builder.setTitle(message);
				builder.setItems(p.conflicts.toArray(new CharSequence[p.conflicts.size()]), null);
			} else {
				builder.setTitle(R.string.backup_import_progress_title);
				builder.setMessage(message);
			}
			builder.create().show();
		} else {
			App.toastUser(message);
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
		if (firstProgressToShow != null) {
			updateProgress(dialog, firstProgressToShow);
		}
		return dialog;
	}

	public static ImportFragment create(Context context, FragmentManager fm) {
		ImportFragment fragment = new ImportFragment();
		fragment.parentFragmentManager = fm;
		fragment.task = new ImporterTask(context.getResources());
		fragment.task.setCallbacks(fragment);
		return fragment;
	}

	private final class ImportFinishedListener implements ImportCallbacks {
		private final Context context;

		ImportFinishedListener(Context context) {
			this.context = context;
		}

		@Override public void importStarting() {
			// ignore, can't start UI
		}
		@Override public void importProgress(@NonNull Progress progress) {
			// ignore, there's no UI
		}
		@Override public void importFinished(@NonNull Progress p) {
			onFinish(context, p);
		}
	}
}
