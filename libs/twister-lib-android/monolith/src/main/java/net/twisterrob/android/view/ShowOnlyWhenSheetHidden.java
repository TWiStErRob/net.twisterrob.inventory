package net.twisterrob.android.view;

import android.support.annotation.*;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.view.View;

import net.twisterrob.android.annotation.BottomSheetState;

public class ShowOnlyWhenSheetHidden extends BottomSheetCallback {
	private final ViewProvider viewProvider;
	public ShowOnlyWhenSheetHidden(ViewProvider viewProvider) {
		this.viewProvider = viewProvider;
	}
	@Override public void onStateChanged(@NonNull View bottomSheet, @BottomSheetState int newState) {
		switch (newState) {
			case BottomSheetBehavior.STATE_SETTLING:
			case BottomSheetBehavior.STATE_DRAGGING:
			case BottomSheetBehavior.STATE_EXPANDED:
			case BottomSheetBehavior.STATE_COLLAPSED:
				viewProvider.getView().setVisibility(View.INVISIBLE);
				break;
			case BottomSheetBehavior.STATE_HIDDEN:
				viewProvider.getView().setVisibility(View.VISIBLE);
				break;
		}
	}
	@Override public void onSlide(@NonNull View bottomSheet, @FloatRange(from = -1, to = 1) float slideOffset) {
		// no op
	}
}
