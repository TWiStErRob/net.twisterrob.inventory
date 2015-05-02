package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.*;
import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.io.ExporterTask;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks;
import net.twisterrob.inventory.android.content.io.xml.ZippedXMLExporter;

public class ExportFragment extends BaseDialogFragment implements ExportCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(ExportFragment.class);

	private ExporterTask task;
	private FragmentManager parentFragmentManager;
	private Progress progress;

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		task.setCallbacks(this);
	}

	@Override public void onDetach() {
		task.setCallbacks(new ExportFinishedListener(getResources()));
		super.onDetach();
	}

	public void execute() {
		task.execute();
	}

	@Override public void exportStarting() {
		LOG.trace("exportStarting");
		show(parentFragmentManager.beginTransaction().addToBackStack("export"), "export");
		parentFragmentManager = null;
	}

	@Override public void exportProgress(Progress progress) {
		this.progress = progress;
		LOG.trace("exportProgress {}", progress);
		ProgressDialog dialog = (ProgressDialog)getDialog();
		if (dialog != null) {
			updateProgress(dialog, progress);
		} else {
			LOG.warn("Premature stop, no dialog");
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
				dialog.setMessage(
						getString(R.string.backup_export_progress_images, progress.imagesCount, progress.total));
				dialog.setIndeterminate(false);
				dialog.setProgress(progress.imagesTried);
				dialog.setMax(progress.imagesCount);
				break;
		}
	}

	@Override public void exportFinished(Progress res) {
		LOG.trace("exportFinished {}", res);
		dismissAllowingStateLoss();
		displayFinishMessage(getResources(), res);
	}

	private static void displayFinishMessage(Resources res, Progress p) {
		String message;
		if (p.failure == null) {
			if (p.imagesFailed == 0) {
				message = res.getString(R.string.backup_export_result_success, p.total);
			} else {
				message = res.getString(R.string.backup_export_result_warning, p.total, p.imagesFailed, p.imagesTried);
			}
		} else {
			message = res.getString(R.string.backup_export_result_failed, p.failure.getMessage());
		}
		App.toastUser(message);
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
		if (progress != null) {
			updateProgress(dialog, progress);
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

	private static final class ExportFinishedListener implements ExportCallbacks {
		Resources res;

		ExportFinishedListener(Resources res) {
			this.res = res;
		}

		@Override public void exportStarting() {
			// ignore
		}
		@Override public void exportProgress(Progress progress) {
			// ignore
		}
		@Override public void exportFinished(Progress p) {
			displayFinishMessage(res, p);
		}
	}
}
