package net.twisterrob.android.test.matchers;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import org.hamcrest.*;

import static org.hamcrest.Matchers.*;

import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.*;
import android.os.Build.*;
import android.preference.Preference;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.test.espresso.*;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.runner.lifecycle.Stage;

import static androidx.test.core.app.ApplicationProvider.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.java.utils.ReflectionTools;
import net.twisterrob.test.hamcrest.NamedMatcher;

import static net.twisterrob.android.test.junit.InstrumentationExtensions.*;
import static net.twisterrob.test.hamcrest.Matchers.*;

public class AndroidMatchers {
	public static @NonNull <T> Matcher<T> nothing() {
		return new BaseMatcher<T>() {
			@Override public void describeTo(Description description) {
				description.appendText("nothing");
			}
			@Override public boolean matches(Object item) {
				return false;
			}
		};
	}
	public static @NonNull <T> Matcher<T> once(Matcher<T> matcher) {
		return new OnceMatcher<>(matcher);
	}
	public static @NonNull Matcher<String> containsWord(String word) {
		return new NamedMatcher<>("contains word '" + word + "'",
				matchesPattern("^.*\\b" + Pattern.quote(word) + "\\b.*$"));
	}
	public static @NonNull Matcher<Context> hasPackageInstalled(@NonNull String packageName) {
		return new HasInstalledPackage(packageName);
	}
	public static @NonNull Matcher<Intent> canBeResolvedTo(final Matcher<ResolveInfo> resolveInfoMatcher) {
		return canBeResolvedTo(0, resolveInfoMatcher);
	}
	public static @NonNull Matcher<Intent> canBeResolvedTo(
			final int flags, final Matcher<ResolveInfo> resolveInfoMatcher) {
		return new TypeSafeMatcher<Intent>() {
			@Override protected boolean matchesSafely(Intent intent) {
				ResolveInfo info = getApplicationContext().getPackageManager().resolveActivity(intent, flags);
				return resolveInfoMatcher.matches(info);
			}
			@Override public void describeTo(Description description) {
				description.appendText("Intent can be resolved with flags: ").appendValue(flags)
				           .appendText(" to ").appendDescriptionOf(resolveInfoMatcher);
			}
		};
	}
	public static @NonNull Matcher<Intent> canBeResolved(final Matcher<? super List<ResolveInfo>> resolveInfoMatcher) {
		return canBeResolved(0, resolveInfoMatcher);
	}
	public static @NonNull Matcher<Intent> canBeResolved(
			final int flags, final Matcher<? super List<ResolveInfo>> resolveInfoMatcher) {
		return new FeatureMatcher<Intent, List<ResolveInfo>>(resolveInfoMatcher,
				"Intent resolves to activities", "resolved activities") {
			@Override protected List<ResolveInfo> featureValueOf(Intent intent) {
				return getApplicationContext().getPackageManager().queryIntentActivities(intent, flags);
			}
		};
	}

	public static @NonNull Matcher<String> isString(@StringRes int stringId) {
		return equalTo(getApplicationContext().getResources().getString(stringId));
	}
	public static @NonNull Matcher<CharSequence> isText(@StringRes int textId) {
		return equalTo(getApplicationContext().getResources().getText(textId));
	}
	public static @NonNull Matcher<CharSequence> cs(final Matcher<String> stringMatcher) {
		return new TypeSafeDiagnosingMatcher<CharSequence>() {
			@Override protected boolean matchesSafely(CharSequence item, Description mismatchDescription) {
				return stringMatcher.matches(item.toString());
			}
			@Override public void describeTo(Description description) {
				description.appendDescriptionOf(stringMatcher).appendText(" as CharSequence");
			}
		};
	}

	/**
	 * Matches {@code %[argument_index$][flags][width][.precision][t]conversion}.
	 * @see java.util.Formatter#formatSpecifier formatSpecifier in the JDK
	 */
	@SuppressWarnings("JavadocReference")
	private static final Pattern formatSpecifier =
			Pattern.compile("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");
	public static @NonNull Matcher<String> formattedRes(@StringRes int stringId) {
		String format = getApplicationContext().getResources().getString(stringId);
		return matchesPattern(formatSpecifier.matcher(format).replaceAll(".*?"));
	}

	// region View matchers
	public static @NonNull Matcher<View> anyView() {
		return any(View.class);
	}

	public static @NonNull Matcher<View> withErrorText(final Matcher<String> stringMatcher) {
		return new BoundedMatcher<View, TextView>(TextView.class) {
			@Override public void describeTo(final Description description) {
				description.appendText("with error text: ").appendDescriptionOf(stringMatcher);
			}

			@Override public boolean matchesSafely(final TextView textView) {
				return stringMatcher.matches(textView.getError().toString());
			}
		};
	}

	public static @NonNull Matcher<View> withWidth(@NonNull Matcher<Integer> widthMatcher) {
		return new FeatureMatcher<View, Integer>(widthMatcher, "width of the view", "width") {
			@Override protected Integer featureValueOf(View actual) {
				return actual.getWidth();
			}
		};
	}
	public static @NonNull Matcher<View> withHeight(@NonNull Matcher<Integer> heightMatcher) {
		return new FeatureMatcher<View, Integer>(heightMatcher, "height of the view", "height") {
			@Override protected Integer featureValueOf(View actual) {
				return actual.getHeight();
			}
		};
	}
	public static @NonNull Matcher<View> withSize(@NonNull Matcher<Integer> sizeMatcher) {
		return allOf(withWidth(sizeMatcher), withHeight(sizeMatcher));
	}

	public static @NonNull ViewAction checkIfUnchecked() {
		return new ViewAction() {
			@Override public String getDescription() {
				return "check Checkable if unchecked";
			}
			@Override public Matcher<View> getConstraints() {
				return allOf(instanceOf(Checkable.class), isNotChecked());
			}
			@Override public void perform(UiController uiController, View view) {
				click().perform(uiController, view);
			}
		};
	}

	public static @NonNull ViewAction uncheckIfChecked() {
		return new ViewAction() {
			@Override public String getDescription() {
				return "uncheck Checkable if checked";
			}
			@Override public Matcher<View> getConstraints() {
				return allOf(instanceOf(Checkable.class), isChecked());
			}
			@Override public void perform(UiController uiController, View view) {
				click().perform(uiController, view);
			}
		};
	}
	// endregion View matchers

	public static @NonNull <T> Matcher<T> hasPropertyLite(
			@NonNull String propertyName, @NonNull Matcher<?> valueMatcher) {
		return HasPropertyWithValueLite.hasProperty(propertyName, valueMatcher);
	}
	public static @NonNull String stringRes(@StringRes int stringId) {
		return getApplicationContext().getResources().getString(stringId);
	}
	public static @NonNull Matcher<String> containsStringRes(@StringRes int stringId) {
		return Matchers.containsString(getApplicationContext().getResources().getString(stringId));
	}

	// region Cursor matchers
	public static Matcher<Cursor> withNoColumn(final String columnName) {
		return new TypeSafeDiagnosingMatcher<Cursor>() {
			@Override protected boolean matchesSafely(Cursor cursor, Description mismatchDescription) {
				int columnIndex = cursor.getColumnIndex(columnName);
				if (columnIndex != DatabaseTools.INVALID_COLUMN) {
					mismatchDescription
							.appendText("Column named ")
							.appendValue(columnName)
							.appendText(" found in ")
							.appendValueList("[", ", ", "]", cursor.getColumnNames());
					return false;
				}
				return true;
			}
			@Override public void describeTo(Description description) {
				description.appendText("with no column named ").appendValue(columnName);
			}
		};
	}
	public static Matcher<Cursor> withStringColumn(String columnName, Matcher<String> valueMatcher) {
		return withColumn(columnName, String.class, valueMatcher);
	}
	public static Matcher<Cursor> withColumn(String columnName, String expectedValue) {
		return withStringColumn(columnName, is(expectedValue));
	}
	public static Matcher<Cursor> withColumn(String columnName, long expectedValue) {
		return withColumn(columnName, Long.TYPE, is(expectedValue));
	}
	public static Matcher<Cursor> withColumn(String columnName, Long expectedValue) {
		return withLongColumn(columnName, is(expectedValue));
	}
	public static Matcher<Cursor> withLongColumn(String columnName, Matcher<Long> valueMatcher) {
		return withColumn(columnName, Long.class, valueMatcher);
	}
	public static <T> Matcher<Cursor> withColumn(String columnName, Class<T> columnType, Matcher<T> valueMatcher) {
		return new CursorHasColumn<>(columnName, columnType, valueMatcher);
	}
	// endregion

	// region Preference matchers
	public static @NonNull Matcher<Preference> withKey(String key) {
		return withKey(equalTo(key));
	}
	public static @NonNull Matcher<Preference> withKey(final Matcher<String> keyMatcher) {
		return new FeatureMatcher<Preference, String>(keyMatcher, "Preference with key", "key") {
			@Override protected String featureValueOf(Preference actual) {
				return actual.getKey();
			}
		};
	}
	public static @NonNull Matcher<Preference> withTitle(CharSequence title) {
		return withTitle(equalTo(title));
	}
	public static @NonNull Matcher<Preference> withTitle(final Matcher<CharSequence> titleMatcher) {
		return new FeatureMatcher<Preference, CharSequence>(titleMatcher, "Preference with title", "title") {
			@Override protected CharSequence featureValueOf(Preference actual) {
				return actual.getTitle();
			}
		};
	}
	public static @NonNull Matcher<Preference> withSummary(CharSequence summary) {
		return withSummary(equalTo(summary));
	}
	public static @NonNull Matcher<Preference> withSummary(final Matcher<CharSequence> summaryMatcher) {
		return new FeatureMatcher<Preference, CharSequence>(summaryMatcher, "Preference with summary", "summary") {
			@Override protected CharSequence featureValueOf(Preference actual) {
				return actual.getSummary();
			}
		};
	}
	// endregion

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

	// region Search matchers
	public static @NonNull Matcher<View> isSearchView() {
		return anyOf(
				isAssignableFrom(androidx.appcompat.widget.SearchView.class),
				isAssignableFrom(android.widget.SearchView.class)
		);
	}
	public static @NonNull Matcher<Cursor> searchSuggestion(Matcher<String> titleMatcher) {
		return withStringColumn(SearchManager.SUGGEST_COLUMN_TEXT_1, titleMatcher);
	}
	// endregion

	// region Bitmap matchers
	public static @NonNull Matcher<Bitmap> hasBackgroundColor(@ColorInt final int backgroundColor) {
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

	// region AdapterView matchers
	public static @NonNull Matcher<Integer> invalidPosition() {
		return is(AdapterView.INVALID_POSITION);
	}

	/**
	 * @see #invalidPosition()
	 */
	@SuppressWarnings("unchecked")
	public static @NonNull Matcher<View> selectedPosition(Matcher<Integer> positionMatcher) {
		return (Matcher<View>)(Matcher<?>)new FeatureMatcher<AdapterView<?>, Integer>(
				positionMatcher, "AdapterView with selected position", "selected position") {
			@Override protected Integer featureValueOf(AdapterView<?> actual) {
				return actual.getSelectedItemPosition();
			}
		};
	}

	public static @NonNull Matcher<View> isItemChecked() {
		return new TypeSafeDiagnosingMatcher<View>() {
			@TargetApi(VERSION_CODES.HONEYCOMB)
			@Override protected boolean matchesSafely(View item, Description mismatchDescription) {
				ViewParent parent = item.getParent();
				if (!(parent instanceof AbsListView)) {
					mismatchDescription.appendText("view is not an item");
					return false;
				}
				AbsListView absList = (AbsListView)parent;
				int position = absList.getPositionForView(item);
				if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
					boolean checked = absList.isItemChecked(position);
					if (!checked) {
						mismatchDescription.appendText("item is not checked");
					}
					return checked;
				} else if (absList instanceof ListView) {
					ListView list = (ListView)absList;
					boolean checked = list.isItemChecked(position);
					if (!checked) {
						mismatchDescription.appendText("item is not checked");
					}
					return checked;
				} else {
					mismatchDescription.appendText(absList + " doesn't support item checking");
					return false;
				}
			}
			@Override public void describeTo(Description description) {
				description.appendText("list item checked");
			}
		};
	}

	/**
	 * @see #invalidPosition()
	 */
	@SuppressWarnings("unchecked")
	public static @NonNull Matcher<View> checkedPosition(Matcher<Integer> positionMatcher) {
		return (Matcher<View>)(Matcher<?>)new FeatureMatcher<AbsListView, Integer>(
				positionMatcher, "AbsListView with checked position", "checked position") {
			@TargetApi(VERSION_CODES.HONEYCOMB)
			@Override protected Integer featureValueOf(AbsListView actual) {
				if (VERSION_CODES.HONEYCOMB <= VERSION.SDK_INT) {
					return actual.getCheckedItemPosition();
				} else {
					if (actual instanceof ListView) {
						ListView casted = (ListView)actual;
						return casted.getCheckedItemPosition();
					} else {
						Method getCheckedItemPosition =
								ReflectionTools.tryFindDeclaredMethod(actual.getClass(), "getCheckedItemPosition");
						if (getCheckedItemPosition != null) {
							try {
								return (int)getCheckedItemPosition.invoke(actual);
							} catch (Exception ignore) {
								// whatever, it was best effort for old versions of Android
							}
						}
						throw new UnsupportedOperationException(actual + " does not support getCheckedItemPosition");
					}
				}
			}
		};
	}
	// endregion
}
