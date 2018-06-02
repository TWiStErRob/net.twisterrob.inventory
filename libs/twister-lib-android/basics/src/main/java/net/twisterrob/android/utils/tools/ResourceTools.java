package net.twisterrob.android.utils.tools;

import java.util.Locale;

import org.slf4j.*;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.support.annotation.*;
import android.util.TypedValue;

import static android.util.TypedValue.*;

import static net.twisterrob.android.AndroidConstants.*;

@SuppressWarnings({"unused", "StaticMethodOnlyUsedInOneClass"})
public /*static*/ abstract class ResourceTools {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceTools.class);

	public static float dip(Context context, float number) {
		return TypedValue.applyDimension(COMPLEX_UNIT_DIP, number, context.getResources().getDisplayMetrics());
	}
	public static int dipInt(Context context, float number) {
		return (int)dip(context, number);
	}

	public static float sp(Context context, float number) {
		return TypedValue.applyDimension(COMPLEX_UNIT_SP, number, context.getResources().getDisplayMetrics());
	}
	public static int spInt(Context context, float number) {
		return (int)sp(context, number);
	}

	public static @RawRes int getRawResourceID(@Nullable Context context, @NonNull String rawResourceName) {
		return getResourceID(context, RES_TYPE_RAW, rawResourceName);
	}

	public static @IdRes int getIDResourceID(@Nullable Context context, String idResourceName) {
		return getResourceID(context, RES_TYPE_ID, idResourceName);
	}

	public static @ColorRes int getColorResourceID(@Nullable Context context, String drawableResourceName) {
		return getResourceID(context, RES_TYPE_COLOR, drawableResourceName);
	}

	public static @DrawableRes int getDrawableResourceID(@Nullable Context context, String drawableResourceName) {
		return getResourceID(context, RES_TYPE_DRAWABLE, drawableResourceName);
	}

	public static @StringRes int getStringResourceID(@Nullable Context context, @NonNull String stringResourceName) {
		return getResourceID(context, RES_TYPE_STRING, stringResourceName);
	}

	public static @NonNull CharSequence getText(@NonNull Context context, @NonNull String stringResourceName) {
		int id = getStringResourceID(context, stringResourceName);
		if (id == INVALID_RESOURCE_ID) {
			throw new NotFoundException(String.format(Locale.ROOT, "Resource '%s' is not a valid string in '%s'",
					stringResourceName, context.getPackageName()));
		}
		try {
			return context.getText(id);
		} catch (NotFoundException ex) {
			//noinspection UnnecessaryInitCause NotFoundException(String, ex) was added in API 24
			throw (NotFoundException)new NotFoundException(
					String.format(Locale.ROOT, "Resource '%s' is not a valid string in '%s'",
							stringResourceName, context.getPackageName())
			).initCause(ex);
		}
	}

	private static @AnyRes int getResourceID(@Nullable Context context,
			@NonNull String resourceType, @NonNull String resourceName) {
		int resID;
		if (context != null) {
			resID = context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
		} else {
			resID = Resources.getSystem().getIdentifier(resourceName, resourceType, ANDROID_PACKAGE);
		}
		if (resID == INVALID_RESOURCE_ID) {
			LOG.warn("No {} resource found with name '{}' in package '{}'",
					resourceType, resourceName, context != null? context.getPackageName() : null);
		}
		return resID;
	}

	private ResourceTools() {
		// static utility class
	}
}
