package net.twisterrob.inventory.android.activity.space;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.*;

import net.twisterrob.inventory.android.fragment.BaseDialogFragment;

@SuppressLint("ValidFragment") // this should never be re-created: setRetainInstance(true)
class NoProgressTaskExecutor extends BaseDialogFragment implements TaskEndListener {
	interface UITask {
		void setCallbacks(TaskEndListener callbacks);
		void execute(Activity activity);
	}

	private TaskEndListener listener;
	private UITask task;

	@SuppressWarnings("deprecation") // https://github.com/TWiStErRob/net.twisterrob.inventory/issues/258
	public NoProgressTaskExecutor() {
		setRetainInstance(true);
	}

	@Override public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		listener = (TaskEndListener)context;
		task.setCallbacks(this);
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(false);
		task.execute(requireActivity());
	}

	@Override public void onDetach() {
		task.setCallbacks(new NoActionTaskEndListener());
		listener = null;
		super.onDetach();
	}

	@SuppressWarnings("deprecation") // blocking the user's view intentionally
	@Override public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(requireContext());
		dialog.setIndeterminate(true);
		//dialog.setTitle(title);
		dialog.setMessage("Please wait...");
		return dialog;
	}

	@Override public void taskDone() {
		dismissAllowingStateLoss();
		listener.taskDone();
	}

	public static @NonNull NoProgressTaskExecutor create(@NonNull UITask task) {
		NoProgressTaskExecutor fragment = new NoProgressTaskExecutor();
		fragment.task = task;
		return fragment;
	}
}
