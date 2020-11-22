package net.twisterrob.android.utils.log;

import org.slf4j.*;

import com.google.android.material.snackbar.Snackbar;

import net.twisterrob.android.annotation.SnackbarDismissEvent;
import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;

public class LoggingSnackbarCallback extends Snackbar.Callback {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingSnackbarCallback.class);
	@Override public void onDismissed(Snackbar snackbar, @SnackbarDismissEvent int event) {
		super.onDismissed(snackbar, event);
		log("onDismissed", snackbar, SnackbarDismissEvent.Converter.toString(event));
	}
	@Override public void onShown(Snackbar snackbar) {
		super.onShown(snackbar);
		log("onShown", snackbar);
	}
	private void log(String method, Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), method, null, args);
	}
}
