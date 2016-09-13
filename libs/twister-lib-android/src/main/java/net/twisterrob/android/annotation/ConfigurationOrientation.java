package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.content.res.Configuration;
import android.support.annotation.IntDef;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressWarnings("deprecation")
@IntDef(value = {
		Configuration.ORIENTATION_UNDEFINED,
		Configuration.ORIENTATION_LANDSCAPE,
		Configuration.ORIENTATION_PORTRAIT,
		Configuration.ORIENTATION_SQUARE
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ConfigurationOrientation {
	class Converter {
		@DebugHelper
		public static String toString(@ConfigurationOrientation int state) {
			switch (state) {
				case Configuration.ORIENTATION_UNDEFINED:
					return "ORIENTATION_UNDEFINED";
				case Configuration.ORIENTATION_LANDSCAPE:
					return "ORIENTATION_LANDSCAPE(land)";
				case Configuration.ORIENTATION_PORTRAIT:
					return "ORIENTATION_PORTRAIT(port)";
				case Configuration.ORIENTATION_SQUARE:
					return "ORIENTATION_SQUARE";
			}
			return "orientation::" + state;
		}
	}
}
