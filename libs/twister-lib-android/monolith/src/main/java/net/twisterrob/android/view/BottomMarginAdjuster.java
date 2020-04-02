package net.twisterrob.android.view;

import org.slf4j.*;

import android.support.annotation.*;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

import net.twisterrob.android.annotation.BottomSheetState;

public class BottomMarginAdjuster extends BottomSheetCallback {
	private static final Logger LOG = LoggerFactory.getLogger(BottomMarginAdjuster.class);

	private final ViewProvider viewProvider;
	private final boolean adjustOnStateChange;
	public BottomMarginAdjuster(ViewProvider viewProvider, boolean adjustOnStateChange) {
		this.viewProvider = viewProvider;
		this.adjustOnStateChange = adjustOnStateChange;
	}
	@Override public void onStateChanged(@NonNull View bottomSheet, @BottomSheetState int newState) {
		if (!adjustOnStateChange) {
			return;
		}
		switch (newState) {
			case BottomSheetBehavior.STATE_EXPANDED:
				onSlide(bottomSheet, 1);
				break;
			case BottomSheetBehavior.STATE_COLLAPSED:
				onSlide(bottomSheet, 0);
				break;
			case BottomSheetBehavior.STATE_HIDDEN:
				onSlide(bottomSheet, -1);
				break;
			case BottomSheetBehavior.STATE_DRAGGING:
				// don't care, onSlide is being called
				break;
			case BottomSheetBehavior.STATE_SETTLING:
				// don't care, onSlide is being called
				break;
		}
	}

	@Override public void onSlide(@NonNull View bottomSheet, @FloatRange(from = -1, to = 1) float slideOffset) {
		float offset = slideOffset;
		//noinspection Range: tell that to BottomSheetBehavior
		if (Float.isNaN(slideOffset) || slideOffset == Float.POSITIVE_INFINITY) {
			offset = 0;
		}
		BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
		MarginLayoutParams params = (MarginLayoutParams)viewProvider.getView().getLayoutParams();
		// the sheet can have less content than peekHeight
		// workaround because when that is true, slideOffset won't reach ±1.0
		float ratio = bottomSheet.getHeight() / (float)behavior.getPeekHeight();
		if (bottomSheet.getHeight() < behavior.getPeekHeight() && offset < ratio) {
			offset = offset / ratio;
		}
		int height = Math.min(behavior.getPeekHeight(), bottomSheet.getHeight());
		height *= 1 + Math.min(0, offset); // slideOffset is negative so this is really "1 - offset"
		LOG.trace("slideOffset={}, offset={}, sheet={}, peek={}, ratio={}, margin={}",
				slideOffset, offset, bottomSheet.getHeight(), behavior.getPeekHeight(), ratio, height);
		params.bottomMargin = height;
		viewProvider.getView().setLayoutParams(params);
	}
}
