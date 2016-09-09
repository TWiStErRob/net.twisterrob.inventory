package net.twisterrob.android.test.matchers;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.*;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;

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
	public static @NonNull Matcher<String> containsString(@StringRes int stringId) {
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
}
