package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.SuppressLint;
import android.content.res.Configuration;

import androidx.annotation.*;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressLint("InlinedApi")
@IntDef(value = {
		Configuration.DENSITY_DPI_UNDEFINED,
		// @hide Configuration.DENSITY_DPI_ANY
		0xfffe,
		// @hide Configuration.DENSITY_DPI_NONE
		0xffff
})
@IntRange(from = 0)
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ConfigurationDensityDpi {
	class Converter {
		private static final int DENSITY_DPI_ANY = 0xfffe;
		private static final int DENSITY_DPI_NONE = 0xffff;
		@SuppressLint("SwitchIntDef")
		@DebugHelper
		public static String toString(@ConfigurationDensityDpi int dpi) {
			switch (dpi) {
				case Configuration.DENSITY_DPI_UNDEFINED:
					return "DENSITY_DPI_UNDEFINED";
				case DENSITY_DPI_ANY:
					return "DENSITY_DPI_ANY";
				case DENSITY_DPI_NONE:
					return "DENSITY_DPI_NONE(nodpi)";
			}
			return "dpi::" + dpi;
		}
	}
}
