package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		Snackbar.Callback.DISMISS_EVENT_SWIPE,
		Snackbar.Callback.DISMISS_EVENT_ACTION,
		Snackbar.Callback.DISMISS_EVENT_TIMEOUT,
		Snackbar.Callback.DISMISS_EVENT_MANUAL,
		Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface SnackbarDismissEvent {
	class Converter {
		@DebugHelper
		public static String toString(@SnackbarDismissEvent int event) {
			switch (event) {
				case Snackbar.Callback.DISMISS_EVENT_SWIPE:
					return "DISMISS_EVENT_SWIPE";
				case Snackbar.Callback.DISMISS_EVENT_ACTION:
					return "DISMISS_EVENT_ACTION";
				case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
					return "DISMISS_EVENT_TIMEOUT";
				case Snackbar.Callback.DISMISS_EVENT_MANUAL:
					return "DISMISS_EVENT_MANUAL";
				case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
					return "DISMISS_EVENT_CONSECUTIVE";
			}
			return "snackBarDismissEvent::" + event;
		}
	}
}
