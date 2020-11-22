package net.twisterrob.android.view;

import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.*;

import net.twisterrob.android.annotation.BottomSheetState;

public class ShowOnlyWhenSheetNotExpandedFAB extends BottomSheetCallback {
	private final FloatingActionButton fab;
	public ShowOnlyWhenSheetNotExpandedFAB(FloatingActionButton fab) {
		this.fab = fab;
	}
	@Override public void onStateChanged(@NonNull View bottomSheet, @BottomSheetState int newState) {
		switch (newState) {
			case BottomSheetBehavior.STATE_EXPANDED:
				fab.hide();
				break;
			case BottomSheetBehavior.STATE_COLLAPSED:
			case BottomSheetBehavior.STATE_HIDDEN:
				fab.show();
				break;
			case BottomSheetBehavior.STATE_DRAGGING:
			case BottomSheetBehavior.STATE_SETTLING:
				break;
		}
	}
	@Override public void onSlide(@NonNull View bottomSheet, @FloatRange(from = -1, to = 1) float slideOffset) {
		if (slideOffset < 0) {
			fab.show();
		} else if (slideOffset > 0) {
			fab.hide();
		}
	}
}
