package net.twisterrob.inventory.android.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.view.View;

import net.twisterrob.inventory.android.view.Action;

public abstract class BaseAction implements Action {
	private ValidationException validationError;

	@Override public final void prepare() {
		try {
			doPrepare();
		} catch (ValidationException ex) {
			validationError = ex;
			throw validationError;
		}
	}
	protected abstract void doPrepare();

	@Override public final CharSequence getFailureMessage(Resources res) {
		if (validationError != null) {
			return validationError.getMessage(res);
		} else {
			return getGenericFailureMessage(res);
		}
	}
	protected abstract CharSequence getGenericFailureMessage(Resources res);

	@Override public final void execute() {
		doExecute();
	}
	protected abstract void doExecute();

	@Override public View getConfirmationView(Context context) {
		return null;
	}

	@Override public String toString() {
		Class<?> actionClass = getClass();
		while ("".equals(actionClass.getSimpleName())) { // find the first non-inner-class
			actionClass = actionClass.getSuperclass();
		}
		return getClass().getName() + "(" + actionClass.getSimpleName() + ")" + '@' + Integer.toHexString(hashCode());
	}

	protected static final class ValidationException extends RuntimeException {
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
			return res.getString(validationError, args);
		}
	}
}
