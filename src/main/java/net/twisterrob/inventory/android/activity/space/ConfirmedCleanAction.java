package net.twisterrob.inventory.android.activity.space;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import net.twisterrob.inventory.android.fragment.BaseDialogFragment;

@SuppressLint("ValidFragment") // this should never be re-created: setRetainInstance(true)
class ConfirmedCleanAction extends BaseDialogFragment implements DialogInterface.OnClickListener {
	private final NoProgressTaskExecutor.UITask task;
	private final CharSequence title;
	private final CharSequence message;

	ConfirmedCleanAction(CharSequence title, CharSequence message, NoProgressTaskExecutor.UITask task) {
		this.title = title;
		this.message = message;
		this.task = task;
	}

	@Override public void onClick(DialogInterface var1, int var2) {
		NoProgressTaskExecutor.create(task).show(getFragmentManager(), "task");
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle var1) {
		return new Builder(getActivity())
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.cancel, null)
				.setCancelable(true)
				.create();
	}
}
