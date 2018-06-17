package net.twisterrob.inventory.android.backup;

import android.content.Context;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog;

import net.twisterrob.android.AndroidConstants;
import net.twisterrob.android.utils.tools.DialogTools.PopupCallbacks;
import net.twisterrob.inventory.android.backup.Progress.Type;

public class LenientProgressInfoProvider implements ProgressInfoProvider {
	private final ProgressInfoProvider provider;
	private boolean hasProgress = false;

	public LenientProgressInfoProvider(Context context) {
		// what progress is used here doesn't matter, because hasProgress is false
		this(new StrictProgressInfoProvider(context, new Progress(Type.Export)));
	}

	public LenientProgressInfoProvider(ProgressInfoProvider provider) {
		this.provider = provider;
	}

	public boolean hasProgress() {
		return hasProgress;
	}
	@Override public void setProgress(@Nullable Progress progress) {
		hasProgress = progress != null;
		if (hasProgress()) {
			this.provider.setProgress(progress);
		}
	}
	@Override public @Nullable Progress getProgress() {
		return hasProgress()? provider.getProgress() : null;
	}
	@Override public @Nullable String getMessage() {
		return hasProgress()? provider.getMessage() : null;
	}
	@Override public boolean isIndeterminate() {
		//noinspection SimplifiableConditionalExpression keep it consistent with other methods
		return hasProgress()? provider.isIndeterminate() : true;
	}
	@Override public int getDone() {
		return hasProgress()? provider.getDone() : 0;
	}
	@Override public int getTotal() {
		return hasProgress()? provider.getTotal() : 0;
	}
	@Override public @Nullable String getTitle() {
		return hasProgress()? provider.getTitle() : null;
	}
	@Override public @DrawableRes int getIcon() {
		return hasProgress()? provider.getIcon() : AndroidConstants.INVALID_RESOURCE_ID;
	}
	@Override public @Nullable AlertDialog displayFinishMessage(@Nullable PopupCallbacks<Void> callback) {
		return hasProgress()? provider.displayFinishMessage(callback) : null;
	}
}
