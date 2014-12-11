package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.io.ExporterTask;
import net.twisterrob.inventory.android.content.io.ExporterTask.ExportCallbacks;

public class ExportFragment extends DialogFragment implements ExportCallbacks {
	private static final Logger LOG = LoggerFactory.getLogger(ExporterTask.class);

	private ExporterTask task;
	private FragmentManager parentFragmentManager;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override public void onDetach() {
		task.setCallbacks(null);
		super.onDetach();
	}

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		task.setCallbacks(this);
	}

	@Override public void exportStarting() {
		LOG.trace("exportStarting");
		show(parentFragmentManager, "export");
	}
	@Override public void exportProgress(Progress progress) {
		LOG.trace("exportProgress {}", progress);
		ProgressDialog dialog = (ProgressDialog)getDialog();
		if (progress.copyingImages) {
			dialog.setMessage("Exporting images");
			dialog.setIndeterminate(false);
			dialog.setProgress(progress.done);
			dialog.setMax(progress.total);
		} else {
			dialog.setMessage("Preparing export");
			dialog.setIndeterminate(false);
			dialog.setProgress(progress.done);
			dialog.setMax(progress.total);
		}
	}
	@Override public void exportFinished(Progress progress) {
		LOG.trace("exportFinished {}", progress);
		dismiss();
		App.toast("Done exporting " + progress.total + " belongings.");
	}

	@Override public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		task.cancel(true);
	}

	@NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setIcon(android.R.drawable.ic_menu_upload);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setTitle("Exporting");
		// needs to be non-null at creation time to be able to change it later
		dialog.setMessage(getActivity().getString(R.string.empty));
		return dialog;
	}

	public static ExportFragment create(FragmentManager fm, ExporterTask task) {
		ExportFragment fragment = new ExportFragment();
		fragment.parentFragmentManager = fm;
		fragment.task = task;
		task.setCallbacks(fragment);
		return fragment;
	}
}
