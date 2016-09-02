package net.twisterrob.android.test.matchers;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.*;

import static android.support.test.InstrumentationRegistry.*;

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
}
