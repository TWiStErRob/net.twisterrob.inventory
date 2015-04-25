package net.twisterrob.android.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

import android.annotation.*;
import android.content.ComponentCallbacks2;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;

@SuppressLint("InlinedApi")
@TargetApi(VERSION_CODES.JELLY_BEAN)
@Target({FIELD, LOCAL_VARIABLE, PARAMETER})
@IntDef({
		        ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
		        ComponentCallbacks2.TRIM_MEMORY_MODERATE,
		        ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
		        ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
		        ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,
		        ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
		        ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE
        })
@Retention(RetentionPolicy.SOURCE)
public @interface TrimMemoryLevel {
}
