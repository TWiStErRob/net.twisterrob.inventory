package net.twisterrob.android.utils.log;

import org.slf4j.*;

import android.support.annotation.*;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.view.View;

import net.twisterrob.android.annotation.BottomSheetState;
import net.twisterrob.android.utils.log.LoggingDebugProvider.LoggingHelper;
import net.twisterrob.android.utils.tools.StringerTools;

public class LoggingBottomSheetCallback extends BottomSheetCallback {
	private static final Logger LOG = LoggerFactory.getLogger(LoggingBottomSheetCallback.class);

	@Override public void onStateChanged(@NonNull View bottomSheet, @BottomSheetState int newState) {
		log("onStateChanged", bottomSheet, BottomSheetState.Converter.toString(newState));
	}

	@Override public void onSlide(@NonNull View bottomSheet, @FloatRange(from = -1, to = 1) float slideOffset) {
		log("onSlide", bottomSheet, slideOffset);
	}

	private void log(String method, Object... args) {
		LoggingHelper.log(LOG, StringerTools.toNameString(this), method, null, args);
	}
}
