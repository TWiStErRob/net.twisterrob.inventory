package net.twisterrob.android.test.matchers;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import android.content.Context;
import android.support.annotation.*;

import static android.support.test.InstrumentationRegistry.*;

public class AndroidMatchers {
	public static @NonNull Matcher<Context> hasPackageInstalled(@NonNull String packageName) {
		return new HasInstalledPackage(packageName);
	}
	public static @NonNull Matcher<String> isString(@StringRes int stringId) {
		return equalTo(getTargetContext().getResources().getString(stringId));
	}
	public static @NonNull <T> Matcher<T> hasProperty(@NonNull String propertyName, @NonNull Matcher<?> valueMatcher) {
		return HasPropertyWithValueLite.hasProperty(propertyName, valueMatcher);
	}
}
