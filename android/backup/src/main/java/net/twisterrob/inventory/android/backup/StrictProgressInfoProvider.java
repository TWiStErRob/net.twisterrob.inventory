package net.twisterrob.inventory.android.backup;

import java.util.concurrent.CancellationException;

import android.content.*;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;

import net.twisterrob.android.utils.tools.DialogTools;
import net.twisterrob.inventory.android.backup.Progress.Phase;
import net.twisterrob.inventory.android.backup.xml.InvalidDataXmlException;
import net.twisterrob.java.utils.ObjectTools;

/** Strict in a sense that it doesn't deal with {@code null}s. */
public class StrictProgressInfoProvider implements ProgressInfoProvider {
	private final Context context;
	private @NonNull Progress progress;

	public StrictProgressInfoProvider(@NonNull Context context, @NonNull Progress progress) {
		this.context = context;
		this.progress = ObjectTools.checkNotNull(progress);
	}

	public void setProgress(@Nullable Progress progress) {
		if (progress == null) {
			throw new IllegalArgumentException("Cannot work without progress.");
		}
		this.progress = progress;
	}

	public @NonNull Progress getProgress() {
		return progress;
	}

	@Override public @NonNull String getMessage() {
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
								return context.getString(R.string.backup_export_result_cancelled);
							} else {
								return context.getString(R.string.backup_export_result_failed,
										progress.failure.getMessage());
							}
						} else {
							return context.getString(R.string.backup_export_result_success,
									progress.total, progress.imagesTotal);
						}
					default:
						throw notImplementedPhase();
				}
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
							} else if (progress.failure instanceof InvalidDataXmlException) {
								return context.getString(R.string.backup_import_result_invalid_xml,
										progress.failure.getMessage());
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
					default:
						throw notImplementedPhase();
				}
		}
		throw notImplementedType();
	}

	@Override public boolean isIndeterminate() {
		return progress.pending || getTotal() <= 0;
	}

	@Override public int getDone() {
		switch (progress.phase) {
			case Init:
				return 0;
			case Data:
			case Finished:
				return progress.done;
			case Images:
				return progress.imagesDone;
			default:
				throw notImplementedPhase();
		}
	}

	@Override public int getTotal() {
		switch (progress.phase) {
			case Init:
				return 0;
			case Data:
			case Finished:
				return progress.total;
			case Images:
				return progress.imagesTotal;
			default:
				throw notImplementedPhase();
		}
	}

	@Override public @NonNull String getTitle() {
		switch (progress.type) {
			case Import:
				if (progress.phase == Phase.Finished) {
					return context.getString(R.string.backup_import_result_finished);
				}
				return context.getString(R.string.backup_import_progress_title);
			case Export:
				if (progress.phase == Phase.Finished) {
					return context.getString(R.string.backup_export_result_finished);
				}
				return context.getString(R.string.backup_export_progress_title);
			default:
				throw notImplementedType();
		}
	}

	@Override public @DrawableRes int getIcon() {
		switch (progress.type) {
			case Import:
				return android.R.drawable.ic_menu_upload;
			case Export:
				return android.R.drawable.ic_menu_upload;
			default:
				throw notImplementedType();
		}
	}

	private UnsupportedOperationException notImplementedPhase() {
		return new UnsupportedOperationException("Phase " + progress.phase + " not implemented in " + progress.type + ".");
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

	@Override
	public @NonNull AlertDialog displayFinishMessage(final @Nullable DialogTools.PopupCallbacks<Void> callback) {
		if (progress.phase != Phase.Finished) {
			throw new IllegalArgumentException("Not finished: " + progress);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setCancelable(false)
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						if (callback != null) {
							callback.finished(null);
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
		return builder.show();
	}
}
