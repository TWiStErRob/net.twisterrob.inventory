package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.content.res.Configuration;

import androidx.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressWarnings("deprecation")
@IntDef(value = {
		Configuration.TOUCHSCREEN_UNDEFINED,
		Configuration.TOUCHSCREEN_NOTOUCH,
		Configuration.TOUCHSCREEN_FINGER,
		Configuration.TOUCHSCREEN_STYLUS
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ConfigurationTouchscreen {
	class Converter {
		@DebugHelper
		public static String toString(@ConfigurationTouchscreen int touch) {
			switch (touch) {
				case Configuration.TOUCHSCREEN_UNDEFINED:
					return "TOUCHSCREEN_UNDEFINED";
				case Configuration.TOUCHSCREEN_NOTOUCH:
					return "TOUCHSCREEN_NOTOUCH(notouch)";
				case Configuration.TOUCHSCREEN_FINGER:
					return "TOUCHSCREEN_FINGER(finger)";
				case Configuration.TOUCHSCREEN_STYLUS:
					return "TOUCHSCREEN_STYLUS";
			}
			return "touchscreen::" + touch;
		}
	}
}
