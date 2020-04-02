package net.twisterrob.android.view;

import java.util.*;

import android.support.annotation.*;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.view.View;

import net.twisterrob.android.annotation.BottomSheetState;

public class MultiBottomSheetCallback extends BottomSheetCallback {
	private final BottomSheetCallback[] callbacks;
	public MultiBottomSheetCallback(BottomSheetCallback... callbacks) {
		this.callbacks = callbacks;
	}
	@Override public void onStateChanged(@NonNull View bottomSheet, @BottomSheetState int newState) {
		for (BottomSheetCallback callback : callbacks) {
			callback.onStateChanged(bottomSheet, newState);
		}
	}
	@Override public void onSlide(@NonNull View bottomSheet, @FloatRange(from = -1, to = 1) float slideOffset) {
		for (BottomSheetCallback callback : callbacks) {
			callback.onSlide(bottomSheet, slideOffset);
		}
	}

	public static class Builder {
		private final List<BottomSheetCallback> callbacks = new LinkedList<>();
		public Builder add(BottomSheetCallback callback) {
			callbacks.add(callback);
			return this;
		}
		public BottomSheetCallback build() {
			if (callbacks.size() == 1) {
				return callbacks.get(0);
			} else {
				return new MultiBottomSheetCallback(callbacks.toArray(new BottomSheetCallback[callbacks.size()]));
			}
		}
	}
}
