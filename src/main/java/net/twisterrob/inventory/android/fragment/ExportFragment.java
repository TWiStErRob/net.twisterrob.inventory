package net.twisterrob.inventory.android.fragment;

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
import net.twisterrob.inventory.android.content.io.ExporterTask;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks;
import net.twisterrob.inventory.android.content.io.xml.ZippedXMLExporter;

public class ExportFragment extends BaseDialogFragment implements ExportCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(ExportFragment.class);

	private ExporterTask task;
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
			task.setCallbacks(new ExportFinishedListener(getActivity().getApplicationContext()));
		}
		super.onPause();
	}

	public void execute() {
		task.execute();
	}

	@Override public void exportStarting() {
		LOG.trace("exportStarting");
		show(parentFragmentManager, "export");
		parentFragmentManager = null;
	}

	@Override public void exportProgress(@NonNull Progress progress) {
		LOG.trace("exportProgress {}", progress);
		ProgressDialog dialog = (ProgressDialog)getDialog();
		if (dialog != null) {
			updateProgress(dialog, progress);
		} else {
			firstProgressToShow = progress;
			LOG.warn("Premature progress, no dialog");
		}
	}
	private void updateProgress(ProgressDialog dialog, Progress progress) {
		switch (progress.phase) {
			case Init:
				dialog.setMessage(getString(R.string.backup_export_progress_init));
				dialog.setIndeterminate(true);
				break;
			case Data:
				dialog.setMessage(getString(R.string.backup_export_progress_data));
				dialog.setIndeterminate(false);
				dialog.setProgress(progress.done);
				dialog.setMax(progress.total);
				break;
			case Images:
				dialog.setMessage(getString(R.string.backup_export_progress_images,
						progress.imagesTotal, progress.total));
				dialog.setIndeterminate(false);
				dialog.setProgress(progress.imagesDone);
				dialog.setMax(progress.imagesTotal);
				break;
		}
	}

	@Override public void exportFinished(@NonNull Progress res) {
		displayFinishMessage(getActivity(), res);
	}

	private void displayFinishMessage(final Context context, Progress p) {
		LOG.trace("exportFinished {}", context);
		dismissAllowingStateLoss();

		String message;
		if (p.failure != null) {
			message = context.getString(R.string.backup_export_result_failed, p.failure.getMessage());
		} else {
			message = context.getString(R.string.backup_export_result_success, p.total, p.imagesTotal);
		}

		if (context instanceof Activity) {
			new Builder(context)
					.setCancelable(false)
					.setNeutralButton(android.R.string.ok, new OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							if (context instanceof OnRefreshListener) {
								((OnRefreshListener)context).onRefresh();
							}
						}
					})
					.setTitle(R.string.backup_export_progress_title)
					.setMessage(message)
					.create()
					.show()
			;
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
		dialog.setTitle(R.string.backup_export_progress_title);
		// needs to be non-null at creation time to be able to change it later
		dialog.setMessage(getActivity().getString(R.string.empty));
		if (firstProgressToShow != null) {
			updateProgress(dialog, firstProgressToShow);
		}
		return dialog;
	}

	public static ExportFragment create(Context context, FragmentManager fm) {
		ExportFragment fragment = new ExportFragment();
		fragment.parentFragmentManager = fm;
		fragment.task = new ExporterTask(new ZippedXMLExporter(), context);
		fragment.task.setCallbacks(fragment);
		return fragment;
	}

	private final class ExportFinishedListener implements ExportCallbacks {
		private final Context context;

		ExportFinishedListener(Context context) {
			this.context = context;
		}

		@Override public void exportStarting() {
			// ignore, can't start UI
		}
		@Override public void exportProgress(@NonNull Progress progress) {
			// ignore, there's no UI
		}
		@Override public void exportFinished(@NonNull Progress p) {
			displayFinishMessage(context, p);
		}
	}
}
