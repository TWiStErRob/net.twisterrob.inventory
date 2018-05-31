package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.support.annotation.IntDef;

import static android.content.res.Configuration.*;

import net.twisterrob.java.annotations.DebugHelper;

@SuppressLint({"InlinedApi", "UniqueConstants"})
@IntDef(value = {
		Configuration.SCREENLAYOUT_SIZE_MASK,
		Configuration.SCREENLAYOUT_SIZE_UNDEFINED,
		Configuration.SCREENLAYOUT_SIZE_SMALL,
		Configuration.SCREENLAYOUT_SIZE_NORMAL,
		Configuration.SCREENLAYOUT_SIZE_LARGE,
		Configuration.SCREENLAYOUT_SIZE_XLARGE,

		Configuration.SCREENLAYOUT_LONG_MASK,
		Configuration.SCREENLAYOUT_LONG_UNDEFINED,
		Configuration.SCREENLAYOUT_LONG_NO,
		Configuration.SCREENLAYOUT_LONG_YES,

		Configuration.SCREENLAYOUT_LAYOUTDIR_MASK,
		Configuration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED,
		Configuration.SCREENLAYOUT_LAYOUTDIR_LTR,
		Configuration.SCREENLAYOUT_LAYOUTDIR_RTL,

		Configuration.SCREENLAYOUT_ROUND_MASK,
		Configuration.SCREENLAYOUT_ROUND_UNDEFINED,
		Configuration.SCREENLAYOUT_ROUND_YES,
		Configuration.SCREENLAYOUT_ROUND_NO,

		Configuration.SCREENLAYOUT_UNDEFINED,
		0x10000000 // @hide Configuration.SCREENLAYOUT_COMPAT_NEEDED 
})
@Retention(RetentionPolicy.SOURCE)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
public @interface ConfigurationScreenLayout {
	class Converter {
		private static final int SCREENLAYOUT_COMPAT_NEEDED = 0x10000000;
		@SuppressWarnings("WrongConstant")
		@DebugHelper
		public static String toString(@ConfigurationScreenLayout int layout) {
			if (layout == SCREENLAYOUT_UNDEFINED) {
				return "SCREENLAYOUT_UNDEFINED";
			}
			String size = getSize(layout & Configuration.SCREENLAYOUT_SIZE_MASK);
			String isLong = getLong(layout & Configuration.SCREENLAYOUT_LONG_MASK);
			String dir = getLayoutDir(layout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK);
			String round = getRound(layout & Configuration.SCREENLAYOUT_ROUND_MASK);
			String result = size + "|" + isLong + "|" + dir;
			if ((layout & SCREENLAYOUT_COMPAT_NEEDED) != 0) {
				result += "|" + "SCREENLAYOUT_COMPAT_NEEDED";
			}
			int remainder = layout & ~(SCREENLAYOUT_COMPAT_NEEDED
					| Configuration.SCREENLAYOUT_SIZE_MASK
					| Configuration.SCREENLAYOUT_LONG_MASK
					| Configuration.SCREENLAYOUT_LAYOUTDIR_MASK
					| Configuration.SCREENLAYOUT_ROUND_MASK
			);
			if (remainder != 0) {
				result += "|" + "unknown" + "(" + Integer.toHexString(remainder) + ")";
			}
			return result;
		}

		@SuppressLint("SwitchIntDef")
		private static String getSize(@ConfigurationScreenLayout int screenLayout) {
			switch (screenLayout) {
				case Configuration.SCREENLAYOUT_SIZE_MASK:
					return "SCREENLAYOUT_SIZE_MASK";
				case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
					return "SCREENLAYOUT_SIZE_UNDEFINED";
				case Configuration.SCREENLAYOUT_SIZE_SMALL:
					return "SCREENLAYOUT_SIZE_SMALL(small)";
				case Configuration.SCREENLAYOUT_SIZE_NORMAL:
					return "SCREENLAYOUT_SIZE_NORMAL(normal)";
				case Configuration.SCREENLAYOUT_SIZE_LARGE:
					return "SCREENLAYOUT_SIZE_LARGE(large)";
				case Configuration.SCREENLAYOUT_SIZE_XLARGE:
					return "SCREENLAYOUT_SIZE_XLARGE(xlarge)";
			}
			return "size::" + screenLayout;
		}
		@SuppressLint("SwitchIntDef")
		private static String getLayoutDir(@ConfigurationScreenLayout int screenLayout) {
			switch (screenLayout) {
				case Configuration.SCREENLAYOUT_LAYOUTDIR_MASK:
					return "SCREENLAYOUT_LAYOUTDIR_MASK";
				case Configuration.SCREENLAYOUT_LAYOUTDIR_UNDEFINED:
					return "SCREENLAYOUT_LAYOUTDIR_UNDEFINED";
				case Configuration.SCREENLAYOUT_LAYOUTDIR_LTR:
					return "SCREENLAYOUT_LAYOUTDIR_LTR(ldltr)";
				case Configuration.SCREENLAYOUT_LAYOUTDIR_RTL:
					return "SCREENLAYOUT_LAYOUTDIR_RTL(ldrtl)";
			}
			return "layoutDir::" + screenLayout;
		}
		@SuppressLint("SwitchIntDef")
		private static String getLong(@ConfigurationScreenLayout int screenLayout) {
			switch (screenLayout) {
				case Configuration.SCREENLAYOUT_LONG_MASK:
					return "SCREENLAYOUT_LONG_MASK";
				case Configuration.SCREENLAYOUT_LONG_UNDEFINED:
					return "SCREENLAYOUT_LONG_UNDEFINED";
				case Configuration.SCREENLAYOUT_LONG_NO:
					return "SCREENLAYOUT_LONG_NO(notlong)";
				case Configuration.SCREENLAYOUT_LONG_YES:
					return "SCREENLAYOUT_LONG_YES(long)";
			}
			return "long::" + screenLayout;
		}
		@SuppressLint("SwitchIntDef")
		private static String getRound(@ConfigurationScreenLayout int screenLayout) {
			switch (screenLayout) {
				case Configuration.SCREENLAYOUT_ROUND_MASK:
					return "SCREENLAYOUT_ROUND_MASK";
				case Configuration.SCREENLAYOUT_ROUND_UNDEFINED:
					return "SCREENLAYOUT_ROUND_UNDEFINED";
				case Configuration.SCREENLAYOUT_ROUND_NO:
					return "SCREENLAYOUT_ROUND_NO(notround)";
				case Configuration.SCREENLAYOUT_ROUND_YES:
					return "SCREENLAYOUT_ROUND_YES(round)";
			}
			return "round::" + screenLayout;
		}
	}
}
