package net.twisterrob.inventory.android.backup;

import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;

import net.twisterrob.android.utils.tools.DialogTools;

public interface ProgressInfoProvider {
	void setProgress(@Nullable Progress progress);
	@Nullable Progress getProgress();
	@Nullable String getMessage();
	boolean isIndeterminate();
	int getDone();
	int getTotal();
	@Nullable String getTitle();
	@DrawableRes int getIcon();
	@Nullable AlertDialog displayFinishMessage(@Nullable DialogTools.PopupCallbacks<Void> callback);
}
