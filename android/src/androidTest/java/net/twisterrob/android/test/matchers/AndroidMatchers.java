package net.twisterrob.android.test.matchers;

import java.util.Locale;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.*;
import android.support.annotation.*;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;
import android.widget.ImageView;

import static android.support.test.InstrumentationRegistry.*;

import static net.twisterrob.android.test.junit.InstrumentationExtensions.*;
import static net.twisterrob.test.hamcrest.Matchers.*;

public class AndroidMatchers {
	public static @NonNull Matcher<Context> hasPackageInstalled(@NonNull String packageName) {
		return new HasInstalledPackage(packageName);
	}
	public static @NonNull Matcher<String> isString(@StringRes int stringId) {
		return equalTo(getTargetContext().getResources().getString(stringId));
	}
	public static @NonNull <T> Matcher<T> hasPropertyLite(
			@NonNull String propertyName, @NonNull Matcher<?> valueMatcher) {
		return HasPropertyWithValueLite.hasProperty(propertyName, valueMatcher);
	}
	public static @NonNull Matcher<Cursor> hasColumn(
			@NonNull String columnName, @NonNull Matcher<String> valueMatcher) {
		return CursorHasColumn.hasColumn(columnName, valueMatcher);
	}
	public static @NonNull Matcher<View> anyView() {
		return any(View.class);
	}
	public static @NonNull Matcher<String> containsStringRes(@StringRes int stringId) {
		return Matchers.containsString(getTargetContext().getResources().getString(stringId));
	}

	// region BuildConfig matchers
	public static @NonNull Matcher<Class<?>> isDebuggable() {
		return hasDebug(is(true));
	}
	public static @NonNull Matcher<Class<?>> hasDebug(Matcher<? super Boolean> valueMatcher) {
		return hasConstant("DEBUG", valueMatcher);
	}
	public static @NonNull Matcher<Class<?>> hasApplicationId(Matcher<? super String> valueMatcher) {
		return hasConstant("APPLICATION_ID", valueMatcher);
	}
	public static @NonNull Matcher<Class<?>> isDebugBuild() {
		return hasBuildType(is("debug"));
	}
	public static @NonNull Matcher<Class<?>> isReleaseBuild() {
		return hasBuildType(is("release"));
	}
	public static @NonNull Matcher<Class<?>> hasBuildType(Matcher<? super String> valueMatcher) {
		return hasConstant("BUILD_TYPE", valueMatcher);
	}
	public static @NonNull Matcher<Class<?>> hasFlavor(Matcher<? super String> valueMatcher) {
		return hasConstant("FLAVOR", valueMatcher);
	}
	public static @NonNull Matcher<Class<?>> hasVersionCode(Matcher<? super Integer> valueMatcher) {
		return hasConstant("VERSION_CODE", valueMatcher);
	}
	public static @NonNull Matcher<Class<?>> hasVersionName(Matcher<? super String> valueMatcher) {
		return hasConstant("VERSION_NAME", valueMatcher);
	}
	// endregion

	// region Activity matchers
	/**
	 * Don't use {@code not(isFinishing())}, the error message won't have enough information about the failure.
	 * Use {@code isFinishing(not(...)} instead.
	 * @see #notFinishing()
	 */
	public static @NonNull Matcher<Activity> isFinishing() {
		return isFinishing(is(true));
	}
	public static @NonNull Matcher<Activity> notFinishing() {
		return isFinishing(is(not(true)));
	}
	public static @NonNull Matcher<Activity> isFinishing(Matcher<Boolean> matcher) {
		return new FeatureMatcher<Activity, Boolean>(matcher, "activity finishing", "is finishing") {
			@Override protected Boolean featureValueOf(Activity actual) {
				return actual.isFinishing();
			}
		};
	}
	public static @NonNull Matcher<Activity> isInStage(Stage stage) {
		return isInStage(is(stage));
	}
	public static @NonNull Matcher<Activity> isInStage(Matcher<Stage> matcher) {
		return new FeatureMatcher<Activity, Stage>(matcher, "activity is in stage", "stage") {
			@Override protected Stage featureValueOf(Activity actual) {
				return getActivityStage(actual);
			}
		};
	}
	// endregion

	// region Bitmap matchers
	public static @NonNull Matcher<Bitmap> hasBackground(@ColorInt final int backgroundColor) {
		return new TypeSafeDiagnosingMatcher<Bitmap>() {
			@Override protected boolean matchesSafely(Bitmap item, Description mismatchDescription) {
				int pixel = item.getPixel(0, 0);
				if (pixel != backgroundColor) {
					mismatchDescription
							.appendText("top left pixel is ")
							.appendValue(String.format(Locale.ROOT, "#%08X", pixel));
					return false;
				}
				return true;
			}
			@Override public void describeTo(Description description) {
				description
						.appendText("has background color: ")
						.appendValue(String.format(Locale.ROOT, "#%08X", backgroundColor));
			}
		};
	}
	@SuppressWarnings("unchecked")
	public static @NonNull Matcher<View> withBitmap(final Matcher<Bitmap> matcher) {
		return (Matcher<View>)(Matcher<?>)new TypeSafeDiagnosingMatcher<ImageView>() {
			@Override public void describeTo(Description description) {
				description.appendText("ImageView with Bitmap: ").appendDescriptionOf(matcher);
			}
			@Override protected boolean matchesSafely(ImageView item, Description mismatchDescription) {
				Drawable drawable = item.getDrawable();
				Bitmap bitmap = null;
				boolean recycle = false;
				try {
					if (drawable instanceof BitmapDrawable) {
						bitmap = ((BitmapDrawable)drawable).getBitmap();
					} else {
						recycle = true;
						bitmap = Bitmap.createBitmap(
								drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
						Canvas canvas = new Canvas(bitmap);
						drawable.draw(canvas);
					}
					if (!matcher.matches(bitmap)) {
						matcher.describeMismatch(bitmap, mismatchDescription);
						return false;
					}
					return true;
				} finally {
					if (recycle && bitmap != null) {
						bitmap.recycle();
					}
				}
			}
		};
	}
	// endregion
}
