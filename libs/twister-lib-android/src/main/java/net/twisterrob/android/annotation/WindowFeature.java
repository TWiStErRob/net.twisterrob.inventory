package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import android.annotation.*;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;
import android.support.v4.view.WindowCompat;
import android.view.Window;

@SuppressLint("InlinedApi")
@TargetApi(VERSION_CODES.LOLLIPOP)
@IntDef({
		        Window.FEATURE_OPTIONS_PANEL,
		        Window.FEATURE_NO_TITLE,
		        Window.FEATURE_PROGRESS,
		        Window.FEATURE_LEFT_ICON,
		        Window.FEATURE_RIGHT_ICON,
		        Window.FEATURE_INDETERMINATE_PROGRESS,
		        Window.FEATURE_CONTEXT_MENU,
		        Window.FEATURE_CUSTOM_TITLE,
		        Window.FEATURE_ACTION_BAR,
		        WindowCompat.FEATURE_ACTION_BAR,
		        Window.FEATURE_ACTION_BAR_OVERLAY,
		        WindowCompat.FEATURE_ACTION_BAR_OVERLAY,
		        Window.FEATURE_ACTION_MODE_OVERLAY,
		        WindowCompat.FEATURE_ACTION_MODE_OVERLAY,
		        Window.FEATURE_SWIPE_TO_DISMISS,
		        Window.FEATURE_CONTENT_TRANSITIONS,
		        Window.FEATURE_ACTIVITY_TRANSITIONS
        })
@Retention(RetentionPolicy.SOURCE)
public @interface WindowFeature {
}
