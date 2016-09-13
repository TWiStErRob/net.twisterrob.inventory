package net.twisterrob.android.utils.tostring.stringers.detailed;

import javax.annotation.Nonnull;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build.*;

import net.twisterrob.android.annotation.*;
import net.twisterrob.java.utils.tostring.*;

@TargetApi(VERSION_CODES.N)
public class ConfigurationStringer extends Stringer<Configuration> {
	@SuppressWarnings("deprecation")
	@Override public void toString(@Nonnull ToStringAppender append, Configuration config) {
		append.beginPropertyGroup("screen");
		append.rawProperty("orientation", ConfigurationOrientation.Converter.toString(config.orientation));
		append.rawProperty("touchscreen", ConfigurationTouchscreen.Converter.toString(config.touchscreen));
		append.rawProperty("layout", ConfigurationScreenLayout.Converter.toString(config.screenLayout));
		if (VERSION_CODES.M <= VERSION.SDK_INT) {
			append.booleanProperty(config.isScreenRound(), "round");
		}
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			append.rawProperty("density", ConfigurationDensityDpi.Converter.toString(config.densityDpi));
		}
		if (VERSION_CODES.HONEYCOMB_MR2 <= VERSION.SDK_INT) {
			if (config.screenWidthDp == Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
				append.rawProperty("width", "SCREEN_WIDTH_DP_UNDEFINED");
			} else {
				append.measuredProperty("width", "dp", config.screenWidthDp);
			}
			if (config.screenHeightDp == Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
				append.rawProperty("height", "SCREEN_HEIGHT_DP_UNDEFINED");
			} else {
				append.measuredProperty("height", "dp", config.screenHeightDp);
			}
			if (config.smallestScreenWidthDp == Configuration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
				append.rawProperty("smallestWidth", "SMALLEST_SCREEN_WIDTH_DP_UNDEFINED");
			} else {
				append.measuredProperty("smallestWidth", "dp", config.smallestScreenWidthDp);
			}
		}
		append.complexProperty("fontScale", config.fontScale);
		append.endPropertyGroup();

		append.beginPropertyGroup("layout");
		if (VERSION_CODES.JELLY_BEAN_MR1 <= VERSION.SDK_INT) {
			//noinspection WrongConstant
			append.rawProperty("direction", ViewLayoutDirection.Converter.toString(config.getLayoutDirection()));
		}
		append.rawProperty("uiMode", ConfigurationUIMode.Converter.toString(config.uiMode));
		append.endPropertyGroup();

		append.beginPropertyGroup("local");
		append.complexProperty("locale", config.locale);
		if (VERSION_CODES.N <= VERSION.SDK_INT) {
			append.complexProperty("locales", config.getLocales());
		}
		if (config.mnc == Configuration.MNC_ZERO) {
			append.rawProperty("MNC(mobile Network code)", "MNC_ZERO");
		} else {
			append.rawProperty("MNC(mobile Network code)", config.mnc);
		}
		if (config.mcc == 0) {
			append.rawProperty("MCC(mobile Country code)", "undefined");
		} else {
			append.rawProperty("MCC(mobile Country code)", config.mcc);
		}
		append.endPropertyGroup();

		append.beginPropertyGroup("keyboard");
		append.selfDescribingProperty(ConfigurationKeyboard.Converter.toString(config.keyboard));
		append.rawProperty("hidden", ConfigurationKeyboardHidden.Converter.toString(config.keyboardHidden));
		append.rawProperty("hardHidden", ConfigurationHardKeyboardHidden.Converter.toString(config.hardKeyboardHidden));
		append.endPropertyGroup();

		append.beginPropertyGroup("navigation");
		append.selfDescribingProperty(ConfigurationNavigation.Converter.toString(config.navigation));
		append.rawProperty("hidden", ConfigurationNavigationHidden.Converter.toString(config.navigationHidden));
		append.endPropertyGroup();
	}
}

