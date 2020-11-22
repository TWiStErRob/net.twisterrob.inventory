package net.twisterrob.inventory.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import androidx.annotation.*;

import net.twisterrob.inventory.android.view.Action;

public abstract class BaseAction implements Action {
	@Prepared private ValidationException validationError;

	@Override public final void prepare() {
		try {
			doPrepare();
		} catch (ValidationException ex) {
			validationError = ex;
			throw validationError;
		}
	}
	/** @throws ValidationException if the action cannot be executed, it must contain an error message. */
	@WorkerThread
	protected abstract void doPrepare() throws ValidationException;

	@Override public final @NonNull CharSequence getFailureMessage(@NonNull Resources res) {
		if (validationError != null) {
			return validationError.getMessage(res);
		} else {
			return getGenericFailureMessage(res);
		}
	}
	/** @see #getFailureMessage(Resources) */
	@UiThread
	protected abstract @NonNull CharSequence getGenericFailureMessage(@NonNull Resources res);

	@Override public final void execute() {
		doExecute();
	}
	@WorkerThread
	protected abstract void doExecute();

	@Override public View getConfirmationView(@NonNull Context context) {
		return null;
	}

	@Override public @NonNull String toString() {
		Class<?> actionClass = getClass();
		while ("".equals(actionClass.getSimpleName())) { // find the first non-inner-class
			actionClass = actionClass.getSuperclass();
		}
		return getClass().getName() + "(" + actionClass.getSimpleName() + ")" + '@' + Integer.toHexString(hashCode());
	}

	public static final class ValidationException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		private final int validationError;
		private final Object[] args;

		public ValidationException(@StringRes int validationError, Object... args) {
			this(null, validationError, args);
		}

		public ValidationException(Throwable throwable, @StringRes int validationError, Object... args) {
			super("Validation error", throwable);
			this.validationError = validationError;
			this.args = args;
		}

		public String getMessage(Resources res) {
			resolveArgs(res);
			return res.getString(validationError, args);
		}
		private void resolveArgs(Resources res) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Resolvable) {
					args[i] = ((Resolvable)args[i]).resolve(res);
				}
			}
		}
		public interface Resolvable {
			@Nullable Object resolve(@NonNull Resources res);
		}
	}
}
