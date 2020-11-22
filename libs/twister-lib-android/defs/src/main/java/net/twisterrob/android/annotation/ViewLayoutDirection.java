package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressLint("InlinedApi")
@IntDef(value = {
		View.LAYOUT_DIRECTION_RTL,
		View.LAYOUT_DIRECTION_LTR,
		View.LAYOUT_DIRECTION_LOCALE,
		View.LAYOUT_DIRECTION_INHERIT
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ViewLayoutDirection {
	class Converter {
		@DebugHelper
		public static String toString(@ViewLayoutDirection int dir) {
			switch (dir) {
				case View.LAYOUT_DIRECTION_RTL:
					return "LAYOUT_DIRECTION_RTL";
				case View.LAYOUT_DIRECTION_LTR:
					return "LAYOUT_DIRECTION_LTR";
				case View.LAYOUT_DIRECTION_LOCALE:
					return "LAYOUT_DIRECTION_LOCALE";
				case View.LAYOUT_DIRECTION_INHERIT:
					return "LAYOUT_DIRECTION_INHERIT";
			}
			return "layoutDirection::" + dir;
		}
	}
}
