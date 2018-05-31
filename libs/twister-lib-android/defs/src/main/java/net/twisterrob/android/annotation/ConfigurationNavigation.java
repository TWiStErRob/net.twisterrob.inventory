package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.content.res.Configuration;
import android.support.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@IntDef(value = {
		Configuration.NAVIGATION_UNDEFINED,
		Configuration.NAVIGATION_NONAV,
		Configuration.NAVIGATION_DPAD,
		Configuration.NAVIGATION_TRACKBALL,
		Configuration.NAVIGATION_WHEEL
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ConfigurationNavigation {
	class Converter {
		@DebugHelper
		public static String toString(@ConfigurationNavigation int nav) {
			switch (nav) {
				case Configuration.NAVIGATION_UNDEFINED:
					return "NAVIGATION_UNDEFINED";
				case Configuration.NAVIGATION_NONAV:
					return "NAVIGATION_NONAV(nonav)";
				case Configuration.NAVIGATION_DPAD:
					return "NAVIGATION_DPAD(dpad)";
				case Configuration.NAVIGATION_TRACKBALL:
					return "NAVIGATION_TRACKBALL(trackball)";
				case Configuration.NAVIGATION_WHEEL:
					return "NAVIGATION_WHEEL(wheel)";
			}
			return "navigation::" + nav;
		}
	}
}
