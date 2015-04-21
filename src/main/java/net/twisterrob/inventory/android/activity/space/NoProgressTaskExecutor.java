package net.twisterrob.inventory.android.activity.space;

import android.app.*;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.twisterrob.inventory.android.fragment.BaseDialogFragment;

class NoProgressTaskExecutor extends BaseDialogFragment implements TaskEndListener {
	interface UITask {
		void setCallbacks(TaskEndListener callbacks);
		void execute(Activity activity);
	}

	private TaskEndListener listener;
	private UITask task;

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (TaskEndListener)activity;
		task.setCallbacks(this);
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(false);
		task.execute(getActivity());
	}

	@Override public void onDetach() {
		task.setCallbacks(new NoActionTaskEndListener());
		listener = null;
		super.onDetach();
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setIndeterminate(true);
		//dialog.setTitle(title);
		dialog.setMessage("Please wait...");
		return dialog;
	}

	@Override public void taskDone() {
		dismissAllowingStateLoss();
		listener.taskDone();
	}

	public static NoProgressTaskExecutor create(UITask task) {
		NoProgressTaskExecutor fragment = new NoProgressTaskExecutor();
		fragment.task = task;
		return fragment;
	}
}
