package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.*;
import android.content.ComponentCallbacks2;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressLint("InlinedApi")
@TargetApi(VERSION_CODES.JELLY_BEAN)
@IntDef(value = {
		ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
		ComponentCallbacks2.TRIM_MEMORY_MODERATE,
		ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
		ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
		ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
		ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
		ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface TrimMemoryLevel {
	class Converter {
		/** @see ComponentCallbacks2 */
		@DebugHelper
		public static String toString(@TrimMemoryLevel int level) {
			switch (level) {
				case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
					return "TRIM_MEMORY_COMPLETE";
				case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
					return "TRIM_MEMORY_MODERATE";
				case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
					return "TRIM_MEMORY_BACKGROUND";
				case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
					return "TRIM_MEMORY_UI_HIDDEN";
				case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
					return "TRIM_MEMORY_RUNNING_CRITICAL";
				case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
					return "TRIM_MEMORY_RUNNING_LOW";
				case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
					return "TRIM_MEMORY_RUNNING_MODERATE";
			}
			return "trimMemoryLevel::" + level;
		}
	}
}
