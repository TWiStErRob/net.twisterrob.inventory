package net.twisterrob.android.utils.concurrent;

import androidx.annotation.*;

class SimpleAsyncTaskHelper {
	static @Nullable <T> T getSingleOrThrow(@NonNull String type, @Nullable T[] params, boolean allowZeroLength) {
		T param;
		if (params != null) {
			if (params.length == 1) {
				param = params[0];
			} else if (allowZeroLength && params.length == 0) {
				param = null;
			} else {
				throw invalidArg(type + " had " + params.length + " parameters");
			}
		} else {
			param = null;
		}
		return param;
	}

	static RuntimeException invalidArg(@NonNull String message) {
		return new IllegalArgumentException(SimpleAsyncTask.class.getSimpleName() + " is for simple tasks, " + message);
	}
}
