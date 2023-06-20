package net.twisterrob.inventory.android.components;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public interface ErrorMapper {
	@NonNull CharSequence getError(@NonNull Throwable ex, @StringRes int errorResource, Object... args);
	@NonNull CharSequence getError(@NonNull Throwable ex, @NonNull CharSequence message);
}
