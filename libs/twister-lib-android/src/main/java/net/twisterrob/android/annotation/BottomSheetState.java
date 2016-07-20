package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.support.annotation.IntDef;
import android.support.design.widget.BottomSheetBehavior;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		BottomSheetBehavior.STATE_EXPANDED,
		BottomSheetBehavior.STATE_COLLAPSED,
		BottomSheetBehavior.STATE_DRAGGING,
		BottomSheetBehavior.STATE_SETTLING,
		BottomSheetBehavior.STATE_HIDDEN
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface BottomSheetState {
	class Converter {
		@DebugHelper
		public static String toString(@BottomSheetState int state) {
			switch (state) {
				case BottomSheetBehavior.STATE_EXPANDED:
					return "STATE_EXPANDED";
				case BottomSheetBehavior.STATE_COLLAPSED:
					return "STATE_COLLAPSED";
				case BottomSheetBehavior.STATE_DRAGGING:
					return "STATE_DRAGGING";
				case BottomSheetBehavior.STATE_SETTLING:
					return "STATE_SETTLING";
				case BottomSheetBehavior.STATE_HIDDEN:
					return "STATE_HIDDEN";
			}
			return "bottomSheetState::" + state;
		}
	}
}
