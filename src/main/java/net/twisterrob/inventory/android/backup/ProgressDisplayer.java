package net.twisterrob.inventory.android.backup;

import java.util.concurrent.CancellationException;

import android.content.*;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AlertDialog;

import net.twisterrob.inventory.android.R;

public class ProgressDisplayer {
	private final Context context;
	private Progress progress;

	public ProgressDisplayer(Context context, Progress progress) {
		this(context);
		this.progress = progress;
	}
	public ProgressDisplayer(Context context) {
		this.context = context;
	}

	public boolean hasProgress() {
		return progress != null;
	}

	public void setProgress(Progress progress) {
		this.progress = progress;
	}

	public Progress getProgress() {
		return progress;
	}

	public String getMessage() {
		switch (progress.type) {
			case Export:
				switch (progress.phase) {
					case Init:
						return context.getString(R.string.backup_export_progress_init);
					case Data:
						return context.getString(R.string.backup_export_progress_data);
					case Images:
						return context.getString(R.string.backup_export_progress_images, progress.imagesTotal,
								progress.total);
					case Finished:
						if (progress.failure != null) {
							if (progress.failure instanceof CancellationException) {
								return context.getString(R.string.backup_import_result_cancelled);
							} else {
								return context.getString(R.string.backup_export_result_failed,
										progress.failure.getMessage());
							}
						} else {
							return context.getString(R.string.backup_export_result_success,
									progress.total, progress.imagesTotal);
						}
				}
				break;
			case Import:
				switch (progress.phase) {
					case Init:
						return context.getString(R.string.backup_import_progress_init);
					case Data:
						return context.getString(R.string.backup_import_progress_data);
					case Images:
						return context.getString(R.string.backup_import_progress_images);
					case Finished:
						if (progress.failure != null) {
							if (progress.failure instanceof CancellationException) {
								return context.getString(R.string.backup_import_result_cancelled);
							} else {
								return context.getString(R.string.backup_import_result_failed,
										progress.failure.getMessage());
							}
						} else {
							if (progress.warnings.isEmpty()) {
								return context.getString(R.string.backup_import_result_success,
										progress.total);
							} else {
								return context.getString(R.string.backup_import_result_warning,
										progress.total, progress.done, progress.warnings.size());
							}
						}
				}
				break;
		}
		throw notImplementedPhase();
	}

	public boolean isIndeterminate() {
		return progress.pending || getTotal() <= 0;
	}

	public int getDone() {
		switch (progress.phase) {
			case Init:
				return 0;
			case Data:
			case Finished:
				return progress.done;
			case Images:
				return progress.imagesDone;
		}
		throw notImplementedPhase();
	}

	public int getTotal() {
		switch (progress.phase) {
			case Init:
				return 0;
			case Data:
			case Finished:
				return progress.total;
			case Images:
				return progress.imagesTotal;
		}
		throw notImplementedPhase();
	}

	public String getTitle() {
		switch (progress.type) {
			case Import:
				return context.getString(R.string.backup_import_progress_title);
			case Export:
				return context.getString(R.string.backup_export_progress_title);
		}
		throw notImplementedType();
	}

	public @DrawableRes int getIcon() {
		switch (progress.type) {
			case Import:
				return android.R.drawable.ic_menu_upload;
			case Export:
				return android.R.drawable.ic_menu_upload;
		}
		throw notImplementedType();
	}

	private UnsupportedOperationException notImplementedPhase() {
		return new UnsupportedOperationException("Phase " + progress.phase + " not implemented.");
	}
	private UnsupportedOperationException notImplementedType() {
		return new UnsupportedOperationException("Type " + progress.type + " not implemented.");
	}
	public static boolean isVeryDifferentFrom(Progress prev, Progress curr) {
		if (prev == null || curr == null
				|| prev.phase != curr.phase
				|| prev.pending != curr.pending
				|| prev.type != curr.type
				|| prev.failure != curr.failure
				) {
			return true;
		}
		// everything is the same, let's check if it progressed more than about 1%
		return farEnoughPercent(prev, curr, 0.009f) || farEnoughImagePercent(prev, curr, 0.009f);
	}
	private static boolean farEnoughPercent(Progress prev, Progress curr, float threshold) {
		int difference = Math.abs(prev.done - curr.done);
		float differencePercent = (float)difference / Math.max(prev.total, curr.total);
		return threshold <= differencePercent;
	}
	private static boolean farEnoughImagePercent(Progress prev, Progress curr, float threshold) {
		int imageDifference = Math.abs(prev.imagesDone - curr.imagesDone);
		float imageDifferencePercent = (float)imageDifference / Math.max(prev.imagesTotal, curr.imagesTotal);
		return threshold <= imageDifferencePercent;
	}

	public void displayFinishMessage() {
		// TODO handle power button of this
		// TODO handle rotation of this and also WindowLeaked probably related
		// (android.view.WindowLeaked: Activity net.twisterrob.inventory.android.activity.BackupActivity has leaked window)
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setCancelable(false)
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						if (context instanceof OnRefreshListener) {
							((OnRefreshListener)context).onRefresh();
						}
					}
				});
		if (!progress.warnings.isEmpty()) {
			builder.setTitle(getMessage());
			builder.setItems(progress.warnings.toArray(new CharSequence[progress.warnings.size()]), null);
		} else {
			builder.setTitle(getTitle());
			builder.setMessage(getMessage());
		}
		builder.show();
	}
}
