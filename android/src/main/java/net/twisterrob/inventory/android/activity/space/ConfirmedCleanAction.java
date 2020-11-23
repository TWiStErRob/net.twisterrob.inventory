package net.twisterrob.inventory.android.activity.space;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

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
		setRetainInstance(true);
	}

	@Override public void onClick(DialogInterface var1, int var2) {
		NoProgressTaskExecutor.create(task).show(getParentFragmentManager(), "task");
	}

	@Override public @NonNull Dialog onCreateDialog(Bundle var1) {
		return new AlertDialog.Builder(requireActivity())
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.cancel, null)
				.setCancelable(true)
				.create();
	}
}
