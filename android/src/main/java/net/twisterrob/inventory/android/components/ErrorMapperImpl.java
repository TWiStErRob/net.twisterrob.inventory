package net.twisterrob.inventory.android.components;

import javax.inject.Inject;

import androidx.annotation.NonNull;

import net.twisterrob.inventory.android.App;

class ErrorMapperImpl implements ErrorMapper {

	@Inject
	public ErrorMapperImpl() {
	}

	@Override public @NonNull CharSequence getError(
			@NonNull Throwable ex, int errorResource, Object... args
	) {
		return App.getError(ex, errorResource, args);
	}
	@Override public @NonNull CharSequence getError(
			@NonNull Throwable ex, @NonNull CharSequence message
	) {
		return App.getError(ex, message);
	}
}
